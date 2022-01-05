package kr.co.romarket;

import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

final class AndroidBridge {
    private final Handler handler = new Handler();

    @JavascriptInterface
    public void callAndroid(final String str ) {
        handler.post(new Runnable() {
            public void run() {

            }
        });
    }

    @JavascriptInterface
    public void zoom_img(final String zoomImgSrc) {
        handler.post(new Runnable() {
            public void run() {
                //((ZoomImg_activity)ZoomImg_activity.zoom_img_context).show_zoom_img(zoom_img_src);
                ((MainActivity)MainActivity.mainActivityContext).showZoomImg(zoomImgSrc );
            }
        });
    }

    @JavascriptInterface
    public void request_run_scan(final String call_from) {
        handler.post(new Runnable() {
            public void run() {
                ((MainActivity)MainActivity.mainActivityContext).barcodeScan();
            }
        });
    }




}
