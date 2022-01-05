package kr.co.romarket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ZoomActivity extends AppCompatActivity {

    public static float maxTextureSize = 4000;
    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        int[] arrMaxTextureSize = new int[1];
        GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, arrMaxTextureSize, 0);
        Log.d("imgpopuptest", "maxTextureSize[0]="+arrMaxTextureSize[0]);
        if (arrMaxTextureSize[0] == 0) {
            Log.d("imgpopuptest", "인식못함, 재시도");
            GLES10.glGetIntegerv(GL11.GL_MAX_TEXTURE_SIZE, arrMaxTextureSize, 0);
        }
        if (arrMaxTextureSize[0] == 0) arrMaxTextureSize[0] = 4000;
        this.maxTextureSize = arrMaxTextureSize[0];

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom );  // layout xml 과 자바파일을 연결

        new DownloadImageTask((ImageView) findViewById(R.id.zoom_image)).execute(MainActivity.mZoomImgSrc );
        setContentView(R.layout.activity_zoom );

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

            float img_width = result.getWidth();
            float img_height = result.getHeight();
            float new_img_width = 0;
            float new_img_height = 0;
            float resize_base = 0;

            float base_size = ZoomActivity.maxTextureSize;
            if (base_size < 4000) {
                base_size = 4000;
            }

            if (img_width > base_size || img_height > base_size){
                //고행사도 이미지는 Bitmap too large to be uploaded into a texture 오류발생...
                base_size = base_size -10;
                if (img_width > img_height) {
                    //가로가 큰 경우 가로를 기준으로 리사이즈
                    resize_base = base_size / img_width;
                    new_img_width = base_size;
                    new_img_height = img_height * resize_base;
                } else {
                    //세로가 큰 경우
                    resize_base = base_size / img_height;
                    new_img_height = base_size;
                    new_img_width = img_width * resize_base;
                }
                Bitmap resized = null;
                resized = Bitmap.createScaledBitmap(result, (int)new_img_width , (int)new_img_height, false);

                ImageView zoom_img = (ImageView)findViewById(R.id.zoom_image);
                zoom_img.setBackgroundColor(Color.parseColor("#000000"));
                zoom_img.setImageBitmap(resized);
                mAttacher = new PhotoViewAttacher(zoom_img);
                // 3.화면에 꽉차는 옵션 (선택사항)
                mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                ImageView zoom_img = (ImageView)findViewById(R.id.zoom_image);
                zoom_img.setBackgroundColor(Color.parseColor("#000000"));
                zoom_img.setImageBitmap(result);
                mAttacher = new PhotoViewAttacher(zoom_img);
                // 3.화면에 꽉차는 옵션 (선택사항)
                mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean call_goback = true;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            // overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



}
