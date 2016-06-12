package com.picspy.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.util.Map;

/**
 * Created by BrunelAmC on 4/2/2016.
 */
public class GameImageRequest extends ImageRequest {
    private static final String TAG = "GameImageReq";
    private Context context;

    /**
     * Creates a new image request, decoding to a maximum specified width and
     * height. If both width and height are zero, the image will be decoded to
     * its natural size. If one of the two is nonzero, that dimension will be
     * clamped and the other one will be set to preserve the image's aspect
     * ratio. If both width and height are nonzero, the image will be decoded to
     * be fit in the rectangle of dimensions width x height while keeping its
     * aspect ratio.
     *
     * @param url           URL of the image
     * @param listener      Listener to receive the decoded bitmap
     * @param maxWidth      Maximum width to decode this bitmap to, or zero for none
     * @param maxHeight     Maximum height to decode this bitmap to, or zero for
     *                      none
     * @param scaleType     The ImageViews ScaleType used to calculate the needed image size.
     * @param decodeConfig  Format to decode the bitmap to
     * @param errorListener Error listener, or null to ignore errors
     */
    private GameImageRequest(Context context, String url, Response.Listener<Bitmap> listener,
                             int maxWidth, int maxHeight, ImageView.ScaleType scaleType,
                            Bitmap.Config decodeConfig, Response.ErrorListener errorListener) {
        super(url, listener, maxWidth, maxHeight, scaleType, decodeConfig, errorListener);
        Log.d(TAG, url);
        this.context = context;
    }

    public static GameImageRequest getImage(Context context, String filename,
                                            Response.Listener<Bitmap> listener, int maxWidth,
                                            int maxHeight, ImageView.ScaleType scaleType,
                                            Bitmap.Config decodeConfig,
                                            Response.ErrorListener errorListener) {
        String url = DspUriBuilder.buildFileUploadUri(DspUriBuilder.FILE_URI, filename);
        return  new GameImageRequest(context, url, listener, maxWidth, maxHeight, scaleType,
                decodeConfig, errorListener);
    }

    /**
     * Returns a list of extra HTTP headers to go along with this request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     *
     * @throws AuthFailureError In the event of auth failure
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return  AppConstants.dspHeaders(context);
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }
}
