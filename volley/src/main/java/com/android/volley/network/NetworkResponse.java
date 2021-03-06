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

import android.support.annotation.NonNull;

import com.android.volley.Request;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Data and headers returned from {@link Network#performRequest(Request)}.
 */
public class NetworkResponse
{
    /**
     * The HTTP status code.
     */
    public final int statusCode;

    /**
     * Raw data from this response.
     */
    public final byte[] data;

    /**
     * Response headers.
     *
     * <p>This map is case-insensitive. It should not be mutated directly.
     *
     * <p>Note that if the server returns two headers with the same (case-insensitive) name, this
     * map will only contain the last one. Use {@link #allHeaders} to inspect all headers returned
     * by the server.
     */
    public final Map<String, String> headers;

    /**
     * All response headers. Must not be mutated directly.
     */
    public final List<Header> allHeaders;

    /**
     * True if the server returned a 304 (Not Modified).
     */
    public final boolean notModified;

    /**
     * Network round trip time in milliseconds.
     */
    public final long networkTimeMs;

    /**
     * Creates a new network response for an OK response with no headers.
     *
     * @param data Response body
     */
    public NetworkResponse(byte[] data)
    {
        this(HttpURLConnection.HTTP_OK, data, false, 0, Collections.<Header>emptyList());
    }

    /**
     * Creates a new network response for an OK response.
     *
     * @param data    Response body
     * @param headers Headers returned with this response, or null for none
     * @deprecated see {@link #NetworkResponse(int, byte[], boolean, long, List)}. This constructor
     * cannot handle server responses containing multiple headers with the same name. This
     * constructor may be removed in a future release of Volley.
     */
    @Deprecated
    public NetworkResponse(byte[] data,
                           Map<String, String> headers)
    {
        this(HttpURLConnection.HTTP_OK, data, headers, false, 0);
    }

    /**
     * Creates a new network response.
     *
     * @param statusCode  the HTTP status code
     * @param data        Response body
     * @param headers     Headers returned with this response, or null for none
     * @param notModified True if the server returned a 304 and the data was already in cache
     * @deprecated see {@link #NetworkResponse(int, byte[], boolean, long, List)}. This constructor
     * cannot handle server responses containing multiple headers with the same name. This
     * constructor may be removed in a future release of Volley.
     */
    @Deprecated
    public NetworkResponse(int statusCode,
                           byte[] data,
                           Map<String, String> headers,
                           boolean notModified)
    {
        this(statusCode, data, headers, notModified, 0);
    }

    /**
     * Creates a new network response.
     *
     * @param statusCode    the HTTP status code
     * @param data          Response body
     * @param headers       Headers returned with this response, or null for none
     * @param notModified   True if the server returned a 304 and the data was already in cache
     * @param networkTimeMs Round-trip network time to receive network response
     * @deprecated see {@link #NetworkResponse(int, byte[], boolean, long, List)}. This constructor
     * cannot handle server responses containing multiple headers with the same name. This
     * constructor may be removed in a future release of Volley.
     */
    @Deprecated
    public NetworkResponse(int statusCode,
                           byte[] data,
                           Map<String, String> headers,
                           boolean notModified,
                           long networkTimeMs)
    {
        this(statusCode, data, headers, Headers.toAllHeaderList(headers), notModified, networkTimeMs);
    }

    /**
     * Creates a new network response.
     *
     * @param statusCode    the HTTP status code
     * @param data          Response body
     * @param notModified   True if the server returned a 304 and the data was already in cache
     * @param networkTimeMs Round-trip network time to receive network response
     * @param allHeaders    All headers returned with this response, or null for none
     */
    public NetworkResponse(int statusCode,
                           byte[] data,
                           boolean notModified,
                           long networkTimeMs,
                           List<Header> allHeaders)
    {
        this(statusCode, data, Headers.toHeaderMap(allHeaders), allHeaders, notModified, networkTimeMs);
    }

    private NetworkResponse(int statusCode,
                            byte[] data,
                            Map<String, String> headers,
                            List<Header> allHeaders,
                            boolean notModified,
                            long networkTimeMs)
    {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
        this.allHeaders = allHeaders == null ? null : Collections.unmodifiableList(allHeaders);
        this.notModified = notModified;
        this.networkTimeMs = networkTimeMs;
    }

    @NonNull
    @Override
    public String toString()
    {
        return "{"
                + "code=" + statusCode
                + ",contentLength=" + data.length
                + ",headers=" + (allHeaders != null ? allHeaders.toString() : "null")
                + "}";
    }
}
