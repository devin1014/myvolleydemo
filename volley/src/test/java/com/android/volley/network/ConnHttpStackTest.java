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

package com.android.volley.network;

import com.android.volley.Request.Method;
import com.android.volley.mock.TestRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ConnHttpStackTest
{
    @Mock
    private HttpURLConnection mMockConnection;
    private ConnHttpStack mConnHttpStack;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        when(mMockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        mConnHttpStack = new ConnHttpStack()
        {
            @Override
            protected HttpURLConnection createConnection(URL url)
            {
                return mMockConnection;
            }
        };
    }

    @Test
    public void connectionForDeprecatedGetRequest() throws Exception
    {
        TestRequest.DeprecatedGet request = new TestRequest.DeprecatedGet();
        //        assertEquals(request.getMethod(), Method.DEPRECATED_GET_OR_POST);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection, never()).setRequestMethod(anyString());
        verify(mMockConnection, never()).setDoOutput(true);
    }

    @Test
    public void connectionForDeprecatedPostRequest() throws Exception
    {
        TestRequest.DeprecatedPost request = new TestRequest.DeprecatedPost();
        //        assertEquals(request.getMethod(), Method.DEPRECATED_GET_OR_POST);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("POST");
        verify(mMockConnection).setDoOutput(true);
    }

    @Test
    public void connectionForGetRequest() throws Exception
    {
        TestRequest.Get request = new TestRequest.Get();
        assertEquals(request.getMethod(), Method.GET);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("GET");
        verify(mMockConnection, never()).setDoOutput(true);
    }

    @Test
    public void connectionForPostRequest() throws Exception
    {
        TestRequest.Post request = new TestRequest.Post();
        assertEquals(request.getMethod(), Method.POST);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("POST");
        verify(mMockConnection, never()).setDoOutput(true);
    }

    @Test
    public void connectionForPostWithBodyRequest() throws Exception
    {
        TestRequest.PostWithBody request = new TestRequest.PostWithBody();
        assertEquals(request.getMethod(), Method.POST);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("POST");
        verify(mMockConnection).setDoOutput(true);
    }

    @Test
    public void connectionForPutRequest() throws Exception
    {
        TestRequest.Put request = new TestRequest.Put();
        assertEquals(request.getMethod(), Method.PUT);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("PUT");
        verify(mMockConnection, never()).setDoOutput(true);
    }

    @Test
    public void connectionForPutWithBodyRequest() throws Exception
    {
        TestRequest.PutWithBody request = new TestRequest.PutWithBody();
        assertEquals(request.getMethod(), Method.PUT);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("PUT");
        verify(mMockConnection).setDoOutput(true);
    }

    @Test
    public void connectionForDeleteRequest() throws Exception
    {
        TestRequest.Delete request = new TestRequest.Delete();
        assertEquals(request.getMethod(), Method.DELETE);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("DELETE");
        verify(mMockConnection, never()).setDoOutput(true);
    }

    @Test
    public void connectionForHeadRequest() throws Exception
    {
        TestRequest.Head request = new TestRequest.Head();
        assertEquals(request.getMethod(), Method.HEAD);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("HEAD");
        verify(mMockConnection, never()).setDoOutput(true);
    }

    @Test
    public void connectionForOptionsRequest() throws Exception
    {
        TestRequest.Options request = new TestRequest.Options();
        assertEquals(request.getMethod(), Method.OPTIONS);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("OPTIONS");
        verify(mMockConnection, never()).setDoOutput(true);
    }

    @Test
    public void connectionForTraceRequest() throws Exception
    {
        TestRequest.Trace request = new TestRequest.Trace();
        assertEquals(request.getMethod(), Method.TRACE);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("TRACE");
        verify(mMockConnection, never()).setDoOutput(true);
    }

    @Test
    public void connectionForPatchRequest() throws Exception
    {
        TestRequest.Patch request = new TestRequest.Patch();
        assertEquals(request.getMethod(), Method.PATCH);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("PATCH");
        verify(mMockConnection, never()).setDoOutput(true);
    }

    @Test
    public void connectionForPatchWithBodyRequest() throws Exception
    {
        TestRequest.PatchWithBody request = new TestRequest.PatchWithBody();
        assertEquals(request.getMethod(), Method.PATCH);

        ConnHttpStack.setConnectionParametersForRequest(mMockConnection, request);
        verify(mMockConnection).setRequestMethod("PATCH");
        verify(mMockConnection).setDoOutput(true);
    }

    @Test
    public void executeRequestClosesConnection_connectionError() throws Exception
    {
        when(mMockConnection.getResponseCode()).thenThrow(new SocketTimeoutException());
        try
        {
            mConnHttpStack.executeRequest(
                    new TestRequest.Get(), Collections.<String, String>emptyMap());
            fail("Should have thrown exception");
        }
        catch (IOException e)
        {
            verify(mMockConnection).disconnect();
        }
    }

    @Test
    public void executeRequestClosesConnection_invalidResponseCode() throws Exception
    {
        when(mMockConnection.getResponseCode()).thenReturn(-1);
        try
        {
            mConnHttpStack.executeRequest(
                    new TestRequest.Get(), Collections.<String, String>emptyMap());
            fail("Should have thrown exception");
        }
        catch (IOException e)
        {
            verify(mMockConnection).disconnect();
        }
    }

    @Test
    public void executeRequestClosesConnection_noResponseBody() throws Exception
    {
        when(mMockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NO_CONTENT);
        mConnHttpStack.executeRequest(new TestRequest.Get(), Collections.<String, String>emptyMap());
        verify(mMockConnection).disconnect();
    }

    @Test
    public void executeRequestClosesConnection_hasResponseBody() throws Exception
    {
        when(mMockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mMockConnection.getInputStream())
                .thenReturn(new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));
        HttpResponse response =
                mConnHttpStack.executeRequest(
                        new TestRequest.Get(), Collections.<String, String>emptyMap());
        // Shouldn't be disconnected until the stream is consumed.
        verify(mMockConnection, never()).disconnect();
        response.getContent().close();
        verify(mMockConnection).disconnect();
    }

    @Test
    public void convertHeaders()
    {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(null, Collections.singletonList("Ignored"));
        headers.put("HeaderA", Collections.singletonList("ValueA"));
        List<String> values = new ArrayList<>();
        values.add("ValueB_1");
        values.add("ValueB_2");
        headers.put("HeaderB", values);
        List<Header> result = ConnHttpStack.convertHeaders(headers);
        List<Header> expected = new ArrayList<>();
        expected.add(new Header("HeaderA", "ValueA"));
        expected.add(new Header("HeaderB", "ValueB_1"));
        expected.add(new Header("HeaderB", "ValueB_2"));
        assertEquals(expected, result);
    }
}
