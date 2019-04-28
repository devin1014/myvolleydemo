package liuwei.android.myvolleydemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.volley.Volley;
import com.android.volley.Volley.Builder;
import com.android.volley.Request;
import com.android.volley.RequestQueue.RequestEventListener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
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
        mVolley = new Builder(this)
                .buildAndRetainInstance();
        mVolley.addRequestEventListener(new RequestEventListener()
        {
            @Override
            public void onRequestEvent(Request<?> request, int event)
            {
                //TODO
            }
        });

        initComponent();
    }

    private void initComponent()
    {
        findViewById(R.id.volley_get).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mVolley.add(new StringRequest(C.URL_BAIDU_M_SEC,
                        new Listener<String>()
                        {
                            @Override
                            public void onResponse(String response)
                            {
                                if (response != null)
                                {
                                }
                            }
                        },
                        new ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                if (error != null)
                                {
                                }
                            }
                        })
                );
            }
        });
    }
}
