package kr.co.romarket.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import kr.co.romarket.MainActivity;
import kr.co.romarket.PushPopupActivity;
import kr.co.romarket.R;

public class RomarketFcmService extends FirebaseMessagingService {

    public static String CHANNEL_ID = "ROMARKET";
    public static String CHANNEL_NAME = "ROMARKET";
    NotificationManager manager;
    NotificationCompat.Builder builder;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Token을 서버로 전송

        Log.d("RomarketFcmService:onNewToken", "token : " + token );

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

        Log.d("RomarketFcmService:onMessageReceived", "fcmMsg : " + fcmMsg );
        Log.d("RomarketFcmService:onMessageReceived", "fcmMsgKind : " + fcmMsgKind );
        Log.d("RomarketFcmService:onMessageReceived", "fcmConnUrl : " + fcmConnUrl );
        Log.d("RomarketFcmService:onMessageReceived", "fcmShopSeq : " + fcmShopSeq );
        Log.d("RomarketFcmService:onMessageReceived", "fcmShopName : " + fcmShopName );

        Log.d("RomarketFcmService:onMessageReceived", "MainActivity.fcmId : " + MainActivity.fcmId );
        Log.d("RomarketFcmService:onMessageReceived", "MainActivity.andId : " + MainActivity.andId );
        Log.d("RomarketFcmService:onMessageReceived", "MainActivity.versionNumber : " + MainActivity.versionNumber );

        showTopNoti("title", fcmMsg );

        // 앱이 실행중 인지에 따라
        Log.d("RomarketFcmService:onMessageReceived", "MainActivity.isBackGround : " + MainActivity.isBackGround);

        if(MainActivity.isBackGround == false ) {
            Log.d("RomarketFcmService:onMessageReceived", "PushPopupActivity : " );

            Intent pushPop = new Intent(MainActivity.mainActivityContext, PushPopupActivity.class );
            pushPop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            pushPop.putExtra("msg", fcmMsg );
            pushPop.putExtra("conn_url", fcmConnUrl );
            pushPop.putExtra("msg_kind", fcmMsgKind );
            pushPop.putExtra("shop_seq", fcmShopSeq );
            pushPop.putExtra("shop_name", fcmShopName );
            pushPop.putExtra("is_sound", "Y" );

            startActivity(pushPop );

        } else {

        }

    }

    private void showTopNoti (String title, String msg ) {
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

        Intent intent = new Intent(getApplicationContext(), MainActivity.class );
        intent.putExtra("name", "test");
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

        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg)) ;

        Notification notification = builder.build();
        //알림창 실행
        manager.notify(1, notification);

    }


}
