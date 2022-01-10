package kr.co.romarket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

public class PushPopupActivity extends AppCompatActivity  {

    public static String tmpConnUrl = null;
    public static String msgKind = null;
    public static String shopSeq = null;
    public static String shopName = null;
    private static Context popContext = null; //applicationContext
    boolean customTitleSupported;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("PushPopupActivity:onCreate", "push Pop" );
        /*
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD );
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //투명배경

        // 창이 나타나고 바닥은 검정색 반투명으로
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        */

        setContentView(R.layout.activity_popup);
        this.popContext = this;

        customTitleBar(getText(R.string.app_name).toString(), getText(R.string.title_ment).toString());

        // Data Load
        Bundle extras = getIntent().getExtras();
        String msg = extras.getString("msg");
        String isSound = extras.getString("is_sound");
        this.msgKind = extras.getString("msg_kind");
        this.shopSeq = extras.getString("shop_seq");
        this.shopName = extras.getString("shop_name");

        // 소리 및 진동 설정
        if ("Y".equals(isSound) ) {
            AudioManager clsAudioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            switch( clsAudioManager.getRingerMode() ) {
                case AudioManager.RINGER_MODE_VIBRATE :
                    // 진동 모드
                    Vibrator mVibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    // 1초 진동
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mVibe.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        mVibe.vibrate(1000);
                    }

                    break;

                case AudioManager.RINGER_MODE_NORMAL :
                    Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(),RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
                    ringtone.play();

                    break;

            }
        }


        if (StringUtils.isNotEmpty(this.shopName)) {
            this.shopName = "로마켓";
        }
        String[] arrMsg = msg.split("º");

        String newMsg = "";
        for(String line : arrMsg ) {
            newMsg += line + "\r\n";
        }

        if(newMsg != null && newMsg.length() > 200) {
            DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;

            int parent_layout_width = width - 50;
            int parent_layout_height = height - 100;

            RelativeLayout rl = (RelativeLayout) findViewById(R.id.parent_r_layout);
            rl.getLayoutParams().width = parent_layout_width;
            rl.getLayoutParams().height = parent_layout_height;
        }

        Log.d("PushPopupActivity:onCreate", "push Pop" );
        String tmpSubjectMsg = "[정보]".concat(this.shopName );
        TextView tvText = (TextView) findViewById(R.id.textView1);
        tvText.setMaxLines(100);
        tvText.setVerticalScrollBarEnabled(true);
        tvText.setMovementMethod(new ScrollingMovementMethod());
        tvText.setText(newMsg);
        TextView subject_bar=(TextView)findViewById(R.id.text_subject_bar);
        subject_bar.setText(tmpSubjectMsg);
        Log.d("PushPopupActivity:onCreate", "push Pop" );

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void customTitleBar(String left, String right) {
        if (right.length() > 20) {
            right = right.substring(0, 20);
        }
        // set up custom title
        if (customTitleSupported ) {
            getWindow().setFeatureInt(android.view.Window.FEATURE_CUSTOM_TITLE, R.layout.menu_title_layout );
            TextView titleTvLeft = (TextView) findViewById(R.id.titleTvLeft);
            titleTvLeft.setText(right);
        }
    }

    public void btnClick (View v) {

        if (v.getId() == R.id.button_confirm) { // || v.getId() == R.id.your_image) {
            // Intent i = new Intent(this.popContext, MainActivity.class);
            // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // i.putExtra("conn_url", "imgpopup" ); //
            // i.putExtra("noti_from", "imgpopup" );
            // i.putExtra("shop_seq", this.shopSeq );
            // startActivity(i);
            this.finish();

        } else if (v.getId() == R.id.button_cancel) {
            this.finish();
        }
    }


}
