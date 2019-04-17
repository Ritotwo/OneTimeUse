package com.app.awqsome.onetimeuse;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.app.awqsome.onetimeuse.Database.DatabaseHelperClass;
import com.app.awqsome.onetimeuse.Otp.Token;
import com.app.awqsome.onetimeuse.Otp.TokenPersistence;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class BarcodeScanner extends AppCompatActivity {

    private final static String TAG = "BarcodeScanner Activity";

    private DatabaseHelperClass db;

    SurfaceView cameraPreview;
    TextView tvResult;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    String lastBarcode;
    final int RequestCameraPermissionID = 1001;
    private boolean isCodeDetected = false;
    private byte[] iv;
    private byte[] encryptedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        db = new DatabaseHelperClass(this);
        cameraPreview = findViewById(R.id.camera_preview);
        tvResult = findViewById(R.id.tv_result);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();

        //Log.d(TAG, cameraSource.getPreviewSize().getHeight() + " : " + cameraSource.getPreviewSize().getWidth());

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    ActivityCompat.requestPermissions(BarcodeScanner.this, new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);
                    return;
                }

                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if(qrCodes.size() != 0) {
                    tvResult.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!isCodeDetected) {
                                Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(100);
                                tvResult.setText(qrCodes.valueAt(0).displayValue);
                                lastBarcode = qrCodes.valueAt(0).displayValue;
                                try {
                                    Token token = new Token(lastBarcode);
                                    token.generateCodes();
                                    if(!new TokenPersistence(BarcodeScanner.this).tokenExists(token)) {
                                        new TokenPersistence(BarcodeScanner.this).save(token);
                                        HashMap<String, byte[]> map = saveKeyStore(token.getID(), token.getSecret2());
                                        db.saveAccount(token.getIssuer(), token.getLabel(), map.get("encrypted"), map.get("iv"));
                                        byte[] ecData = db.getLastItem().getEncryptedData();
                                        byte[] ecIV = db.getLastItem().getEncryptediv();
                                        Log.d(TAG, "run: " + Arrays.toString(ecData) + Arrays.toString(ecIV));
                                        Log.d(TAG, "run: " + getDecryptedKey(token.getID(), ecData, ecIV));
                                    }
                                    //saveKeyStore(token.getSecret());
                                    Log.d(TAG, token.generateCodes().getCurrentCode());
                                    isCodeDetected = false;
                                } catch (Token.TokenUriInvalidException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(BarcodeScanner.this, MainActivity.class);
                                navigateUpToFromChild(BarcodeScanner.this, intent);
                            }
                        }
                    });
                }
            }
        });

        tvResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BarcodeScanner.this, MainActivity.class);
                navigateUpToFromChild(BarcodeScanner.this, intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    HashMap<String, byte[]> saveKeyStore(String alias, String secret) {
        HashMap<String, byte[]> map = new HashMap<>();
        try {
            final KeyGenerator keyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build();

            keyGenerator.init(keyGenParameterSpec);
            final SecretKey secretKey = keyGenerator.generateKey();
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            iv = cipher.getIV();
            encryptedData = cipher.doFinal(secret.getBytes("UTF-8"));

            map.put("encrypted", encryptedData);
            map.put("iv", iv);

            Log.d(TAG, " iv : " + Arrays.toString(iv) + " encrypted code : " + Arrays.toString(encryptedData));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "saveKeyStore: " + map.toString());
        return map;
    }

    String getDecryptedKey(String alias, byte[] encryptedData, byte[] encryptedIV) {
        String unEncryptedData = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
                    .getEntry(alias, null);
            final SecretKey secretKey = secretKeyEntry.getSecretKey();

            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec spec = new GCMParameterSpec(128, encryptedIV);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            final byte[] decodedData = cipher.doFinal(encryptedData);
            unEncryptedData = new String(decodedData, "UTF-8");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return unEncryptedData;
    }
}
