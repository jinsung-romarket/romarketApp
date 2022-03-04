package kr.co.romarket;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.zxing.integration.android.IntentIntegrator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.net.URISyntaxException;

import kr.co.romarket.common.RomarketUtil;
import kr.co.romarket.common.ThreadTask;
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

    //
    public static String mZoomImgSrc;
    // 위치 기반
    public static LocationManager mLocationManager;

    // Back Button 처리
    private long backPressedTime = 0;

    // App 확인
    private LifecycleObserver lifecycleObserver = null;
    public static boolean isBackGround = true;

    // Cookie
    public static CookieManager cookieManager;

    // Splash Image
    public static Bitmap mSplashImg;

    public static ProgressBar mMainWaitCircle;

    final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            String provider = location.getProvider();
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();

            Log.d("MainActivity", "onLocationChanged:provider : " + provider);
            // txtResult.setText("위치정보 : " + provider + "\n" + "위도 : " + longitude + "\n" + "경도 : " + latitude + "\n" + "고도 : " + altitude);

            StringBuffer locationUrl = new StringBuffer();
            locationUrl.append("javascript:");
            locationUrl.append("response_location (");
            locationUrl.append(longitude).append(",");
            locationUrl.append(latitude).append(",");
            locationUrl.append(altitude).append(",");
            locationUrl.append("'").append(provider).append("'");
            locationUrl.append(");");

            Log.d("MainActivity", "onLocationChanged:locationUrl : " + locationUrl.toString() );
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

    // App 종료
    private void exitApp () {
        moveTaskToBack(true );
        if(Build.VERSION.SDK_INT >= 21 ) {
            finishAndRemoveTask();
        } else {
            finish();
        }
        System.exit(0 );
    }

    private void setupLifeCycleObserver() {
        this.lifecycleObserver = new CycleListener();
        this.isBackGround = true;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this.lifecycleObserver );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLifeCycleObserver();

        setContentView(R.layout.activity_main);

        // 혹시 Extra로 들어온 데이터가 있는지 확인
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if(StringUtils.isNotEmpty(extras.getString("shopSeq")) ) {
                this.pShopSeq = extras.getString("shopSeq");
                Log.d("MainActivity", "onCreate:extras.shopSeq : " + this.pShopSeq );
            }
            if(StringUtils.isNotEmpty(extras.getString("pageCode")) ) {
                this.pPageCode = extras.getString("pageCode");
                Log.d("MainActivity", "onCreate:extras.pageCode : " + this.pPageCode );
            }
        }

        // 파라메터 log
        Log.d("MainActivity", "onCreate:andId : " + this.andId);
        Log.d("MainActivity", "onCreate:fcmId : " + this.fcmId);
        Log.d("MainActivity", "onCreate:pShopSeq : " + this.pShopSeq);
        Log.d("MainActivity", "onCreate:pPageCode : " + this.pPageCode);

        this.mainActivityContext = this;

        // 화면 객체
        this.mainWebView = (WebView) findViewById(R.id.mainWebView );
        this.mainImageView = (ImageView) findViewById(R.id.splashImgViewMain );
        this.mMainWaitCircle = (ProgressBar) findViewById(R.id.mainWaitCircle );

        // Splash 이미지 변경
        this.mainImageView.setImageBitmap(this.mSplashImg );

        /*
        double rnd = Math.random();
        int splashIdx = (int)(rnd * 100) % 2;
        if(splashIdx == 0) {
            mainImageView.setImageResource(R.drawable.main_splash01);
        } else {
            mainImageView.setImageResource(R.drawable.main_splash02);
        }
        */

        // 위치 기반
        this.mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Web View 설정
        // 자바스크립트 사용여부
        this.mainWebView.getSettings().setJavaScriptEnabled(true );
        // 자바스크립트가 창을 자동으로 열 수 있게할지 여부
        this.mainWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true );
        // 이미지 자동 로드
        this.mainWebView.getSettings().setLoadsImagesAutomatically(true );
        // wide viewport 설정
        // 설정값이 false인 경우, layout 너비는 디바이스 픽셀에 맞추어 설정된다.
        // 값이 true이고 페이지에 뷰포트 메타 태그가 있으면 태그에 지정된 너비 값이 사용된다.
        // 페이지에 태그가 없거나 너비가 없는 경우 넓은 뷰포트가 사용된다.
        this.mainWebView.getSettings().setUseWideViewPort(true );
        //컨텐츠가 웹뷰보다 클때 스크린크기에 맞추기
        this.mainWebView.getSettings().setLoadWithOverviewMode(true );
        // 줌설정
        this.mainWebView.getSettings().setSupportZoom(false );
        // 줌아이콘
        this.mainWebView.getSettings().setBuiltInZoomControls(false );
        // 캐시설정
        // LOAD_CACHE_ELSE_NETWORK : 캐시 기간만료 시 네트워크 접속
        // LOAD_CACHE_ONLY : 캐시만 불러옴 (네트워크 사용 X)
        // LOAD_DEFAULT : 기본 모드, 캐시 사용, 기간 만료 시 네트워크 사용
        // LOAD_NO_CACHE : 캐시모드 사용안함
        // LOAD_NORMAL : 기본모드 캐시 사용 @Deprecated
        this.mainWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE );
        // 앱내부의 캐시 사용 여부
        this.mainWebView.getSettings().setAppCacheEnabled(false );
        // 로컬 스토리지 사용여부
        this.mainWebView.getSettings().setDomStorageEnabled(true );
        // 파일 액세스 허용 여부
        this.mainWebView.getSettings().setAllowFileAccess(true );
        // 멀티윈도우를 지원할지 여부
        this.mainWebView.getSettings().setSupportMultipleWindows(true );
        // 웹뷰를 통해 Content URL 에 접근할지 여부
        this.mainWebView.getSettings().setAllowContentAccess(true );
        // 내부에 @JavascriptInterface 메서드 구현
        this.mainWebView.addJavascriptInterface(new AndroidBridge(), "android");

        // Custom Web View
        this.mainWebView.setWebChromeClient(new CustomWebChromeClient() );
        this.mainWebView.setWebViewClient(new CustomWebViewClient() );

        this.mainWebView.clearCache(true);

        // Cookie Set
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        setCookieAllow(this.mainWebView );
        Log.d("MainActivity", "onCreate:this.cookieManager : " + this.cookieManager );

        // 서버로 폰정보 전송
        setPhoneInfo();

    }

    private void setCookieAllow( WebView webView) {
        try {
            this.cookieManager = CookieManager.getInstance();
            this.cookieManager.setAcceptCookie(true);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                this.cookieManager.setAcceptThirdPartyCookies(webView, true);
            }
        } catch (Exception e) {

        }
    }

    // 서버로 폰정보 전송
    public void setPhoneInfo () {

        new ThreadTask<String, String>() {
            @Override
            protected void onPreExecute() {
                MainActivity.mMainWaitCircle.setVisibility(View.VISIBLE );
            }

            @Override
            protected String doInBackground(String arg) {
                String setResult = null;
                StringBuffer urlBuf = new StringBuffer();
                urlBuf.append(Constant.serverUrl );
                urlBuf.append(Constant.setInfoUrl );

                urlBuf.append("?").append("dvKind=").append("android" );
                urlBuf.append("&").append("groupId=").append("" );
                urlBuf.append("&").append("dvId=").append("" );
                urlBuf.append("&").append("andId=").append(MainActivity.andId );
                urlBuf.append("&").append("fcmId=").append(MainActivity.fcmId );
                urlBuf.append("&").append("appVer=").append(MainActivity.versionNumber );

                // MainActivity.pShopSeq = "2";
                if(StringUtils.isNotEmpty(MainActivity.pShopSeq) ) {
                    urlBuf.append("&").append("shopSeq=").append(MainActivity.pShopSeq );
                }

                try {
                    Log.d("MainActivity", "setPhoneInfo:urlBuf : " + urlBuf.toString() );
                    setResult = RomarketUtil.httpConnect(urlBuf.toString() , null );
                    Log.d("MainActivity", "setPhoneInfo:setResult : " + setResult );
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("MainActivity", "setPhoneInfo:exception : " + e.getMessage() );
                }

                return setResult;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d("MainActivity", "setPhoneInfo:onPostExecute : " + result );
                MainActivity.mMainWaitCircle.setVisibility(View.INVISIBLE );

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result );
                    String dvId = jsonObject.getString("dvId" );
                    String shopSeq = jsonObject.getString("shopSeq" );
                    String memberName = jsonObject.getString("memberName");
                    String memberSeq = jsonObject.getString("memberSeq");
                    Log.d("MainActivity", "setPhoneInfo:dvId : " + dvId );
                    Log.d("MainActivity", "setPhoneInfo:shopSeq : " + shopSeq );

                    // dvId = "null";
                    if(StringUtils.isNotEmpty(dvId) && !"null".equals(dvId ) ) {
                        StringBuffer urlBuf = new StringBuffer();
                        String pageUrl = Constant.mainViewUrl;
                        if(Constant.PAGE_CODE_TODAY.equals(MainActivity.pPageCode) ) {
                            pageUrl = Constant.todayViewUrl;
                        } else if (Constant.PAGE_CODE_EVENT.equals(MainActivity.pPageCode) ) {
                            pageUrl = Constant.evtViewUrl;
                        } else if (Constant.PAGE_CODE_CART.equals(MainActivity.pPageCode) ) {
                            pageUrl = Constant.cartViewUrl;
                        } else if (Constant.PAGE_CODE_ORDER.equals(MainActivity.pPageCode) ) {
                            pageUrl = Constant.orderViewUrl;
                        }

                        // 앱 시작시 페이지 로딩
                        urlBuf.append(Constant.serverUrl );
                        urlBuf.append(pageUrl );

                        // cookie Set
                        // cookieManager.removeAllCookies(null );
                        String ckStr = cookieManager.getCookie(Constant.serverUrl);
                        String loginOption = null;
                        Log.d("MainActivity", "setPhoneInfo:ckStr : " + ckStr );
                        if(StringUtils.isNotEmpty(ckStr) ) {
                            String[] temp = ckStr.split(";");
                            for (String ar1 : temp ){
                                Log.d("MainActivity", "setPhoneInfo:cookie : " + ar1 );
                                if(ar1.contains("loginOption")){
                                    String[] temp1 = ar1.split("=");
                                    loginOption = temp1[1];
                                    break;
                                }
                            }
                        }

                        cookieManager.setCookie(Constant.serverUrl, String.format("%s=%s", "dvId", dvId));
                        Log.d("MainActivity", "setPhoneInfo:loginOption : " + loginOption );
                        if(StringUtils.isEmpty(loginOption) || "NO_AUTO_LOGIN".equals(loginOption) ) {
                            loginOption = "AUTO_LOGIN";
                            cookieManager.setCookie(Constant.serverUrl, String.format("%s=%s", "loginOption", loginOption));
                        }
                        if(StringUtils.isNotEmpty(shopSeq) ) {
                            Log.d("MainActivity", "setPhoneInfo:cookie shopSeq : " + shopSeq );
                            cookieManager.setCookie(Constant.serverUrl, String.format("%s=%s", "shopSeq", shopSeq));
                        }

                        // Toast Message
                        if(StringUtils.isNotEmpty(memberName) && StringUtils.isNotEmpty(memberSeq) && !"LOGOUT".equals(loginOption) ) {
                            String toastMsg = String.format("%s님 환영합니다.", memberName );
                            // Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG ).show();
                            customToastView(toastMsg );
                        } else {
                            if ("LOGOUT".equals(loginOption)) {
                                String toastMsg = String.format("로그인후 사용해주세요.", memberName);
                                // Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG ).show();
                                customToastView(toastMsg);
                            }
                        }

                        Log.d("MainActivity", "setPhoneInfo:urlBuf : " + urlBuf.toString() );
                        MainActivity.mMainWaitCircle.setVisibility(View.VISIBLE );

                        MainActivity.mainWebView.loadUrl(urlBuf.toString() );

                        // 초기화
                        MainActivity.pShopSeq = null;
                        MainActivity.pPageCode = null;
                    } else {

                        CustomDialog customDialog = new CustomDialog(MainActivity.this );
                        customDialog.setTitle("알림");
                        customDialog.setMessage("서버접속 오류입니다.\n네트웍 상태를 확인하시거나\n네트웍은 문제가 없으실 경우\n서버작업중일수 있으니\n잠시후에 다시 접속해 주십시요.");
                        customDialog.setNegativeButtonText("다시시도");
                        customDialog.setPositiveButtonText("종료");
                        customDialog.showCustomDialog();
                        // negativeButton
                        customDialog.negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setPhoneInfo();
                            }
                        });
                        // positiveButton
                        customDialog.positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismissDialog();
                                exitApp();
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

        }.execute("");

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
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
                // Toast.makeText(getApplicationContext(), "한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
                customToastView("한번 더 누르면 앱이 종료됩니다.");
            }
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode ) {
            case Constant.REQUEST_CODE_CAMERA : {
                Log.d("MainActivity", "onRequestPermissionsResult:REQUEST_CODE_CAMERA : " );

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
                Log.d("MainActivity", "onRequestPermissionsResult:REQUEST_CODE_LOCALTION : " );

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Toast.makeText(this, "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_LONG).show();
                    customToastView("앱 실행을 위한 권한이 설정 되었습니다" );

                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, mLocationListener );
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, mLocationListener );

                } else {
                    // Toast.makeText(this, "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_LONG).show();
                    customToastView("앱 실행을 위한 권한이 취소 되었습니다" );

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
        Log.d("MainActivity", "onActivityResult:requestCode : " + requestCode );

        if(requestCode == Constant.RESULT_REQUEST_SCAN ) {
            if(resultCode == RESULT_OK ) {
                String barcode = data.getStringExtra("SCAN_BARCODE");
                String barcodeType = data.getStringExtra("BARCODE_TYPE");
                Log.d("MainActivity", "onActivityResult:barcode : " + barcode );
                Log.d("MainActivity", "onActivityResult:barcodeType : " + barcodeType );

                if(StringUtils.isNotEmpty(barcode) ) {
                    // customToastView(barcode );
                    StringBuffer barcodeUrl = new StringBuffer();
                    barcodeUrl.append("javascript:");
                    barcodeUrl.append("barcodeScanResult (");
                    barcodeUrl.append("'").append(barcode).append("'");
                    barcodeUrl.append(");");

                    Log.d("MainActivity", "onActivityResult:barcodeUrl : " + barcodeUrl.toString() );
                    mainWebView.loadUrl(barcodeUrl.toString() );

                }
            }
        }

    }

    /** -------------------------------------------------------------------- **/
    // Zoom Image
    public void showZoomImg (String zoomImgSrc ) {
        Log.d("MainActivity", "showZoomImg:zoomImgSrc : " + zoomImgSrc );

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
            /*
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setOrientationLocked(true );
            integrator.setPrompt("붉은선에 바코드를 맞춰주세요.");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
            */

            Intent intent = new Intent(MainActivity.this, ScanActivity.class );
            startActivityForResult(intent, Constant.RESULT_REQUEST_SCAN );
        }
    }

    public void requestLocation () {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Log.d("MainActivity", "requestLocation:REQUEST_CODE_LOCALTION 1 : " );
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Toast.makeText(this, "앱 실행을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();
                customToastView("앱 실행을 위해서는 권한을 설정해야 합니다" );
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constant.REQUEST_CODE_LOCALTION );
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constant.REQUEST_CODE_LOCALTION );
            }

        } else {
            Log.d("MainActivity", "requestLocation:REQUEST_CODE_LOCALTION 2 : " );
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, mLocationListener );
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, mLocationListener );

        }
    }

    public void customToastView (String msg ) {
        LayoutInflater inflater = getLayoutInflater();

        View toastDesign = inflater.inflate(R.layout.custom_toast, (ViewGroup)findViewById(R.id.toast_design_root));
        TextView text = toastDesign.findViewById(R.id.toastMsgTextView );
        text.setText(msg );

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 30); // CENTER를 기준으로 0, 0 위치에 메시지 출력
        toast.setDuration(Toast.LENGTH_SHORT );
        toast.setView(toastDesign);
        toast.show();
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

            // 쿠키허용
            setCookieAllow(newWebView );

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
            final JsResult finalRes = result;
            new AlertDialog.Builder(view.getContext())
                    .setTitle("로마켓")
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finalRes.confirm();
                        }
                    })
                    .create()
                    .show();
            return true;
        }
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            final JsResult finalRes = result;
            new AlertDialog.Builder(view.getContext())
                    .setTitle("로마켓")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finalRes.confirm();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finalRes.cancel();
                                }
                            })
                    .create()
                    .show();
            return true;
        }
    } // class CustomWebChromeClient

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

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("MainActivity", "onPageStarted:OK : " );
        }

        /**
         * 웹페이지 로딩이 끝났을 때 처리
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("MainActivity", "onPageFinished:OK : " );
            if(mainImageView.getVisibility() == View.VISIBLE ) {
                mainImageView.setVisibility(View.GONE);
                mMainWaitCircle.setVisibility(View.GONE);
            }

            // Cooike
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.getInstance().sync();
            } else {
                CookieManager.getInstance().flush();
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
    } // class CustomWebViewClient

}