package Utility;

import android.app.ProgressDialog;
import android.content.Context;

public class MyProgressBar {
    private static MyProgressBar mInstance;
    private Context mContext;
    private ProgressDialog progressDialog;

    public MyProgressBar(Context context) {
        mContext = context;
        progressDialog = new ProgressDialog(mContext);
    }

    public static synchronized  MyProgressBar newInstance(Context context) {
        if(mInstance == null) {
            mInstance = new MyProgressBar(context);
        }

        return mInstance;
    }

    public void setTitle(String msg) {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(mContext);
        }

        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
//        new android.os.Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                progressDialog.show();
//            }
//        },100);
    }

    public void dismissDialog() {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(mContext);
        }

        progressDialog.setCancelable(true);
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
