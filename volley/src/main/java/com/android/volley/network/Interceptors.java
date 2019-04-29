package com.android.volley.network;

import android.support.annotation.NonNull;

import com.android.volley.Cache;
import com.android.volley.Request;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Interceptors
{
    // -----------------------------------------------------------------------
    // - Header Interceptor
    // -----------------------------------------------------------------------
    interface HeaderInterceptor
    {
        boolean enable();

        @NonNull
        Map<String, String> interceptRequest(@NonNull Map<String, String> headers,
                                             @NonNull Request<?> request);

        @NonNull
        List<Header> interceptResponse(@NonNull List<Header> headers,
                                       @NonNull HttpResponse response,
                                       @NonNull Request<?> request);
    }

    // -----------------------------------------------------------------------
    // - Impl
    // -----------------------------------------------------------------------
    class HeaderInterceptorImp implements HeaderInterceptor
    {
        boolean enable = true;

        void setEnable(boolean enable)
        {
            this.enable = enable;
        }

        @Override
        public boolean enable()
        {
            return enable;
        }

        @NonNull
        @Override
        public Map<String, String> interceptRequest(@NonNull Map<String, String> headers,
                                                    @NonNull Request<?> request)
        {
            Map<String, String> cacheHeader = getCacheHeaders(request.getCacheEntry());

            headers.putAll(cacheHeader);

            return headers;
        }

        @NonNull
        @Override
        public List<Header> interceptResponse(@NonNull List<Header> headers,
                                              @NonNull HttpResponse response,
                                              @NonNull Request<?> request)
        {
            headers.addAll(response.getHeaders());

            return headers;
        }

        private Map<String, String> getCacheHeaders(Cache.Entry entry)
        {
            // If there's no cache entry, we're done.
            if (entry == null)
            {
                return Collections.emptyMap();
            }

            Map<String, String> headers = new HashMap<>();

            if (entry.etag != null)
            {
                headers.put(Headers.HEADER_IF_NONE_MATCH, entry.etag);
            }

            if (entry.lastModified > 0)
            {
                headers.put(Headers.HEADER_IF_MODIFIED_SINCE, Headers.formatEpochAsRfc1123(entry.lastModified));
            }

            return headers;
        }
    }

    // -----------------------------------------------------------------------
    // - Cache Interceptor
    // -----------------------------------------------------------------------
    interface CacheInterceptor extends HeaderInterceptor
    {
    }

    // -----------------------------------------------------------------------
    // - Impl
    // -----------------------------------------------------------------------
    class CacheInterceptorImp implements CacheInterceptor
    {
        CacheInterceptorImp()
        {
        }

        @Override
        public boolean enable()
        {
            return false;
        }

        @NonNull
        @Override
        public Map<String, String> interceptRequest(@NonNull Map<String, String> headers,
                                                    @NonNull Request<?> request)
        {
            return headers;
        }

        @NonNull
        @Override
        public List<Header> interceptResponse(@NonNull List<Header> headers,
                                              @NonNull HttpResponse response,
                                              @NonNull Request<?> request)
        {
            if (request.shouldCache())
            {
                Header cacheControlHeader = findHeader(headers, Headers.HEADER_CACHE_CONTROL);
                if (cacheControlHeader != null)
                {
                    String[] tokens = cacheControlHeader.getName().split(",", 0);
                    for (String t : tokens)
                    {
                        String token = t.trim();
                        if (token.equals(Headers.HEADER_NO_CACHE) || token.equals(Headers.HEADER_NO_STORE))
                        {
                            headers.remove(cacheControlHeader);
                            Header newCacheControlHeader = new Header(Headers.HEADER_CACHE_CONTROL,
                                    Headers.HEADER_MAX_AGE + request.getCachePolicy().getCacheMaxAge());
                            headers.add(newCacheControlHeader);
                            break;
                        }
                    }
                }

                Header eTag = findHeader(headers, Headers.HEADER_ETAG);
                if (eTag == null)
                {
                    eTag = new Header(Headers.HEADER_ETAG, Integer.toHexString(String.valueOf(System.currentTimeMillis()).hashCode()));
                    headers.add(eTag);
                }

                Header lastModified = findHeader(headers, Headers.HEADER_LAST_MODIFIED);
                if (lastModified == null)
                {
                    lastModified = new Header(Headers.HEADER_LAST_MODIFIED, Headers.formatEpochAsRfc1123(new Date().getTime()));
                    headers.add(lastModified);
                }
            }
            else
            {
                Header cacheControlHeader = findHeader(headers, Headers.HEADER_CACHE_CONTROL);
                if (cacheControlHeader != null)
                {
                    boolean hasNoCache = false;
                    String[] tokens = cacheControlHeader.getName().split(",", 0);
                    for (String t : tokens)
                    {
                        String token = t.trim();
                        if (token.equals(Headers.HEADER_NO_CACHE) || token.equals(Headers.HEADER_NO_STORE))
                        {
                            hasNoCache = true;
                            break;
                        }
                    }
                    if (!hasNoCache)
                    {
                        headers.remove(cacheControlHeader);
                        headers.add(new Header(Headers.HEADER_CACHE_CONTROL, Headers.HEADER_NO_CACHE));
                    }
                }
                else
                {
                    headers.add(new Header(Headers.HEADER_CACHE_CONTROL, Headers.HEADER_NO_CACHE));
                }
            }

            return headers;
        }

        private Header findHeader(List<Header> headers, String name)
        {
            for (Header h : headers)
            {
                if (h.getName().equalsIgnoreCase(name))
                {
                    return h;
                }
            }

            return null;
        }
    }
}
