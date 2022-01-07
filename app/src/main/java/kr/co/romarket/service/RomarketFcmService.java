package kr.co.romarket.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class RomarketFcmService extends FirebaseMessagingService {

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
        Log.d("RomarketFcmService:onNewToken", "remoteMessage : " + remoteMessage );

    }
}
