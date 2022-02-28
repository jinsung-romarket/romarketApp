package kr.co.romarket.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RomarketUtil {

    // http Connect
    public static String httpConnect (String urlStr, String dataStr) throws Exception {
        OutputStreamWriter wr = null;
        BufferedReader rd = null;
        String result = null;

        Log.d("httpConnect", "urlStr : " + urlStr );

        try {
            // Send data
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true );
            conn.setDoInput(true ); // 읽기모드 지정
            conn.setUseCaches(false ); // 캐싱데이터를 받을지 안받을지
            conn.setDefaultUseCaches(false ); // 캐싱데이터 디폴트 값 설정
            conn.setConnectTimeout(3000 );
            conn.setReadTimeout(5000 );

            if(dataStr != null && "".equals(dataStr) ) {
                wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(dataStr);
                wr.flush();
            }

            // Get the response
            result = "";
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = rd.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            try {
                if (rd != null) {
                    rd.close();
                }
                if (wr != null) {
                    wr.close();
                }
            } catch (Exception ioe) {

            }

            e.printStackTrace();
            throw new Exception("Server Check Error!");

        } finally {
            try {
                if (rd != null) {
                    rd.close();
                }
                if (wr != null) {
                    wr.close();
                }
            } catch (Exception ioe) {

            }

        }

        return result;
    }

    // Url -> bitmap
    public static Bitmap getBitmapFromURL(String src) {
        InputStream in = null;
        int responseCode = -1;
        Bitmap bitmap = null;
        URL url = null;
        try {
            url = new URL(src);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();
            responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                in = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
                in.close();
                httpURLConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }




}
