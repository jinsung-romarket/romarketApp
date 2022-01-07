package kr.co.romarket;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.zxing.integration.android.IntentIntegrator;

import org.apache.commons.lang3.StringUtils;

import java.net.URISyntaxException;

import kr.co.romarket.config.Constant;

public class MainActivity extends AppCompatActivity {

    // Android ID
    public static String andId = null;
    // Fcm Id
    public static String fcmId = null;
    // Version Number
    public static int versionNumber = 0;
    // 앱 시작시 파라메터
    public static String pShopSeq = null;
    public static String pPageCode = null;
    // Web View
    public static WebView mainWebView;
    public static ImageView mainImageView;

    public static Context mainActivityContext;
    public static Activity mainActivity;

    //
    public static String mZoomImgSrc;
    // 위치 기반
    public static LocationManager mLocationManager;

    // Back Button 처리
    private long backPressedTime = 0;

    final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            String provider = location.getProvider();
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();

            Log.d("MainActivity:gpsLocationListener", "provider : " + provider);
            // txtResult.setText("위치정보 : " + provider + "\n" + "위도 : " + longitude + "\n" + "경도 : " + latitude + "\n" + "고도 : " + altitude);

            StringBuffer locationUrl = new StringBuffer();
            locationUrl.append("javascript:");
            locationUrl.append("response_location (");
            locationUrl.append(longitude).append(",");
            locationUrl.append(latitude).append(",");
            locationUrl.append(altitude).append(",");
            locationUrl.append("'").append(provider).append("'");
            locationUrl.append(");");

            Log.d("MainActivity:gpsLocationListener", "locationUrl : " + locationUrl.toString() );
            mainWebView.loadUrl(locationUrl.toString() );
            mLocationManager.removeUpdates(mLocationListener);

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 파라메터 log
        Log.d("MainActivity:onCreate", "andId : " + this.andId);
        Log.d("MainActivity:onCreate", "fcmId : " + this.fcmId);
        Log.d("MainActivity:onCreate", "pShopSeq : " + this.pShopSeq);
        Log.d("MainActivity:onCreate", "pPageCode : " + this.pPageCode);

        this.mainActivityContext = this;

        // 화면 객체
        mainWebView = (WebView) findViewById(R.id.mainWebView );
        mainImageView = (ImageView) findViewById(R.id.splashImgViewMain );

        // Splash 이미지 변경
        double rnd = Math.random();
        int splashIdx = (int)(rnd * 100) % 2;
        if(splashIdx == 0) {
            mainImageView.setImageResource(R.drawable.main_splash01);
        } else {
            mainImageView.setImageResource(R.drawable.main_splash02);
        }

