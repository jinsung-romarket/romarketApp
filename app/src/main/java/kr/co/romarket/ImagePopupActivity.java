package kr.co.romarket;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImagePopupActivity extends AppCompatActivity {

    public static String tmpShopSeq = null;
    public static String tmpTitleMsg = null;
    public static String tmpSubjectMsg = null;
    public static float maxTextureSize = 4000;
    public static Activity zoomImgActivity;
    public static Context zoomImgContext;
    private static Context imgPopContext = null; //applicationContext

    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("ImagePopupActivity:onCreate", "Image Pop" );

        int[] arrMaxTextureSize = new int[1];
        GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, arrMaxTextureSize, 0);
        Log.d("ImagePopupActivity:onCreate", "arrMaxTextureSize[0] : " + arrMaxTextureSize[0]);
        if (arrMaxTextureSize[0] == 0) {
            Log.d("ImagePopupActivity:onCreate", "인식못함, 재시도" );
            GLES10.glGetIntegerv(GL11.GL_MAX_TEXTURE_SIZE, arrMaxTextureSize, 0);
        }
        if (arrMaxTextureSize[0] == 0){
            arrMaxTextureSize[0] = 4000;
        }
        this.maxTextureSize = arrMaxTextureSize[0];

        imgPopContext = this;
        Bundle extras = getIntent().getExtras();
        String msg = extras.getString("msg");
        String connUrl = extras.getString("conn_url");
        String shopSeq = extras.getString("shop_seq");
        this.tmpShopSeq = shopSeq;
        String shopName = extras.getString("shop_name");
        String msgKind = extras.getString("msg_kind");
        String isSound = extras.getString("is_sound");

        if (StringUtils.isNotEmpty(shopName)) {
            this.tmpTitleMsg = shopName;
        } else {
            this.tmpTitleMsg = "[로마켓]";
        }
        this.tmpSubjectMsg = "[정보]".concat(msg );

        Log.d("ImagePopupActivity:onCreate", "connUrl : " + connUrl );
        Log.d("ImagePopupActivity:onCreate", "msg : " + msg );
        Log.d("ImagePopupActivity:onCreate", "shopSeq : " + shopSeq );
        Log.d("ImagePopupActivity:onCreate", "shopName : " + shopName );
        Log.d("ImagePopupActivity:onCreate", "this.tmpSubjectMsg : " + this.tmpSubjectMsg );
        Log.d("ImagePopupActivity:onCreate", "this.tmpTitleMsg : " + this.tmpTitleMsg );
        Log.d("ImagePopupActivity:onCreate", "isSound : " + isSound );

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

        setContentView(R.layout.activity_zoom );  // layout xml 과 자바파일을 연결
        new DownloadImageTask((ImageView) findViewById(R.id.zoom_image)).execute(connUrl );
        setContentView(R.layout.activity_zoom );

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {

            float imgWidth = result.getWidth();
            float imgHeight = result.getHeight();
            float newImgWidth = 0;
            float newImgHeight = 0;
            float resizeBase = 0;

            float baseSize = ZoomActivity.maxTextureSize;
            if (baseSize < 4000) {
                baseSize = 4000;
            }

            if (imgWidth > baseSize || imgHeight > baseSize){
                //고행사도 이미지는 Bitmap too large to be uploaded into a texture 오류발생...
                baseSize = baseSize -10;
                if (imgWidth > imgHeight) {
                    //가로가 큰 경우 가로를 기준으로 리사이즈
                    resizeBase = baseSize / imgWidth;
                    newImgWidth = baseSize;
                    newImgHeight = imgHeight * resizeBase;
                } else {
                    //세로가 큰 경우
                    resizeBase = baseSize / imgHeight;
                    newImgHeight = baseSize;
                    newImgWidth = imgWidth * resizeBase;
                }
                Bitmap resized = null;
                resized = Bitmap.createScaledBitmap(result, (int)newImgWidth , (int)newImgHeight, false);

                ImageView zoom_img = (ImageView)findViewById(R.id.zoom_image);
                zoom_img.setBackgroundColor(Color.TRANSPARENT );
                zoom_img.setImageBitmap(resized);
                mAttacher = new PhotoViewAttacher(zoom_img);
                // 3.화면에 꽉차는 옵션 (선택사항)
                mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                ImageView zoom_img = (ImageView)findViewById(R.id.zoom_image);
                zoom_img.setBackgroundColor(Color.TRANSPARENT );
                zoom_img.setImageBitmap(result);
                mAttacher = new PhotoViewAttacher(zoom_img);
                // 3.화면에 꽉차는 옵션 (선택사항)
                mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
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
