package kr.co.romarket;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONObject;

import kr.co.romarket.common.RomarketUtil;
import kr.co.romarket.config.Constant;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 시작시 파라메터 확인
        if(getIntent().getScheme() != null && Constant.schemeName.equals(getIntent().getScheme()) ) {
            MainActivity.pShopSeq = getIntent().getData().getQueryParameter("shop_seq");
            MainActivity.pPageCode = getIntent().getData().getQueryParameter("page_code");
        }
        Log.d("SplashActivity", "pShopSeq : " + MainActivity.pShopSeq );
        Log.d("SplashActivity", "pPageCode : " + MainActivity.pPageCode );

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
        Log.d("SplashActivity", "andId : " + MainActivity.andId);

        // Fcm
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                MainActivity.fcmId = instanceIdResult.getToken();
                Log.d("SplashActivity", "fcmId : " + MainActivity.fcmId );
                checkServerStatus();

            }
        });

    }

    // 서버 상태 체크
    private void checkServerStatus () {
        StringBuffer strbuf = new StringBuffer();
        String serverStatus = null;

        // https://dnmart.co.kr/check_server.php
        new Thread(() -> {
            try {
                String serverStatus1 = RomarketUtil.httpConnect(Constant.serverUrl + Constant.checkServerUrl, null );
                Log.d("SplashActivity:Thread", "serverStatus : " + serverStatus1);
                Bundle bun = new Bundle();
                bun.putString("SERVER_STATUS", serverStatus1);
                Message msg = handler.obtainMessage();
                msg.setData(bun );
                handler.sendMessage(msg );

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("SplashActivity", "exception : " + e.getMessage() );

                // Error Dialog 호출

            }
        }).start();
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

    // Thread Message
    Handler handler = new Handler(Looper.getMainLooper() ) {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String serverStatus = bun.getString("SERVER_STATUS");
            Log.d("SplashActivity:Handler", "serverStatus : " + serverStatus );

            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(serverStatus );
                String code = jsonObject.getString("code" );
                String message = jsonObject.getString("msg" );
                Log.d("SplashActivity:Handler", "code : " + code );
                Log.d("SplashActivity:Handler", "message : " + message );

                if(Constant.serverStatusSuccess.equals(code) ) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class );
                    startActivity(intent );
                    finish();

                } else {
                    CustomDialog customDialog = new CustomDialog(SplashActivity.this );
                    customDialog.setTitle("알림");
                    customDialog.setMessage(message );
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

            } catch (Exception e) {

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

        }
    };

}



