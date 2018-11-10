package tgi.com.tgifreertobtdemo.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;

import java.lang.reflect.Member;

public class ProgressDialog {

    private static android.app.ProgressDialog progressDialog;

    public static void show(Context context, String title, String msg, boolean cancelable, @Nullable final Runnable onCancel){
        dismiss();
        progressDialog = android.app.ProgressDialog.show(context, title, msg, true, cancelable, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (onCancel != null) {
                    onCancel.run();
                }
            }
        });
    }

    public static void dismiss(){
        if(progressDialog!=null&&progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}
