package kr.co.romarket;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

public class CustomDialog {

    private Context context;
    private TextView titleTextView;
    private TextView messageTextView;
    public Button negativeButton;
    public Button positiveButton;
    private Dialog dlg;

    // titleTextView
    // messageTextView
    // negativeButton
    // positiveButton

    public CustomDialog (Context context ) {
        this.context = context;

        this.dlg = new Dialog(this.context );
        // 타이틀바 숨김
        this.dlg.requestWindowFeature(Window.FEATURE_NO_TITLE );
        this.dlg.setContentView(R.layout.custom_dialog );
        // 다이얼로그 밖 클릭시 다이얼로그 죽이지 않기.
        this.dlg.setCanceledOnTouchOutside(false );

        this.negativeButton = (Button) dlg.findViewById(R.id.negativeButton );
        this.positiveButton = (Button) dlg.findViewById(R.id.positiveButton );
        this.titleTextView = (TextView) dlg.findViewById(R.id.titleTextView );
        this.messageTextView = (TextView) dlg.findViewById(R.id.messageTextView );

        this.dlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public void setTitle (String title ) {
        if(StringUtils.isEmpty(title) ) {
            title = "Title";
        }
        this.titleTextView.setText(title );
    }

    public void setMessage (String msg ) {
        if(StringUtils.isEmpty(msg) ) {
            msg = "Message";
        }
        this.messageTextView.setText(msg );
    }

    public void setNegativeButtonText (String btnName ) {
        if(StringUtils.isEmpty(btnName) ) {
            btnName = "취소";
        }
        this.negativeButton.setText(btnName );
    }

    public void setPositiveButtonText (String btnName ) {
        if(StringUtils.isEmpty(btnName) ) {
            btnName = "확인";
        }
        this.positiveButton.setText(btnName );
    }

    public void showCustomDialog () {
        // 다이얼로그 생성
        Log.d("CustomDialog:showCustomDialog", "code : " );
        this.dlg.show();
    }

    public void dismissDialog () {
        this.dlg.dismiss();
    }

}
