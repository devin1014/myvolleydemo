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

package com.android.volley.toolbox;

import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.exception.AuthFailureError;
import com.android.volley.exception.ParseError;
import com.android.volley.exception.VolleyError;
import com.android.volley.network.Headers;
import com.android.volley.network.NetworkResponse;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;


/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class SimpleRequest<T> extends Request<T>
{
    /**
     * Lock to guard mListener as it is cleared on cancel() and read on delivery.
     */
    private final Object mLock = new Object();

    @Nullable
    @GuardedBy("mLock")
    private Listener<T> mListener;

    /**
     * Creates a new GET request.
     *
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public SimpleRequest(String url,
                         @Nullable Listener<T> listener,
                         @Nullable ErrorListener errorListener)
    {
        this(Method.GET, url, listener, errorListener);
    }

    /**
     * Creates a new request with the given method.
     *
     * @param method        the request {@link Method} to use
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public SimpleRequest(int method,
                         String url,
                         @Nullable Listener<T> listener,
                         @Nullable ErrorListener errorListener)
    {
        super(method, url, errorListener);
        mListener = listener;
    }

    @Override
    public void cancel()
    {
        super.cancel();
        synchronized (mLock)
        {
            mListener = null;
        }
    }

    @Override
    protected void deliverResponse(T response)
    {
        Listener<T> listener;
        synchronized (mLock)
        {
            listener = mListener;
        }
        if (listener != null)
        {
            listener.onResponse(response);
        }
    }

    private Map<String, String> mParams;

    public void setParams(Map<String, String> params)
    {
        mParams = params;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError
    {
        return mParams;
    }

    @Override
    @SuppressWarnings("DefaultCharset")
    protected final Response<T> parseNetworkResponse(NetworkResponse response)
    {
        String result;
        try
        {
            result = new String(response.data, Headers.parseCharset(response.headers));
        }
        catch (UnsupportedEncodingException e)
        {
            result = new String(response.data, Charset.defaultCharset());
        }

        try
        {
            return Response.success(getResponseParser().parse(result), Headers.parseCacheHeaders(response));
        }
        catch (ParseError parseError)
        {
            return Response.error(parseError);
        }
        catch (NullPointerException e)
        {
            return Response.error(new VolleyError(e));
        }
    }
}
