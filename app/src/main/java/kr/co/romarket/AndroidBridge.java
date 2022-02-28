package kr.co.romarket;

import android.os.Handler;
import android.webkit.JavascriptInterface;

final class AndroidBridge {
    private final Handler handler = new Handler();

    @JavascriptInterface
    public void zoom_img(final String zoomImgSrc) {
        handler.post(new Runnable() {
            public void run() {
                ((MainActivity)MainActivity.mainActivityContext).showZoomImg(zoomImgSrc );
            }
        });
    }

    @JavascriptInterface
    public void request_scan() { // response_scan
        handler.post(new Runnable() {
            public void run() {
                ((MainActivity)MainActivity.mainActivityContext).requestScan();
            }
        });
    }

    @JavascriptInterface
    public void request_location() { // response_location
        handler.post(new Runnable() {
            public void run() {
                ((MainActivity)MainActivity.mainActivityContext).requestLocation();
            }
        });
    }

    @JavascriptInterface
    public void check_phone_num() {
        handler.post(new Runnable() {
            public void run() {
                // ((MainActivity)MainActivity.mainActivityContext).requestPhoneNumber();
            }
        });
    }



}
