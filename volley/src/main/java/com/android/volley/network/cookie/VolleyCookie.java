package com.android.volley.network.cookie;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface VolleyCookie
{
    VolleyCookie NO_COOKIES = new VolleyCookie()
    {
        @Override
        public void saveFromResponse(URI url, Map<String, List<String>> cookies)
        {
        }

        @Override
        public Map<String, List<String>> loadForRequest(URL url)
        {
            return Collections.emptyMap();
        }
    };

    void saveFromResponse(URI url, Map<String, List<String>> cookies);

    Map<String, List<String>> loadForRequest(URL url);
}
