package kr.co.romarket;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        this.mSplashWaitCircle = (ProgressBar)findViewById(R.id.splashWaitCircle );

        // 시작시 파라메터 확인
        if(getIntent().getScheme() != null && Constant.schemeName.equals(getIntent().getScheme()) ) {
            MainActivity.pShopSeq = getIntent().getData().getQueryParameter("shop_seq");
            MainActivity.pPageCode = getIntent().getData().getQueryParameter("page_code");
        }
        Log.d("SplashActivity:onCreate", "pShopSeq : " + MainActivity.pShopSeq );
        Log.d("SplashActivity:onCreate", "pPageCode : " + MainActivity.pPageCode );

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

        // Gcm
        MainActivity.andId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("SplashActivity:onCreate", "andId : " + MainActivity.andId);

        // Fcm
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token ) {
                        MainActivity.fcmId = token;
                        Log.d("SplashActivity:onSuccess", "fcmId : " + MainActivity.fcmId );

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
                    Log.d("SplashActivity:checkServerStatus", "setResult : " + setResult);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("SplashActivity:checkServerStatus", "exception : " + e.getMessage() );
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
                Log.d("SplashActivity:setPhoneInfo", "onPostExecute : " + result );
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
                    Log.d("SplashActivity:Handler", "statCd : " + statCd );
                    Log.d("SplashActivity:Handler", "statMsg : " + statMsg );
                    Log.d("SplashActivity:Handler", "verNo : " + verNo );
                    Log.d("SplashActivity:Handler", "updNeedYn : " + updNeedYn );
                    Log.d("SplashActivity:Handler", "splashImgPath : " + splashImgPath );
                    // imgServerUrl
                    // MainActivity.versionNumber
                    // 필수 업데이트 체크
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
        Log.d("SplashActivity:downloadSplashImg", "downloadSplashImg : ");

        new ThreadTask<String, Bitmap>() {
            @Override
            protected void onPreExecute() {
                SplashActivity.mSplashWaitCircle.setVisibility(View.VISIBLE );
            }

            @Override
            protected Bitmap doInBackground(String arg) {
                Log.d("SplashActivity:downloadSplashImg", "arg : " + arg );
                Bitmap btmResult = null;
                URL url = null;
                InputStream in = null;
                try {
                    url = new URL(arg );
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(true );
                    in = connection.getInputStream();
                    btmResult = BitmapFactory.decodeStream(in);
                    Log.d("SplashActivity:downloadSplashImg", "btmResult : " + btmResult );

                } catch (Exception e) {
                    Log.d("SplashActivity:downloadSplashImg", "error : " + e.getMessage() );
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
                Log.d("SplashActivity:downloadSplashImg", "downloadSplashImg : " + result );
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
                exitApp();
            }
        });
    }

}



