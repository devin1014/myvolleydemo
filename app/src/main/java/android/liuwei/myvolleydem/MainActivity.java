package android.liuwei.myvolleydem;

import android.liuwei.myvolleydem.request.PathRequest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.android.volley.CachePolicy.AllCachePolicy;
import com.android.volley.CachePolicy.DefaultCachePolicy;
import com.android.volley.CachePolicy.ErrorCachePolicy;
import com.android.volley.CachePolicy.NoCachePolicy;
import com.android.volley.Request;
import com.android.volley.Request.ResponseParser;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.Volley;
import com.android.volley.Volley.Builder;
import com.android.volley.VolleyLog;
import com.android.volley.exception.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
{
    private Volley mVolley;
    private TextView mResponseTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VolleyLog.DEBUG = true;
        mVolley = new Builder(this).buildAndRetainInstance();

        initComponent();

        VolleyLog.d("cache file = %s/volley", getCacheDir().getAbsolutePath());
    }

    private void initComponent()
    {
        mResponseTxt = findViewById(R.id.response_content);

        ((RadioGroup) findViewById(R.id.cache_type))
                .setOnCheckedChangeListener(new OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId)
                    {
                        switch (checkedId)
                        {
                            case R.id.standard_cache:
                                mVolley.setCachePolicy(new DefaultCachePolicy());
                                break;
                            case R.id.no_cache:
                                mVolley.setCachePolicy(new NoCachePolicy());
                                break;
                            case R.id.all_cache:
                                mVolley.setCachePolicy(new AllCachePolicy());
                                break;
                            case R.id.error_cache:
                                mVolley.setCachePolicy(new ErrorCachePolicy());
                                break;
                        }
                    }
                });

        findViewById(R.id.volley_get).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //executeGetString();
                executeGetChannel();
            }
        });

        findViewById(R.id.volley_post).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //executePostString();
                executeAccessToken();
            }
        });
    }

    private void resetResponse(String response)
    {
        Log.i("Volley", response);
        mResponseTxt.setText(response);
    }

    private void resetVolleyError(VolleyError error)
    {
        Log.i("Volley", error.toString());
        mResponseTxt.setText(error.toString());
    }

    private void executeGetString()
    {
        mVolley.add(
                new StringRequest(CONSTANT.URL_GET,
                        new Listener<String>()
                        {
                            @Override
                            public void onResponse(String response)
                            {
                                resetResponse(response);
                            }
                        },
                        new ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                resetVolleyError(error);
                            }
                        })
        );
    }

    private void executePostString()
    {
        mVolley.add(
                new PathRequest(
                        CONSTANT.URL_POST,
                        new Listener<String>()
                        {
                            @Override
                            public void onResponse(String response)
                            {
                                resetResponse(response);
                            }
                        },
                        new ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                resetVolleyError(error);
                            }
                        })
        );
    }

    private void executeAccessToken()
    {
        new Request.Builder()
                .post()
                .url(CONSTANT.URL_ACCESS_TOKEN)
                .addParam("format", "json")
                .setListener(new Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        resetResponse(response.toString());
                    }
                })
                .setErrorListener(new ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        resetVolleyError(error);
                    }
                })
                .setResponseParser(new ResponseParser<JSONObject>()
                {
                    @Override
                    public JSONObject parse(String data)
                    {
                        try
                        {
                            return new JSONObject(data);
                        }
                        catch (JSONException e)
                        {
                            return null;
                        }
                    }
                })
                .execute(mVolley);
    }

    private void executeGetChannel()
    {
        new Request.Builder()
                .get()
                .url(CONSTANT.URL_CHANNEL)
                .addParam("format", "json")
                .setListener(new Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        resetResponse(response);
                    }
                })
                .setErrorListener(new ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        resetVolleyError(error);
                    }
                })
                .setResponseParser(new ResponseParser<String>()
                {
                    @Override
                    public String parse(String data)
                    {
                        return data;
                    }
                })
                .execute(mVolley);
    }
}
