package net.nym.fingerprintdemo;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import net.nym.fingerprintlibrary.FingerPrintUtils;
import net.nym.fingerprintlibrary.dialog.FingerprintDialog;
import net.nym.fingerprintlibrary.keystore.LocalAndroidKeyStore;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import static android.Manifest.permission.USE_FINGERPRINT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String IV = "12345678";
    private final String data = "敖包3456";
    private String secretKey;
    private CancellationSignal cancellationSignal;
    LocalAndroidKeyStore encryptLocalAndroidKeyStore;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_encrypt).setOnClickListener(this);
        findViewById(R.id.btn_decrypt).setOnClickListener(this);
        cancellationSignal = new CancellationSignal();

        encryptLocalAndroidKeyStore = new LocalAndroidKeyStore();
        encryptLocalAndroidKeyStore.generateKey(LocalAndroidKeyStore.keyName);

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClick(View view){
        switch (view.getId()){
            case R.id.imageView:
                if (!FingerPrintUtils.isKeyguardSecure(this)){
                    Toast.makeText(this,"请在设置界面中开启密码锁屏功能",Toast.LENGTH_LONG).show();
                    return;
                }
                if (!FingerPrintUtils.hasEnrolledFingerprints(this)){
                    Toast.makeText(this,"您还没有录入指纹, 请在设置界面录入至少一个指纹",Toast.LENGTH_LONG).show();
                    return;
                }
                ActivityCompat.requestPermissions(this,new String[]{USE_FINGERPRINT},0);
                break;
            case R.id.btn_encrypt:
                encrypt();

                break;
            case R.id.btn_decrypt:
                decrypt();
                break;
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void encrypt() {
        FingerprintManager.CryptoObject cryptoObject = encryptLocalAndroidKeyStore.getCryptoObject(
                Cipher.ENCRYPT_MODE
                , null
        );
        FingerPrintUtils.authenticate(this, cryptoObject, cancellationSignal, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                FingerprintManager.CryptoObject cryptoObject1 = result.getCryptoObject();
                if (cryptoObject1 != null){
                    Cipher cipher = cryptoObject1.getCipher();
                    try {
                        byte[] encrypted = cipher.doFinal(data.getBytes());
                        byte[] IV = cipher.getIV();
                        String se = Base64.encodeToString(encrypted, Base64.URL_SAFE);
                        String siv = Base64.encodeToString(IV, Base64.URL_SAFE);
                        MainActivity.this.IV = siv;
                        secretKey = se;
                        Log.e("encrypt","se=" + se + ",siv=" + siv);
                    } catch (BadPaddingException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void decrypt() {
        FingerprintManager.CryptoObject cryptoObject = encryptLocalAndroidKeyStore.getCryptoObject(
                Cipher.DECRYPT_MODE
                , Base64.decode(IV, Base64.URL_SAFE)
        );
        FingerPrintUtils.authenticate(this, cryptoObject, cancellationSignal, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                FingerprintManager.CryptoObject cryptoObject1 = result.getCryptoObject();
                if (cryptoObject1 != null){
                    Cipher cipher = cryptoObject1.getCipher();
                    try {
                        byte[] decrypted = cipher.doFinal(Base64.decode(secretKey, Base64.URL_SAFE));
                        Log.e("decrypted","decrypted=" + new String(decrypted) );
                    } catch (BadPaddingException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cancellationSignal != null){
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
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
                    Log.e("onAuthentication",result.toString());
                }
            });
            dialog.show();
        }
    }
}
