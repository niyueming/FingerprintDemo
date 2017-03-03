package net.nym.fingerprintdemo;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import net.nym.fingerprintlibrary.FingerPrintUtils;
import net.nym.fingerprintlibrary.dialog.FingerprintDialog;

import static android.Manifest.permission.USE_FINGERPRINT;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = (ImageView) findViewById(R.id.imageView2);
//        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
//        animationDrawable.start();
                imageView.setImageResource(R.drawable.cycler);
                AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
                animationDrawable.start();
            }
        },2000);

    }

    public void onClick(View view){
        if (!FingerPrintUtils.isKeyguardSecure(this)){
            Toast.makeText(this,"请在设置界面中开启密码锁屏功能",Toast.LENGTH_LONG).show();
            return;
        }
        if (!FingerPrintUtils.hasEnrolledFingerprints(this)){
            Toast.makeText(this,"您还没有录入指纹, 请在设置界面录入至少一个指纹",Toast.LENGTH_LONG).show();
            return;
        }
        ActivityCompat.requestPermissions(this,new String[]{USE_FINGERPRINT},0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = true;
        for (int i = 0 ,size = grantResults.length;i < size;i ++){
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                isGranted = false;
                break;
            }
        }

        if (isGranted){

            FingerprintDialog dialog = new FingerprintDialog(this);
            dialog.setOnClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("onClick","DialogInterface");
                }
            });
            dialog.setOnFingerPrintCallback(new FingerprintDialog.OnFingerPrintCallback() {
                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    Log.e("onAuthenticationSucceeded",result.toString());
                }
            });
            dialog.show();
        }
    }
}
