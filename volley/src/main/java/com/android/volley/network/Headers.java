package com.android.volley.network;

import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.VolleyLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

public final class Headers
{
    public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_DATE = "Date";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_PRAGMA = "Pragma";
    public static final String HEADER_NO_CACHE = "no-cache";
    public static final String HEADER_NO_STORE = "no-store";
    public static final String HEADER_MAX_AGE = "max-age=";
    public static final String HEADER_STALE_WHILE_REVALIDATE = "stale-while-revalidate=";
    public static final String HEADER_MUST_REVALIDATE = "must-revalidate";
    public static final String HEADER_PROXY_REVALIDATE = "proxy-revalidate";
    public static final String HEADER_EXPIRES = "Expires";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    public static final String HEADER_ETAG = "ETag";


    public static final String DEFAULT_CONTENT_CHARSET = "UTF-8";
    public static final String RFC1123_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";


    /**
     * Converts Headers[] to Map&lt;String, String&gt;.
     *
     * @deprecated Should never have been exposed in the API. This method may be removed in a future
     * release of Volley.
     */
    @Deprecated
    static Map<String, String> convertHeaders(Header[] headers)
    {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headers.length; i++)
        {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }

    /**
     * Combine cache headers with network response headers for an HTTP 304 response.
     *
     * <p>An HTTP 304 response does not have all header fields. We have to use the header fields
     * from the cache entry plus the new ones from the response. See also:
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5
     *
     * @param responseHeaders Headers from the network response.
     * @param entry           The cached response.
     * @return The combined list of headers.
     */
    static List<Header> combineHeaders(List<Header> responseHeaders, Entry entry)
    {
        // First, create a case-insensitive set of header names from the network
        // response.
        Set<String> headerNamesFromNetworkResponse = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (!responseHeaders.isEmpty())
        {
            for (Header header : responseHeaders)
            {
                headerNamesFromNetworkResponse.add(header.getName());
            }
        }

        // Second, add headers from the cache entry to the network response as long as
        // they didn't appear in the network response, which should take precedence.
        List<Header> combinedHeaders = new ArrayList<>(responseHeaders);
        if (entry.allResponseHeaders != null)
        {
            if (!entry.allResponseHeaders.isEmpty())
            {
                for (Header header : entry.allResponseHeaders)
                {
                    if (!headerNamesFromNetworkResponse.contains(header.getName()))
                    {
                        combinedHeaders.add(header);
                    }
                }
            }
        }
        else
        {
            // Legacy caches only have entry.responseHeaders.
            if (!entry.responseHeaders.isEmpty())
            {
                for (Map.Entry<String, String> header : entry.responseHeaders.entrySet())
                {
                    if (!headerNamesFromNetworkResponse.contains(header.getKey()))
                    {
                        combinedHeaders.add(new Header(header.getKey(), header.getValue()));
                    }
                }
            }
        }
        return combinedHeaders;
    }

