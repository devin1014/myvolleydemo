package liuwei.android.myvolleydemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.android.volley.CachePolicy.AllCachePolicy;
import com.android.volley.CachePolicy.DefaultCachePolicy;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VolleyLog.DEBUG = true;
        mVolley = new Builder(this).buildAndRetainInstance();

        initComponent();
    }

    private void initComponent()
    {
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
                                C.URL_GET,
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
                        new StringRequest(
                                C.URL_GET,
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
        }
    };

    private ErrorListener mErrorListener = new ErrorListener()
    {
        @Override
        public void onErrorResponse(VolleyError error)
        {
            Log.i("Volley", error.toString());
        }
    };
}
