package com.android.volley;

import com.android.volley.Request.Method;
import com.android.volley.exception.AuthFailureError;

import java.util.Map;

public interface CachePolicy
{
    boolean shouldCache(Request<?> request);

    boolean responseInError(Request<?> request, Exception e);

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
            return request.getMethod() == Method.GET
                    || request.getMethod() == Method.DEPRECATED_GET_OR_POST;
        }

        @Override
        public int getCacheMaxAge()
        {
            return 0;
        }

        @Override
        public String getCacheKey(Request<?> request)
        {
            String url = request.getUrl();
            // If this is a GET request, just use the URL as the key.
            // For callers using DEPRECATED_GET_OR_POST, we assume the method is GET, which matches
            // legacy behavior where all methods had the same cache key. We can't determine which method
            // will be used because doing so requires calling getPostBody() which is expensive and may
            // throw AuthFailureError.
            int method = request.getMethod();
            if (method == Method.GET
                    || method == Method.DEPRECATED_GET_OR_POST)
            {
                return url;
            }
            return Integer.toString(method) + '-' + url;
        }

        @Override
        public boolean responseInError(Request<?> request, Exception e)
        {
            return false;
        }
    }

    final class ErrorCachePolicy extends DefaultCachePolicy
    {
        @Override
        public boolean responseInError(Request<?> request, Exception e)
        {
            return request.shouldCache() && request.getCacheEntry() != null;
        }
    }

    final class NoCachePolicy implements CachePolicy
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

        @Override
        public boolean responseInError(Request<?> request, Exception e)
        {
            return false;
        }
    }

    final class AllCachePolicy implements CachePolicy
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

        @Override
        public boolean responseInError(Request<?> request, Exception e)
        {
            return false;
        }
    }
}