    public static Map<String, String> toHeaderMap(List<Header> allHeaders)
    {
        if (allHeaders == null)
        {
            return null;
        }
        if (allHeaders.isEmpty())
        {
            return Collections.emptyMap();
        }
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        // Later elements in the list take precedence.
        for (Header header : allHeaders)
        {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

    public static List<Header> toAllHeaderList(Map<String, String> headers)
    {
        if (headers == null)
        {
            return null;
        }
        if (headers.isEmpty())
        {
            return Collections.emptyList();
        }
        List<Header> allHeaders = new ArrayList<>(headers.size());
        for (Map.Entry<String, String> header : headers.entrySet())
        {
            allHeaders.add(new Header(header.getKey(), header.getValue()));
        }
        return allHeaders;
    }

    /**
     * Extracts a {@link com.android.volley.Cache.Entry} from a {@link NetworkResponse}.
     *
     * @param response The network response to parse headers from
     * @return a cache entry for the given response, or null if the response is not cacheable.
     */
    public static Cache.Entry parseCacheHeaders(NetworkResponse response)
    {
        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;

        long serverDate = 0;
        long lastModified = 0;
        long serverExpires = 0;
        long softExpire = 0;
        long finalExpire = 0;
        long maxAge = 0;
        long staleWhileRevalidate = 0;
        boolean hasCacheControl = false;
        boolean mustRevalidate = false;

        String serverEtag;
        String headerValue;

        headerValue = headers.get(HEADER_DATE);
        if (headerValue != null)
        {
            serverDate = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get(HEADER_CACHE_CONTROL);
        if (headerValue != null)
        {
            hasCacheControl = true;
            String[] tokens = headerValue.split(",", 0);
            for (int i = 0; i < tokens.length; i++)
            {
                String token = tokens[i].trim();
                if (token.equals(HEADER_NO_CACHE) || token.equals(HEADER_NO_STORE))
                {
                    return null;
                }
                else if (token.startsWith(HEADER_MAX_AGE))
                {
                    maxAge = parseLong(token.substring(8));
                }
                else if (token.startsWith(HEADER_STALE_WHILE_REVALIDATE))
                {
                    staleWhileRevalidate = parseLong(token.substring(23));
                }
                else if (token.equals(HEADER_MUST_REVALIDATE) || token.equals(HEADER_PROXY_REVALIDATE))
                {
                    mustRevalidate = true;
                }
            }
        }

        headerValue = headers.get(HEADER_EXPIRES);
        if (headerValue != null)
        {
            serverExpires = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get(HEADER_LAST_MODIFIED);
        if (headerValue != null)
        {
            lastModified = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get(HEADER_ETAG);

        // Cache-Control takes precedence over an Expires header, even if both exist and Expires
        // is more restrictive.
        if (hasCacheControl)
        {
            softExpire = now + maxAge * 1000;
            finalExpire = mustRevalidate ? softExpire : softExpire + staleWhileRevalidate * 1000;
        }
        else if (serverDate > 0 && serverExpires >= serverDate)
        {
            // Default semantic for Expire header in HTTP specification is softExpire.
            softExpire = now + (serverExpires - serverDate);
            finalExpire = softExpire;
        }

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = finalExpire;
        entry.serverDate = serverDate;
        entry.lastModified = lastModified;
        entry.responseHeaders = headers;
        entry.allResponseHeaders = response.allHeaders;

        return entry;
    }

    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    public static long parseDateAsEpoch(String dateStr)
    {
        try
        {
            // Parse date in RFC1123 format if this header contains one
            return newRfc1123Formatter().parse(dateStr).getTime();
        }
        catch (ParseException e)
        {
            // Date in invalid format, fallback to 0
            VolleyLog.e(e, "Unable to parse dateStr: %s, falling back to 0", dateStr);
            return 0;
        }
    }

    private static long parseLong(String value)
    {
        try
        {
            return Long.parseLong(value);
        }
        catch (Exception ignored)
        {
            return 0L;
        }
    }

    /**
     * Format an epoch date in RFC1123 format.
     */
    public static String formatEpochAsRfc1123(long epoch)
    {
        return newRfc1123Formatter().format(new Date(epoch));
    }

    private static SimpleDateFormat newRfc1123Formatter()
    {
        SimpleDateFormat formatter = new SimpleDateFormat(RFC1123_FORMAT, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter;
    }

    /**
     * Retrieve a charset from headers
     *
     * @param headers        An {@link java.util.Map} of headers
     * @param defaultCharset Charset to return if none can be found
     * @return Returns the charset specified in the Content-Type of this header, or the
     * defaultCharset if none can be found.
     */
    public static String parseCharset(Map<String, String> headers, String defaultCharset)
    {
        String contentType = headers.get(HEADER_CONTENT_TYPE);
        if (contentType != null)
        {
            String[] params = contentType.split(";", 0);
            for (int i = 1; i < params.length; i++)
            {
                String[] pair = params[i].trim().split("=", 0);
                if (pair.length == 2)
                {
                    if (pair[0].equals("charset"))
                    {
                        return pair[1];
                    }
                }
            }
        }

        return defaultCharset;
    }

    /**
     * Returns the charset specified in the Content-Type of this header, or the HTTP default
     * (ISO-8859-1) if none can be found.
     */
    public static String parseCharset(Map<String, String> headers)
    {
        return parseCharset(headers, DEFAULT_CONTENT_CHARSET);
    }

}
