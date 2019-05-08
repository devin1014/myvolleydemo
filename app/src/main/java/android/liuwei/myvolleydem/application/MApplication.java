package android.liuwei.myvolleydem.application;

import android.app.Application;

import java.net.CookieHandler;
import java.net.CookieManager;

public class MApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }
}
