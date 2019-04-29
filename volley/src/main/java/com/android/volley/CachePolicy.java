package com.android.volley;

import com.android.volley.Request.Method;
import com.android.volley.exception.AuthFailureError;

import java.util.Map;

public interface CachePolicy
{
    boolean shouldCache(Request<?> request);

    int getCacheMaxAge();

    String getCacheKey(Request<?> request);

    // -----------------------------------------------------------------
    // - Default Policy
    // -----------------------------------------------------------------
    class DefaultCachePolicy implements CachePolicy
    {
        @Override
        public boolean shouldCache(Request<?> request)
        {
            return request.getMethod() == Method.GET || request.getMethod() == Method.DEPRECATED_GET_OR_POST;
        }

        @Override
        public int getCacheMaxAge()
        {
            return Integer.MAX_VALUE;
        }

        @Override
        public String getCacheKey(Request<?> request)
        {
            return request.getCacheKey();
        }
    }

    class NoCachePolicy implements CachePolicy
    {
        @Override
        public boolean shouldCache(Request<?> request)
        {
            return false;
        }

        @Override
        public int getCacheMaxAge()
        {
            return 0;
        }

        @Override
        public String getCacheKey(Request<?> request)
        {
            return "";
        }
    }

    class AllCachePolicy implements CachePolicy
    {
        @Override
        public boolean shouldCache(Request<?> request)
        {
            return true;
        }

        @Override
        public int getCacheMaxAge()
        {
            return 24 * 60 * 60; // 24hour
        }

        @Override
        public String getCacheKey(Request<?> request)
        {
            StringBuilder builder = new StringBuilder(request.getUrl());
            try
            {
                Map<String, String> params = request.getParams();
                if (params != null && params.size() > 0)
                {
                    builder.append("?");
                    for (Map.Entry<String, String> entry : params.entrySet())
                    {
                        if (builder.length() == request.getUrl().length())
                        {
                            builder.append("?");
                        }
                        else
                        {
                            builder.append("&");
                        }
                        builder
                                .append(entry.getKey())
                                .append("=")
                                .append(entry.getValue());
                    }
                }
            }
            catch (AuthFailureError authFailureError)
            {
                authFailureError.printStackTrace();
            }
            return builder.toString();
        }
    }
}