        // 위치 기반
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Web View 설정
        // 자바스크립트 사용여부
        mainWebView.getSettings().setJavaScriptEnabled(true );
        // 자바스크립트가 창을 자동으로 열 수 있게할지 여부
        mainWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true );
        // 이미지 자동 로드
        mainWebView.getSettings().setLoadsImagesAutomatically(true );
        // wide viewport 설정
        // 설정값이 false인 경우, layout 너비는 디바이스 픽셀에 맞추어 설정된다.
        // 값이 true이고 페이지에 뷰포트 메타 태그가 있으면 태그에 지정된 너비 값이 사용된다.
        // 페이지에 태그가 없거나 너비가 없는 경우 넓은 뷰포트가 사용된다.
        mainWebView.getSettings().setUseWideViewPort(true );
        //컨텐츠가 웹뷰보다 클때 스크린크기에 맞추기
        mainWebView.getSettings().setLoadWithOverviewMode(true );
        // 줌설정
        mainWebView.getSettings().setSupportZoom(false );
        // 줌아이콘
        mainWebView.getSettings().setBuiltInZoomControls(false );
        // 캐시설정
        // LOAD_CACHE_ELSE_NETWORK : 캐시 기간만료 시 네트워크 접속
        // LOAD_CACHE_ONLY : 캐시만 불러옴 (네트워크 사용 X)
        // LOAD_DEFAULT : 기본 모드, 캐시 사용, 기간 만료 시 네트워크 사용
        // LOAD_NO_CACHE : 캐시모드 사용안함
        // LOAD_NORMAL : 기본모드 캐시 사용 @Deprecated
        mainWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE );
        // 앱내부의 캐시 사용 여부
        mainWebView.getSettings().setAppCacheEnabled(false );
        // 로컬 스토리지 사용여부
        mainWebView.getSettings().setDomStorageEnabled(true );
        // 파일 액세스 허용 여부
        mainWebView.getSettings().setAllowFileAccess(true );
        // 멀티윈도우를 지원할지 여부
        mainWebView.getSettings().setSupportMultipleWindows(true );
        // 웹뷰를 통해 Content URL 에 접근할지 여부
        mainWebView.getSettings().setAllowContentAccess(true );
        // 내부에 @JavascriptInterface 메서드 구현
        mainWebView.addJavascriptInterface(new AndroidBridge(), "android");

        // Custom Web View
        mainWebView.setWebChromeClient(new CustomWebChromeClient() );
        mainWebView.setWebViewClient(new CustomWebViewClient() );

        mainWebView.clearCache(true);

        StringBuffer urlBuf = new StringBuffer();
        String pageUrl = Constant.mainViewUrl;
        if("TODAY".equals(this.pPageCode) ) {
            pageUrl = Constant.todayViewUrl;
        }

        //TEST
        // urlBuf.append(Constant.serverUrl );
        // urlBuf.append("/main_test.php" );
        // urlBuf.append(Constant.checkServerUrl );

        //
        urlBuf.append("https://app.ro-market.com" );
        urlBuf.append(pageUrl );

        // Param
        urlBuf.append("?").append("dv_kind=").append("android" );
        urlBuf.append("&").append("group_id=").append("" );
        urlBuf.append("&").append("dv_id=").append("" );
        urlBuf.append("&").append("and_id=").append(this.andId );
        urlBuf.append("&").append("fcm_id=").append(this.fcmId );
        urlBuf.append("&").append("dv_ver=").append(this.versionNumber );
        this.pShopSeq = "2";
        if(StringUtils.isNotEmpty(this.pShopSeq) ) {
            urlBuf.append("&").append("p_shop_seq=").append(this.pShopSeq );
        }

        Log.d("MainActivity:onCreate", "urlBuf : " + urlBuf.toString() );

        mainWebView.loadUrl(urlBuf.toString() );

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /** Back Button */
    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        long intervalTime = currentTime - backPressedTime;

        if(this.mainWebView.canGoBack() ) {
            this.mainWebView.goBack();
        } else {
            if (0 <= intervalTime && Constant.FINISH_INTERVAL_TIME >= intervalTime) {
                finish();
            } else {
                backPressedTime = currentTime;
                Toast.makeText(getApplicationContext(), "한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode ) {
            case Constant.REQUEST_CODE_CAMERA : {
                Log.d("MainActivity:onRequestPermissionsResult", "REQUEST_CODE_CAMERA : " );

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    IntentIntegrator integrator = new IntentIntegrator(this);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                    integrator.setPrompt("붉은선에 바코드를 맞춰주세요.");
                    integrator.setCameraId(0);
                    integrator.setBeepEnabled(true);
                    integrator.setBarcodeImageEnabled(false);
                    integrator.initiateScan();

                }

                return;
            }

            case Constant.REQUEST_CODE_LOCALTION : {
                Log.d("MainActivity:onRequestPermissionsResult", "REQUEST_CODE_LOCALTION : " );

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_LONG).show();

                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, mLocationListener );
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, mLocationListener );

                } else {
                    Toast.makeText(this, "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_LONG).show();

                }

                return;
            }

            case Constant.REQUEST_CODE_PHONE : {

                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 바코드 리딩 처리
        if (resultCode != 0) {
            String barcode = data.getStringExtra("SCAN_RESULT");
            String barcodeFormat = data.getStringExtra("SCAN_RESULT_FORMAT");

            Log.d("MainActivity:onActivityResult", "barcodeFormat : " + barcodeFormat);
            Log.d("MainActivity:onActivityResult", "barcode : " + barcode);

            if(StringUtils.isNotEmpty(barcode) && "EAN_13".equals(barcodeFormat) ) {
                // mWebView.loadUrl("javascript:response_bar_code('"+str_bar_code+"')");

                StringBuffer barcodeUrl = new StringBuffer();
                barcodeUrl.append("javascript:");
                barcodeUrl.append("response_scan (");
                barcodeUrl.append("'").append(barcode).append("'");
                barcodeUrl.append(");");

                Log.d("MainActivity:onActivityResult", "barcodeUrl : " + barcodeUrl.toString() );
                mainWebView.loadUrl(barcodeUrl.toString() );

            } else {

            }
        }
    }

    /** -------------------------------------------------------------------- **/
    // Zoom Image
    public void showZoomImg (String zoomImgSrc ) {
        Log.d("MainActivity:showZoomImg", "zoomImgSrc : " + zoomImgSrc );

        this.mZoomImgSrc = zoomImgSrc;
        Intent intent = new Intent(
                getApplicationContext(), // 현재 화면의 제어권자
                ZoomActivity.class); // 다음 넘어갈 클래스 지정
        startActivity(intent);
        // overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

    // Barcode Scan
    public void requestScan () {
        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA }, Constant.REQUEST_CODE_CAMERA );
        } else {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("붉은선에 바코드를 맞춰주세요.");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
        }
    }

    public void requestLocation () {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Log.d("MainActivity:requestLocation", "REQUEST_CODE_LOCALTION 1 : " );
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "앱 실행을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constant.REQUEST_CODE_LOCALTION );
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constant.REQUEST_CODE_LOCALTION );
            }

        } else {
            Log.d("MainActivity:requestLocation", "REQUEST_CODE_LOCALTION 2 : " );
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, mLocationListener );
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, mLocationListener );

        }
    }

    /** Custom Class -------------------------------------------------------------------- **/
    // Custom web
    public class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            // MyLog.i(TAG,"onProgressChanged(view:"+view.toString()+ ", newProgress:"+newProgress+")");
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            // MyLog.toastMakeTextShow(view.getContext(), "TAG", "window.open 협의가 필요합니다.");
            WebView newWebView = new WebView(view.getContext());
            WebSettings webSettings = newWebView.getSettings();
            WebSettings settings = newWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setSupportMultipleWindows(true);

            // final Dialog dialog = new Dialog(view.getContext(), R.style.Theme_DialogFullScreen); Theme_Translucent_NoTitleBar_Fullscreen
            final Dialog dialog = new Dialog(view.getContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen );
            dialog.setContentView(newWebView);
            dialog.show();

            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_BACK) {
                        //MyLog.toastMakeTextShow(view.getContext(), "TAG", "KEYCODE_BACK");
                        if(newWebView.canGoBack()){
                            newWebView.goBack();
                        } else {
                            // MyLog.toastMakeTextShow(view.getContext(), "TAG", "Window.open 종료");
                            dialog.dismiss();
                        }
                        return true;
                    }else{
                        return false;
                    }
                }
            });
            newWebView.setWebViewClient(new CustomWebViewClient());
            newWebView.setWebChromeClient(new CustomWebChromeClient() {
                @Override
                public void onCloseWindow(WebView window) {
                    dialog.dismiss();
                }
            });

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
        }
        @Override
        public void onCloseWindow(WebView window) {
            window.setVisibility(View.GONE);
            window.destroy();
            //mWebViewSub=null;
            super.onCloseWindow(window);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            AlertDialog dialog = new AlertDialog.Builder(view.getContext()).
                    setTitle("로마켓").
                    setMessage(message).
                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    }).create();
            dialog.show();
            result.confirm();
            return true;
        }
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("로마켓")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .create()
                    .show();
            return true;
        }
    }

    public class CustomWebViewClient extends WebViewClient {
        @Override
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (!request.getUrl().toString().startsWith("http://") && !request.getUrl().toString().startsWith("https://") && !request.getUrl().toString().startsWith("javascript:")) {
                Intent intent = null;
                try {
                    intent = Intent.parseUri(request.getUrl().toString(), Intent.URI_INTENT_SCHEME);
                    Uri uri = Uri.parse(intent.getDataString());
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));

                    return true;
                } catch (URISyntaxException e) {
                    return false;
                } catch (ActivityNotFoundException e) {
                    if (intent == null) {
                        return false;
                    }

                    String packageName = intent.getPackage();
                    if (packageName != null) {
                        view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                        return true;
                    }
                    return false;
                }
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        /**
         * 웹페이지 로딩이 끝났을 때 처리
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("MainActivity:onPageFinished", "OK : " );
            if(mainImageView.getVisibility() == View.VISIBLE ) {
                mainImageView.setVisibility(View.GONE );
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                // Log.i(TAG, "onReceivedError() " + errorCode + " ---> " + description);
                onReceivedError(errorCode,description);
            }
        }

        private void onReceivedError(int errorCode, String description){
            switch (errorCode) {
                case WebViewClient.ERROR_TIMEOUT:   //연결 시간 초과
                case WebViewClient.ERROR_CONNECT:   //서버로 연결 실패
                    //case WebViewClient.ERROR_UNKNOWN:   // 일반 오류
                case WebViewClient.ERROR_FILE_NOT_FOUND: //404
                case WebViewClient.ERROR_HOST_LOOKUP :
                case WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME:
                case WebViewClient.ERROR_AUTHENTICATION:
                case WebViewClient.ERROR_PROXY_AUTHENTICATION:
                case WebViewClient.ERROR_IO:
                case WebViewClient.ERROR_REDIRECT_LOOP:
                case WebViewClient.ERROR_UNSUPPORTED_SCHEME:
                case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
                case WebViewClient.ERROR_BAD_URL:
                case WebViewClient.ERROR_FILE:
                case WebViewClient.ERROR_TOO_MANY_REQUESTS:
                case WebViewClient.ERROR_UNSAFE_RESOURCE:
                    break;
            }
        }

    }


}