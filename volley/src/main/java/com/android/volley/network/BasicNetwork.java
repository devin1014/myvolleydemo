/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.network;

import android.os.SystemClock;

import com.android.volley.Cache.Entry;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyLog;
import com.android.volley.VolleyLog.NetworkLog;
import com.android.volley.exception.AuthFailureError;
import com.android.volley.exception.ClientError;
import com.android.volley.exception.NetworkError;
import com.android.volley.exception.NoConnectionError;
import com.android.volley.exception.ServerError;
import com.android.volley.exception.TimeoutError;
import com.android.volley.exception.VolleyError;
import com.android.volley.network.Interceptors.CacheHeaderInterceptor;
import com.android.volley.network.Interceptors.HeaderInterceptors;
import com.android.volley.network.Interceptors.HttpHeaderInterceptor;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A network performing Volley requests over an {@link HttpStack}.
 */
public class BasicNetwork implements Network
{
    private static final boolean DEBUG = VolleyLog.DEBUG;

    private static final int SLOW_REQUEST_THRESHOLD_MS = 3000;

    private static final int DEFAULT_POOL_SIZE = 4096;

    private final HttpStack mHttpStack;

    private final HeaderInterceptors mHeaderInterceptors;

    private final ByteArrayPool mPool;

    /**
     * @param httpStack HTTP stack to be used
     */
    public BasicNetwork(HttpStack httpStack)
    {
        // If a pool isn't passed in, then build a small default pool that will give us a lot of
        // benefit and not use too much memory.
        this(httpStack, new ByteArrayPool(DEFAULT_POOL_SIZE));
    }

    /**
     * @param httpStack HTTP stack to be used
     * @param pool      a buffer pool that improves GC performance in copy operations
     */
    public BasicNetwork(HttpStack httpStack,
                        ByteArrayPool pool)
    {
        mHttpStack = httpStack;
        mPool = pool;
        mHeaderInterceptors = new HeaderInterceptors(
                new HttpHeaderInterceptor(),
                new CacheHeaderInterceptor());
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError
    {
        long requestStart = SystemClock.elapsedRealtime();
        while (true)
        {
            HttpResponse httpResponse = null;
            byte[] responseContents = null;
            List<Header> responseHeaders = Collections.emptyList();
            try
            {
                // Gather headers.
                Map<String, String> requestHeaders = mHeaderInterceptors.interceptRequest(request);
                NetworkLog.logRequest(request, requestHeaders);
                httpResponse = mHttpStack.executeRequest(request, requestHeaders);
                int statusCode = httpResponse.getStatusCode();
                responseHeaders = mHeaderInterceptors.interceptResponse(httpResponse, request);
                NetworkLog.logResponse(httpResponse, responseHeaders);
                // Handle cache validation.
                if (statusCode == HttpURLConnection.HTTP_NOT_MODIFIED) // 304
                {
                    Entry entry = request.getCacheEntry();
                    if (entry == null)
                    {
                        return
                                new NetworkResponse(
                                        HttpURLConnection.HTTP_NOT_MODIFIED,
                                        null,
                                        true,
                                        SystemClock.elapsedRealtime() - requestStart,
                                        responseHeaders);
                    }
                    // Combine cached and response headers so the response will be complete.
                    List<Header> combinedHeaders = Headers.combineHeaders(responseHeaders, entry);
                    return
                            new NetworkResponse(
                                    HttpURLConnection.HTTP_NOT_MODIFIED,
                                    entry.data,
                                    true,
                                    SystemClock.elapsedRealtime() - requestStart,
                                    combinedHeaders);
                }

                // Some responses such as 204s do not have content.  We must check.
                InputStream inputStream = httpResponse.getContent();
                if (inputStream != null)
                {
                    responseContents = inputStreamToBytes(inputStream, httpResponse.getContentLength());
                }
                else
                {
                    // Add 0 byte response as a way of honestly representing a
                    // no-content request.
                    responseContents = new byte[0];
                }

                // if the request is slow, log it.
                long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
                logSlowRequests(requestLifetime, request, responseContents, statusCode);

                if (statusCode < 200 || statusCode > 299)
                {
                    throw new IOException();
                }
                return
                        new NetworkResponse(
                                statusCode,
                                responseContents,
                                false,
                                SystemClock.elapsedRealtime() - requestStart,
                                responseHeaders);
            }
            catch (SocketTimeoutException e)
            {
                attemptRetryOnException("socket", request, new TimeoutError());
            }
            catch (MalformedURLException e)
            {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            }
            catch (IOException e)
            {
                int statusCode;
                if (httpResponse != null)
                {
                    statusCode = httpResponse.getStatusCode();
                }
                else
                {
                    if (request.getCachePolicy().responseInError(request, e))
                    {
                        Entry entry = request.getCacheEntry();
                        if (entry != null)
                        {
                            // Combine cached and response headers so the response will be complete.
                            List<Header> combinedHeaders = Headers.combineHeaders(responseHeaders, entry);
                            return
                                    new NetworkResponse(
                                            HttpURLConnection.HTTP_NOT_MODIFIED,
                                            entry.data,
                                            true,
                                            SystemClock.elapsedRealtime() - requestStart,
                                            combinedHeaders);
                        }
                    }

                    throw new NoConnectionError(e);
                }
                VolleyLog.e("Unexpected response code %d for %s", statusCode, request.getUrl());
                NetworkResponse networkResponse;
                if (responseContents != null)
                {
                    networkResponse = new NetworkResponse(
                            statusCode,
                            responseContents,
                            false,
                            SystemClock.elapsedRealtime() - requestStart,
                            responseHeaders);
                    if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED // 401
                            || statusCode == HttpURLConnection.HTTP_FORBIDDEN) //403
                    {
                        attemptRetryOnException("auth", request, new AuthFailureError(networkResponse));
                    }
                    else if (statusCode >= 400 && statusCode <= 499)
                    {
                        // Don't retry other client errors.
                        throw new ClientError(networkResponse);
                    }
                    else if (statusCode >= 500 && statusCode <= 599)
                    {
                        if (request.shouldRetryServerErrors())
                        {
                            attemptRetryOnException("server", request, new ServerError(networkResponse));
                        }
                        else
                        {
                            throw new ServerError(networkResponse);
                        }
                    }
                    else
                    {
                        // 3xx? No reason to retry.
                        throw new ServerError(networkResponse);
                    }
                }
                else
                {
                    attemptRetryOnException("network", request, new NetworkError());
                }
            }
        }
    }

