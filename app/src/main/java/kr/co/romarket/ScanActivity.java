package kr.co.romarket;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanActivity extends AppCompatActivity {

    private IntentIntegrator barcodeScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        barcodeScan = new IntentIntegrator(this);
        barcodeScan.setOrientationLocked(false); // default가 세로모드인데 휴대폰 방향에 따라 가로, 세로로 자동 변경됩니다.
        barcodeScan.setPrompt("붉은선에 바코드를 맞춰주세요.");
        barcodeScan.setCameraId(0);
        barcodeScan.setBeepEnabled(true);
        barcodeScan.setBarcodeImageEnabled(false);
        barcodeScan.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null ) {
            if(result.getContents() == null) {
                // Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                finish();
            } else {
                // Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                Intent intent = new Intent();
                intent.putExtra("SCAN_BARCODE", result.getContents());
                intent.putExtra("BARCODE_TYPE", result.getFormatName());
                setResult(RESULT_OK, intent );
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}