package com.android.volley.network;

import android.support.annotation.NonNull;

import com.android.volley.Cache;
import com.android.volley.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Interceptors
{
    class HeaderInterceptors
    {
        private List<HeaderInterceptor> mInterceptors;

        HeaderInterceptors(HeaderInterceptor... interceptors)
        {
            mInterceptors = Arrays.asList(interceptors);
        }

        Map<String, String> interceptRequest(@NonNull Request<?> request)
        {
            Map<String, String> headers = new HashMap<>();

            for (HeaderInterceptor interceptor : mInterceptors)
            {
                interceptor.interceptRequest(headers, request);
            }

            return headers;
        }

        List<Header> interceptResponse(@NonNull HttpResponse response,
                                       @NonNull Request<?> request)
        {
            List<Header> headers = new ArrayList<>();

            for (HeaderInterceptor interceptor : mInterceptors)
            {
                interceptor.interceptResponse(headers, response, request);
            }

            return headers;
        }
    }

    // -----------------------------------------------------------------------
    // - Header Interceptor
    // -----------------------------------------------------------------------
    interface HeaderInterceptor
    {
        void interceptRequest(@NonNull Map<String, String> headers,
                              @NonNull Request<?> request);

        void interceptResponse(@NonNull List<Header> headers,
                               @NonNull HttpResponse response,
                               @NonNull Request<?> request);
    }

    // -----------------------------------------------------------------------
    // - Impl
    // -----------------------------------------------------------------------
    class HttpHeaderInterceptor implements HeaderInterceptor
    {
        @Override
        public void interceptRequest(@NonNull Map<String, String> headers,
                                     @NonNull Request<?> request)
        {
            Map<String, String> cacheHeader = getCacheHeaders(request.getCacheEntry());

            headers.putAll(cacheHeader);

        }

        @Override
        public void interceptResponse(@NonNull List<Header> headers,
                                      @NonNull HttpResponse response,
                                      @NonNull Request<?> request)
        {
            headers.addAll(response.getHeaders());

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
    // - Impl
    // -----------------------------------------------------------------------
    class CacheHeaderInterceptor implements HeaderInterceptor
    {
        private final String ETAG = "CacheTag-" + Integer.toHexString(getClass().getName().hashCode());

        CacheHeaderInterceptor()
        {
        }

        @Override
        public void interceptRequest(@NonNull Map<String, String> headers,
                                     @NonNull Request<?> request)
        {
        }

        @Override
        public void interceptResponse(@NonNull List<Header> headers,
                                      @NonNull HttpResponse response,
                                      @NonNull Request<?> request)
        {
            if (request.shouldCache())
            {
                // handle 'Cache-Control' header
                Header cacheControl = findHeader(headers, Headers.HEADER_CACHE_CONTROL);
                if (cacheControl != null)
                {
                    String[] tokens = cacheControl.getValue().split(",", 0);
                    for (String t : tokens)
                    {
                        String token = t.trim();
                        if (token.equals(Headers.HEADER_NO_CACHE) || token.equals(Headers.HEADER_NO_STORE))
                        {
                            // add new 'Cache-Control' header
                            headers.remove(cacheControl);
                            headers.add(
                                    new Header(Headers.HEADER_CACHE_CONTROL,
                                            Headers.HEADER_MAX_AGE + request.getCachePolicy().getCacheMaxAge()));
                            removeHeader(headers, Headers.HEADER_PRAGMA);
                            removeHeader(headers, Headers.HEADER_EXPIRES);
                            break;
                        }
                    }
                }

                Header eTag = findHeader(headers, Headers.HEADER_ETAG);
                if (eTag == null)
                {
                    eTag = new Header(Headers.HEADER_ETAG, ETAG);
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

        private void removeHeader(List<Header> headers, String name)
        {
            for (Header h : headers)
            {
                if (h.getName().equalsIgnoreCase(name))
                {
                    headers.remove(h);
                    break;
                }
            }
        }
    }
}
