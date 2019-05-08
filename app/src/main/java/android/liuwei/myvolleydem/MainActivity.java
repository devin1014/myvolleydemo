package android.liuwei.myvolleydem;

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
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.Volley;
import com.android.volley.Volley.Builder;
import com.android.volley.VolleyLog;
import com.android.volley.exception.VolleyError;
import com.android.volley.toolbox.StringRequest;

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
                mVolley.add(
                        new StringRequest(
                                CONSTANT.URL_GET,
                                mStringListener,
                                mErrorListener)
                );
            }
        });

        findViewById(R.id.volley_post).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mVolley.add(
                        new PathRequest(
                                CONSTANT.URL_POST,
                                mStringListener,
                                mErrorListener)
                );
            }
        });
    }

    private Listener<String> mStringListener = new Listener<String>()
    {
        @Override
        public void onResponse(String response)
        {
            Log.i("Volley", response);
            mResponseTxt.setText(response);
        }
    };

    private ErrorListener mErrorListener = new ErrorListener()
    {
        @Override
        public void onErrorResponse(VolleyError error)
        {
            Log.i("Volley", error.toString());
            mResponseTxt.setText(error.toString());
        }
    };
}
