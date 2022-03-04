package kr.co.romarket.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import kr.co.romarket.ImagePopupActivity;
import kr.co.romarket.MainActivity;
import kr.co.romarket.PushPopupActivity;
import kr.co.romarket.R;
import kr.co.romarket.common.RomarketUtil;
import kr.co.romarket.common.ThreadTask;
import kr.co.romarket.config.Constant;

public class RomarketFcmService extends FirebaseMessagingService {

    public static String CHANNEL_ID = "ROMARKET";
    public static String CHANNEL_NAME = "ROMARKET";
    NotificationManager manager;
    NotificationCompat.Builder builder;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Token을 서버로 전송
        Log.d("RomarketFcmService", "onNewToken:token : " + token );
        sendNewToken(token );
    }

    public void sendNewToken (String token ) {
        new ThreadTask<String, String>() {
            @Override
            protected void onPreExecute() {

            }

            @Override
            protected String doInBackground(String arg) {
                String setResult = null;
                StringBuffer urlBuf = new StringBuffer();
                urlBuf.append(Constant.serverUrl );
                urlBuf.append(Constant.setInfoUrl );

                // Gcm
                MainActivity.andId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

                try {
                    PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    int versionCode = pinfo.versionCode;
                    MainActivity.versionNumber = versionCode;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                urlBuf.append("?").append("dvKind=").append("android" );
                urlBuf.append("&").append("groupId=").append("" );
                urlBuf.append("&").append("dvId=").append("" );
                urlBuf.append("&").append("andId=").append(MainActivity.andId );
                urlBuf.append("&").append("fcmId=").append(token );
                urlBuf.append("&").append("appVer=").append(MainActivity.versionNumber );

                // MainActivity.pShopSeq = "2";
                if(StringUtils.isNotEmpty(MainActivity.pShopSeq) ) {
                    urlBuf.append("&").append("shopSeq=").append(MainActivity.pShopSeq );
                }

                try {
                    Log.d("RomarketFcmService", "sendNewToken:urlBuf : " + urlBuf.toString() );
                    setResult = RomarketUtil.httpConnect(urlBuf.toString() , null );
                    Log.d("RomarketFcmService", "sendNewToken:setResult : " + setResult );
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("RomarketFcmService", "sendNewToken:exception : " + e.getMessage() );
                }

                return setResult;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d("RomarketFcmService", "sendNewToken:onPostExecute : " + result );

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result );
                    String dvId = jsonObject.getString("dvId" );
                    String shopSeq = jsonObject.getString("shopSeq" );
                    Log.d("RomarketFcmService", "sendNewToken:dvId : " + dvId );
                    Log.d("RomarketFcmService", "sendNewToken:shopSeq : " + shopSeq );

                    if(StringUtils.isNotEmpty(dvId) ) {

                    } else {

                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

        }.execute("");
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // 수신한 메시지를 처리
        String jsonMsg = "";
        Map<String,String> fcmData = remoteMessage.getData();

        for( Map.Entry<String, String> it : fcmData.entrySet( ) ) {
            jsonMsg = it.getValue( );
        }

        String fcmMsg = null;
        String fcmMsgKind = null;
        String fcmConnUrl = null;
        String fcmShopSeq = null;
        String fcmShopName = null;
        String fcmTitle = null;

        try {
            JSONObject jresponse = new JSONObject(jsonMsg);
            fcmMsg = jresponse.getString("body");
            fcmMsgKind = jresponse.getString("kind");
            fcmConnUrl = jresponse.getString("url");
            fcmShopSeq = jresponse.getString("shopseq");
            fcmShopName = jresponse.getString("shopname");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d("RomarketFcmService", "onMessageReceived:fcmMsg : " + fcmMsg );
        Log.d("RomarketFcmService", "onMessageReceived:fcmMsgKind : " + fcmMsgKind );
        Log.d("RomarketFcmService", "onMessageReceived:fcmConnUrl : " + fcmConnUrl );
        Log.d("RomarketFcmService", "onMessageReceived:fcmShopSeq : " + fcmShopSeq );
        Log.d("RomarketFcmService", "onMessageReceived:fcmShopName : " + fcmShopName );

        Log.d("RomarketFcmService", "onMessageReceived:MainActivity.fcmId : " + MainActivity.fcmId );
        Log.d("RomarketFcmService", "onMessageReceived:MainActivity.andId : " + MainActivity.andId );
        Log.d("RomarketFcmService", "onMessageReceived:MainActivity.versionNumber : " + MainActivity.versionNumber );

        fcmTitle = "";
        if("IMGPOPUP".equals(fcmMsgKind) ) {
            // fcmTitle = fcmShopName + " 할인정보가 도착했습니다.";
            fcmTitle = "할인정보가 도착했습니다";
            fcmMsg = String.format("[%s] 할인정보", fcmShopName);
        } else {
            fcmTitle = "주문확인";
        }

        showTopNoti(fcmTitle, fcmMsg, fcmConnUrl, fcmMsgKind, fcmShopSeq );

        // 앱이 실행중 인지에 따라
        Log.d("RomarketFcmService", "onMessageReceived:MainActivity.isBackGround : " + MainActivity.isBackGround);

        if(MainActivity.isBackGround == false ) {
            Log.d("RomarketFcmService", "onMessageReceived:PushPopupActivity : " );

            if ("IMGPOPUP".equals(fcmMsgKind )) {
                Intent imgPop = new Intent(MainActivity.mainActivityContext, ImagePopupActivity.class);
                imgPop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                imgPop.putExtra("msg", fcmMsg);
                imgPop.putExtra("connUrl", fcmConnUrl);
                imgPop.putExtra("msgKind", fcmMsgKind);
                imgPop.putExtra("shopSeq", fcmShopSeq);
                imgPop.putExtra("shopName", fcmShopName);
                imgPop.putExtra("isSound", "Y");

                startActivity(imgPop);
            } else {
                Intent pushPop = new Intent(MainActivity.mainActivityContext, PushPopupActivity.class );
                pushPop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                pushPop.putExtra("msg", fcmMsg );
                pushPop.putExtra("connUrl", fcmConnUrl );
                pushPop.putExtra("msgKind", fcmMsgKind );
                pushPop.putExtra("shopSeq", fcmShopSeq );
                pushPop.putExtra("shopName", fcmShopName );
                pushPop.putExtra("isSound", "Y" );

                startActivity(pushPop );
            }

        } else {

        }

    }

    private void showTopNoti (String title, String msg, String url, String msgKind, String shopSeq ) {
        builder = null;
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //버전 오레오 이상일 경우
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel( new NotificationChannel(this.CHANNEL_ID, this.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT) );
            builder = new NotificationCompat.Builder(this, this.CHANNEL_ID);
            //하위 버전일 경우
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        // Notification Action Add
        Intent intent = new Intent(getApplicationContext(), MainActivity.class );
        if("IMGPOPUP".equals(msgKind) ) {
            intent.putExtra("pageCode", Constant.PAGE_CODE_EVENT );
        } else {
            intent.putExtra("pageCode", Constant.PAGE_CODE_MAIN );
        }
        if(StringUtils.isNotEmpty(shopSeq) ) {
            intent.putExtra("shopSeq", shopSeq );
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 101, intent, PendingIntent.FLAG_UPDATE_CURRENT );

        // 알림창 제목
        builder.setContentTitle(title );
        // 알림창 메시지
        builder.setContentText(msg );
        // 알림창 아이콘
        builder.setSmallIcon(R.drawable.push_icon );
        // 알림창 클릭시 삭제
        builder.setAutoCancel(true );
        // 알림창 클릭시 인텐트 전달
        builder.setContentIntent(pendingIntent );

        if(StringUtils.isNotEmpty(url) && "IMGPOPUP".equals(msgKind) ) {
            Bitmap icon = RomarketUtil.getBitmapFromURL(url);
            builder.setLargeIcon(icon );
            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(icon).bigLargeIcon(null) );
        }

        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg)) ;

        Notification notification = builder.build();
        //알림창 실행
        manager.notify(1, notification);

    }


}
