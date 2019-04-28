/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley;

import android.content.Context;

import com.android.volley.RequestQueue.RequestEventListener;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.network.BasicNetwork;
import com.android.volley.network.HttpStack;
import com.android.volley.network.ConnHttpStack;
import com.android.volley.network.Network;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import javax.net.ssl.SSLSocketFactory;

public class Volley
{
    /**
     * Default on-disk cache directory.
     */
    private static final String DEFAULT_CACHE_DIR = "volley";
    /**
     * Default http timeout
     */
    public static final int DEFAULT_NETWORK_TIMEOUT_MILLISECOND = 15000;

    private static Volley INSTANCE;

    public static Volley getInstance()
    {
        if (INSTANCE == null)
        {
            throw new IllegalStateException("can not get instance before build Volley.");
        }

        return INSTANCE;
    }

    private final Context mAppContext;

    private final RequestQueue mRequestQueue;

    private Volley(Context context, Builder builder)
    {
        mAppContext = context.getApplicationContext();
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        HttpStack httpStack = builder.httpStack == null
                ? new ConnHttpStack(null, builder.sslSocketFactory) : builder.httpStack;
        Network network = builder.network == null
                ? new BasicNetwork(httpStack) : builder.network;
        RequestQueue queue = new RequestQueue(new DiskCache(cacheDir), network);
        queue.start();
        mRequestQueue = queue;
    }

    private Volley setRetainInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = this;
        }

        return this;
    }

    public void addRequestEventListener(RequestEventListener listener)
    {
        mRequestQueue.addRequestEventListener(listener);
    }

    public void removeRequestEventListener(RequestEventListener listener)
    {
        mRequestQueue.removeRequestEventListener(listener);
    }

    public <T> Request<T> add(Request<T> request)
    {
        return mRequestQueue.add(request);
    }

    public void cancelAll(RequestFilter filter)
    {
        mRequestQueue.cancelAll(filter);
    }

    public void cancelAll(final Object tag)
    {
        mRequestQueue.cancelAll(tag);
    }

    // ---------------------------------------------------------------------------------------------------------
    // - Builder
    // ---------------------------------------------------------------------------------------------------------
    public static class Builder
    {
        private Reference<Context> contextReference;
        private HttpStack httpStack;
        private SSLSocketFactory sslSocketFactory;
        private Network network;

        public Builder(Context context)
        {
            contextReference = new SoftReference<>(context);
        }

        public Builder setHttpStack(HttpStack httpStack)
        {
            this.httpStack = httpStack;
            return this;
        }

        public Builder setSSLSocketFactory(SSLSocketFactory factory)
        {
            this.sslSocketFactory = factory;
            return this;
        }

        public Builder setNetwork(Network network)
        {
            this.network = network;
            return this;
        }

        public Volley build()
        {
            return new Volley(contextReference.get(), this);
        }

        public Volley buildAndRetainInstance()
        {
            return new Volley(contextReference.get(), this).setRetainInstance();
        }
    }
}
