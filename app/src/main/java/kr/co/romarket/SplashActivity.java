package kr.co.romarket;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import kr.co.romarket.common.RomarketUtil;
import kr.co.romarket.common.ThreadTask;
import kr.co.romarket.config.Constant;

public class SplashActivity extends AppCompatActivity {

    public static ProgressBar mSplashWaitCircle;

    private final int MY_REQUEST_CODE = 100;
    private AppUpdateManager mAppUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        this.mSplashWaitCircle = (ProgressBar)findViewById(R.id.splashWaitCircle );

        // 시작시 파라메터 확인
        if(getIntent().getScheme() != null && Constant.schemeName.equals(getIntent().getScheme()) ) {
            Log.d("SplashActivity", "getIntent().getScheme() : ");
            MainActivity.pShopSeq = getIntent().getData().getQueryParameter("shop_seq");
            MainActivity.pPageCode = getIntent().getData().getQueryParameter("page_code");
        }
        Log.d("SplashActivity", "onCreate:pShopSeq : " + MainActivity.pShopSeq );
        Log.d("SplashActivity", "onCreate:pPageCode : " + MainActivity.pPageCode );

        // Version Name
        TextView mVersionName = (TextView) findViewById(R.id.mVersionName );
        int versionCode = 0;
        String versionName = null;

        // Version Number
        MainActivity.versionNumber = 0;
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = pinfo.versionCode;
            versionName = pinfo.versionName;

