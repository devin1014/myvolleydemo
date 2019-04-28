package com.android.volley.network;

import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.exception.AuthFailureError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpStack implements HttpStack
{
    private final OkHttpClient mHttpClient;

    public OkHttpStack(OkHttpClient httpClient)
    {
        mHttpClient = httpClient;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request,
                                       Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError
    {
        Builder builder = new okhttp3.Request.Builder();
        builder.url(request.getUrl());
        Map<String, String> headers = request.getHeaders();
        for (String name : headers.keySet())
        {
            builder.addHeader(name, headers.get(name));
        }
        for (final String name : additionalHeaders.keySet())
        {
            builder.header(name, additionalHeaders.get(name));
        }

        setConnectionParametersForRequest(builder, request);

        Call call = mHttpClient.newCall(builder.build());
        Response response = call.execute();
        ResponseBody body = response.body();

        if (body != null)
        {
            return new HttpResponse(response.code(), convertHeaders(response.headers()), (int) body.contentLength(), body.byteStream());
        }
        else
        {
            return new HttpResponse(response.code(), convertHeaders(response.headers()));
        }
    }

    // VisibleForTesting
    private static List<Header> convertHeaders(okhttp3.Headers headers)
    {
        List<Header> headerList = new ArrayList<>(headers.size());

        for (String name : headers.names())
        {
            if (!TextUtils.isEmpty(name))
            {
                for (String value : headers.values(name))
                {
                    headerList.add(new Header(name, value));
                }
            }
        }

        return headerList;
    }

    @SuppressWarnings("deprecation")
    private static void setConnectionParametersForRequest(Builder builder,
                                                          com.android.volley.Request<?> request)
            throws IOException, AuthFailureError
    {
        switch (request.getMethod())
        {
            case Request.Method.DEPRECATED_GET_OR_POST:
                // Ensure backwards compatibility.  Volley assumes a request with a null body is a GET.
                byte[] postBody = request.getPostBody();
                if (postBody != null)
                {
                    builder.post(RequestBody.create(MediaType.parse(request.getPostBodyContentType()), postBody));
                }
                break;
            case Request.Method.GET:
                builder.get();
                break;
            case Request.Method.DELETE:
                builder.delete();
                break;
            case Request.Method.POST:
                builder.post(createRequestBody(request));
                break;
            case Request.Method.PUT:
                builder.put(createRequestBody(request));
                break;
            case Request.Method.HEAD:
                builder.head();
                break;
            case Request.Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;
            case Request.Method.TRACE:
                builder.method("TRACE", null);
                break;
            case Request.Method.PATCH:
                builder.patch(createRequestBody(request));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static RequestBody createRequestBody(Request r) throws AuthFailureError
    {
        byte[] body = r.getBody();
        if (body == null)
        {
            body = new byte[0];
        }

        return RequestBody.create(MediaType.parse(r.getBodyContentType()), body);
    }
}
