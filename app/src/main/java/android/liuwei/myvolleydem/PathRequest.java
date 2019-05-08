package android.liuwei.myvolleydem;

import android.support.annotation.Nullable;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.exception.AuthFailureError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class PathRequest extends StringRequest
{
    public PathRequest(String url,
                       @Nullable Listener<String> listener,
                       @Nullable ErrorListener errorListener)
    {
        super(Method.POST, url, listener, errorListener);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError
    {
        Map<String, String> params = new HashMap<>();
        params.put("deviceType", "6");
        params.put("version", "9.0329");
        params.put("path", "37599");
        params.put("device", "SM-N9005");
        params.put("hash", "1556533609900.c1a179553319377769be3a311745b253");
        params.put("type", "fvod");
        params.put("isFlex", "true");
        params.put("format", "json");
        return params;
    }
}