            MainActivity.versionNumber = versionCode;
            mVersionName.setText(String.format("Ver : %d %s", versionCode, versionName) );

        } catch (Exception e) {
            e.printStackTrace();

        }

        /*
        mAppUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        // 업데이트 사용 가능 상태인지 체크
        Task<AppUpdateInfo> appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();
        // 사용가능 체크 리스너를 달아준다
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
                Log.d("SplashActivity", "addOnSuccessListener : UPDATE " );

                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            // 유연한 업데이트 사용 시 (AppUpdateType.FLEXIBLE) 사용
                            AppUpdateType.IMMEDIATE,
                            // 현재 Activity
                            SplashActivity.this,
                            // 전역변수로 선언해준 Code
                            MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    Log.e("AppUpdater", "AppUpdateManager Error", e);
                    e.printStackTrace();
                }

            } else {
                Log.d("SplashActivity", "addOnSuccessListener : NOUPDATE " );
            }
        });
        */

        // Gcm
        MainActivity.andId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("SplashActivity", "onCreate:andId : " + MainActivity.andId);

        // Fcm
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token ) {
                        MainActivity.fcmId = token;
                        Log.d("SplashActivity", "onCreate:fcmId : " + MainActivity.fcmId );

                        checkServerStatus();
                    }
                }
        );

        // Display display = getWindowManager().getDefaultDisplay();  // in Activity
        // Point size = new Point();
        // display.getRealSize(size); // or getSize(size)
        // int width = size.x;
        // int height = size.y;
        // Log.d("SplashActivity:onCreate", "x : " + size.x + ", Y : " + size.y);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.d("AppUpdate", "Update flow failed! Result code: " + resultCode); // 로그로 코드 확인
                // showCustomSnackbar(findViewById(R.id.splash_main_view), "코로나 맵을 사용하기 위해서는 업데이트가 필요해요");  //snackbar로 사용자에게 알려주기
                Snackbar.make(findViewById(R.id.splach_layout), "앱을 사용하기 위해서는 업데이트가 필요해요", Snackbar.LENGTH_LONG).show();
                finishAffinity(); // 앱 종료
            }
        }
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, SplashActivity.this, MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
        */
    }

    // 서버 상태 체크
    private void checkServerStatus () {
        StringBuffer strbuf = new StringBuffer();
        String serverStatus = null;

        new ThreadTask<String, String>() {
            @Override
            protected void onPreExecute() {
                SplashActivity.mSplashWaitCircle.setVisibility(View.VISIBLE );
            }

            @Override
            protected String doInBackground(String arg) {
                String setResult = null;
                try {
                    String url = Constant.serverUrl + Constant.checkServerUrl;
                    setResult = RomarketUtil.httpConnect(url, null );
                    Log.d("SplashActivity", "checkServerStatus:setResult : " + setResult);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("SplashActivity", "checkServerStatus:exception : " + e.getMessage() );
                    // Error Dialog 호출
                    // thread 안에서 다이얼로그 호출
                    /*
                    Handler digHandler = new Handler(Looper.getMainLooper());
                    digHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CustomDialog customDialog = new CustomDialog(SplashActivity.this );
                            customDialog.setTitle("알림");
                            customDialog.setMessage("서버접속 오류입니다.\n네트웍 상태를 확인하시거나\n네트웍은 문제가 없으실 경우\n서버작업중일수 있으니\n잠시후에 다시 접속해 주십시요. [1]");
                            customDialog.setNegativeButtonText("");
                            customDialog.setPositiveButtonText("종료");
                            customDialog.showCustomDialog();
                            // negativeButton
                            customDialog.negativeButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    customDialog.dismissDialog();
                                    exitApp();
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
                    }, 0);
                    */
                }

                return setResult;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d("SplashActivity", "checkServerStatus:onPostExecute : " + result );
                SplashActivity.mSplashWaitCircle.setVisibility(View.INVISIBLE );

                if(StringUtils.isEmpty(result) ) {
                    // Error Dialog
                    showErrorDialog();
                    return;
                }

                JSONObject jsonObject = null;

                try {
                    jsonObject = new JSONObject(result );
                    String statCd = jsonObject.getString("statCd" );
                    String statMsg = jsonObject.getString("statMsg" );
                    String verNo = jsonObject.getString("verNo" );
                    String updNeedYn = jsonObject.getString("updNeedYn" );
                    String splashImgPath = jsonObject.getString("splashImgPath" );
                    Log.d("SplashActivity", "checkServerStatus:statCd : " + statCd );
                    Log.d("SplashActivity", "checkServerStatus:statMsg : " + statMsg );
                    Log.d("SplashActivity", "checkServerStatus:verNo : " + verNo );
                    Log.d("SplashActivity", "checkServerStatus:updNeedYn : " + updNeedYn );
                    Log.d("SplashActivity", "checkServerStatus:splashImgPath : " + splashImgPath );
                    // imgServerUrl
                    // MainActivity.versionNumber
                    // 필수 업데이트 체크

                    // updNeedYn = "Y";
                    // verNo = "88";

                    if("Y".equals(updNeedYn) && MainActivity.versionNumber < Integer.valueOf(verNo) ) {
                        // Update Dialog
                        showUpdateDialog();
                    } else {
                        if(Constant.serverStatusSuccess.equals(statCd) ) {

                            if(StringUtils.isNotEmpty(splashImgPath) ) {
                                downloadSplashImg (Constant.imgServerUrl + splashImgPath );
                            } else {
                                viewWebPage ();
                            }

                        } else {
                            // Error Dialog
                            showErrorDialog();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // Error Dialog
                    showErrorDialog();
                }

            }
        }.execute("");

    }


    private void downloadSplashImg (String imgUrl) {
        Log.d("SplashActivity", "downloadSplashImg : ");

        new ThreadTask<String, Bitmap>() {
            @Override
            protected void onPreExecute() {
                SplashActivity.mSplashWaitCircle.setVisibility(View.VISIBLE );
            }

            @Override
            protected Bitmap doInBackground(String arg) {
                Log.d("SplashActivity", "downloadSplashImg:arg : " + arg );
                Bitmap btmResult = null;
                URL url = null;
                InputStream in = null;
                try {
                    url = new URL(arg );
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(true );
                    in = connection.getInputStream();
                    btmResult = BitmapFactory.decodeStream(in);
                    Log.d("SplashActivity", "downloadSplashImg:btmResult : " + btmResult );

                } catch (Exception e) {
                    Log.d("SplashActivity", "downloadSplashImg:error : " + e.getMessage() );
                    e.printStackTrace();
                } finally {
                    if(in != null ) {
                        try {
                            in.close();
                        } catch (Exception e) {

                        }
                    }
                }

                return btmResult;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                Log.d("SplashActivity", "downloadSplashImg:result : " + result );
                SplashActivity.mSplashWaitCircle.setVisibility(View.INVISIBLE );

                if(result != null ) {
                    MainActivity.mSplashImg = result;
                }
                viewWebPage ();

            }
        }.execute(imgUrl);
    }


    private void viewWebPage ( ) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class );
        startActivity(intent );
        finish();
    }

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

    private void showErrorDialog () {
        CustomDialog customDialog = new CustomDialog(SplashActivity.this );
        customDialog.setTitle("알림");
        customDialog.setMessage("서버접속 오류입니다.\n네트웍 상태를 확인하시거나\n네트웍은 문제가 없으실 경우\n서버작업중일수 있으니\n잠시후에 다시 접속해 주십시요.");
        customDialog.setNegativeButtonText("다시시도");
        customDialog.setPositiveButtonText("종료");
        customDialog.showCustomDialog();
        // negativeButton
        customDialog.negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismissDialog();
                checkServerStatus();
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

    private void showUpdateDialog () {
        CustomDialog customDialog = new CustomDialog(SplashActivity.this );
        customDialog.setTitle("알림");
        customDialog.setMessage("필수 업데이트가 있습니다.\n업데이트 하시겠습니까?" );
        customDialog.setNegativeButtonText("종료");
        customDialog.setPositiveButtonText("업데이트");
        customDialog.showCustomDialog();
        // negativeButton
        customDialog.negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismissDialog();
                exitApp();
            }
        });
        // positiveButton
        customDialog.positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismissDialog();
                // 업데이트 처리

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("market://details?id=kr.co.dmart"));
                intent.setPackage("com.android.vending");
                startActivity(intent);

                exitApp();
            }
        });
    }

}



