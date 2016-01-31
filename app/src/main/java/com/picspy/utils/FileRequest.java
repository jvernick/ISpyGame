package com.picspy.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.CharsetUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by BrunelAmC on 1/18/2016.
 */

public class FileRequest extends Request<String> {

    private static final String TAG = "FileReq";
    MultipartEntityBuilder entity = MultipartEntityBuilder.create();
    HttpEntity httpentity;

    private final Response.Listener<String> mListener;
    private Context context;

    private FileRequest(Context context, String url, Response.ErrorListener errorListener,
                        Response.Listener<String> listener, File file) {
        super(Method.POST, url, errorListener);

        this.context = context;
        this.mListener = listener;

        entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        try {
            entity.setCharset(CharsetUtils.get("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        httpentity = new FileEntity(file, "image/jpg");
    }

    public static FileRequest sendPicture(Context context, String imageFileName, String imageAbsolutePath, Response.Listener<String> responseListener, Response.ErrorListener errorListener) {

        String url = DspUriBuilder.buildFileUploadUri(DspUriBuilder.FILE_URI, imageFileName);
        return  new FileRequest(context, url, errorListener, responseListener, new File(imageAbsolutePath));
    }

    @Override
    public String getBodyContentType() {
        return httpentity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            httpentity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }

        return bos.toByteArray();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AppConstants.dspHeaders(context);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            Log.d(TAG, json);
            return Response.success(json,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
        /*try {
            System.out.println("Network Response " + new String(response.data, "UTF-8"));
            return Response.success(new String(response.data, "UTF-8"),
                    getCacheEntry());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.success(new String(response.data), getCacheEntry());
        }*/
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError){
        return VolleyRequest.parseNetworkError(volleyError);
    }

}