    /**
     * Logs requests that took over SLOW_REQUEST_THRESHOLD_MS to complete.
     */
    private void logSlowRequests(long requestLifetime,
                                 Request<?> request,
                                 byte[] responseContents,
                                 int statusCode)
    {
        if (DEBUG || requestLifetime > SLOW_REQUEST_THRESHOLD_MS)
        {
            VolleyLog.d(
                    "HTTP response for request=<%s> [lifetime=%d], [size=%s], "
                            + "[rc=%d], [retryCount=%s]",
                    request,
                    requestLifetime,
                    responseContents != null ? responseContents.length : "null",
                    statusCode,
                    request.getRetryPolicy().getCurrentRetryCount());
        }
    }

    /**
     * Attempts to prepare the request for a retry. If there are no more attempts remaining in the
     * request's retry policy, a timeout exception is thrown.
     *
     * @param request The request to use.
     */
    private static void attemptRetryOnException(String logPrefix,
                                                Request<?> request,
                                                VolleyError exception) throws VolleyError
    {
        RetryPolicy retryPolicy = request.getRetryPolicy();
        int oldTimeout = request.getTimeoutMs();

        try
        {
            retryPolicy.retry(exception);
        }
        catch (VolleyError e)
        {
            request.addMarker(String.format("%s-timeout-giveup [timeout=%s]", logPrefix, oldTimeout));
            throw e;
        }
        request.addMarker(String.format("%s-retry [timeout=%s]", logPrefix, oldTimeout));
    }

    /**
     * Reads the contents of an InputStream into a byte[].
     */
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private byte[] inputStreamToBytes(InputStream in,
                                      int contentLength) throws IOException, ServerError
    {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool, contentLength);
        byte[] buffer = null;
        try
        {
            if (in == null)
            {
                throw new ServerError();
            }
            buffer = mPool.getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1)
            {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        }
        finally
        {
            try
            {
                // Close the InputStream and release the resources by "consuming the content".
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException e)
            {
                // This can happen if there was an exception above that left the stream in
                // an invalid state.
                VolleyLog.v("Error occurred when closing InputStream");
            }
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }
}
