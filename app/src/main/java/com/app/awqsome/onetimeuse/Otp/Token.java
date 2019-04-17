package com.app.awqsome.onetimeuse.Otp;

import android.net.Uri;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import org.jboss.aerogear.security.otp.api.Base32;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
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
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Token {

    public static class TokenUriInvalidException extends Exception {
        private static final long serialVersionUID = -1108624734612362345L;
    }

    private static char[] STEAMCHARS = new char[] {
            '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C',
            'D', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q',
            'R', 'T', 'V', 'W', 'X', 'Y'};

    private static String algo = "sha1";
    private static final String TAG = "Token";
    //private static final String HOTP = "hotp";
    private static final String TOTP = "totp";
    private static final String OTP_AUTH = "otpauth";
    private static final int PERIOD = 30;
    private static final int DIGITS = 6;

    private String label;
    private String issuer;
    private byte[] secret;
    private String secret2;
    private HashMap<String, byte[]> map;

    //ADD TOKENTYPE

    public Token(Uri uri) throws TokenUriInvalidException {
        validateTokenURI(uri);

        String path = uri.getPath();
        path = path.replaceFirst("/", "");
        if(path.length() == 0) throw new TokenUriInvalidException();

        int i = path.indexOf(":");

        //FIRST ; LABEL = ISSUER
        label = path.substring(i >= 0 ? i + 1 : 0);
        issuer = i < 0 ? "" : path.substring(0, i);

        algo = algo.toUpperCase(Locale.US);
        try {
            Mac.getInstance("Hmac" + algo);
        } catch (NoSuchAlgorithmException e) {
            throw new TokenUriInvalidException();
        }

        //I WILL ADD THIS LATER
        /*
        try {
            String d = uri.getQueryParameter("digits");
            if(d == null) d = "6";
            digits = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            throw new TokenUriInvalidException();
        }*/

        //ADD PERIOD

        try {
            String s = uri.getQueryParameter("secret");
            secret = Base32.decode(s);
        } catch (Base32.DecodingException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        secret2 = uri.getQueryParameter("secret");
        Log.d(TAG, "secret : " + Arrays.toString(secret) + " label : " + label + " issuer : " + issuer);
    }

    public Token(String text) throws TokenUriInvalidException {
        this(Uri.parse(text));
    }

    private void validateTokenURI(Uri uri) throws TokenUriInvalidException {

        if(uri == null) throw new TokenUriInvalidException();

        if(uri.getScheme() == null || !uri.getScheme().equals(OTP_AUTH)) throw new TokenUriInvalidException();

        if(uri.getAuthority() == null) throw new TokenUriInvalidException();

        //ADD HOTP LATER
        if(!uri.getAuthority().equals(TOTP)) throw new TokenUriInvalidException();

        if(uri.getPath() == null) throw new TokenUriInvalidException();
    }

    public TokenCode generateCodes() {
        long curr = System.currentTimeMillis();

        //ADD HOTP TOTP SWITCH

        long counter = curr / 1000 / PERIOD;
        Log.d(TAG, "HOTP : " + getHOTP(counter + 1));
        return new TokenCode(getHOTP(counter + 0),
                (counter + 0) * PERIOD * 1000,
                (counter + 1) * PERIOD * 1000,
                new TokenCode(getHOTP(counter + 1),
                        (counter + 1) * PERIOD * 1000,
                        (counter + 2) * PERIOD * 1000));
    }

    private String getHOTP(long counter) {
        // Encode counter in network byte order
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(counter);

        // Create digits divisor
        int div = 1;
        for (int i = DIGITS; i > 0; i--)
            div *= 10;

        // Create the HMAC
        try {
            Mac mac = Mac.getInstance("Hmac" + algo);
            mac.init(new SecretKeySpec(secret, "Hmac" + algo));

            // Do the hashing
            byte[] digest = mac.doFinal(bb.array());

            // Truncate
            int binary;
            int off = digest[digest.length - 1] & 0xf;
            binary = (digest[off] & 0x7f) << 0x18;
            binary |= (digest[off + 1] & 0xff) << 0x10;
            binary |= (digest[off + 2] & 0xff) << 0x08;
            binary |= (digest[off + 3] & 0xff);

            String hotp = "";
            if (issuer.equals("Steam")) {
                for (int i = 0; i < DIGITS; i++) {
                    hotp += STEAMCHARS[binary % STEAMCHARS.length];
                    binary /= STEAMCHARS.length;
                }
            } else {
                binary = binary % div;

                // Zero pad
                hotp = Integer.toString(binary);
                while (hotp.length() != DIGITS)
                    hotp = "0" + hotp;
            }

            return hotp;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getID() {
        String id;
        if (issuer != null && !issuer.equals("")) id = issuer + ":" + label;
        else if (issuer != null && !issuer.equals("")) id = issuer + ":" + label;
        else id = label;
        return id;
    }

    ///

    public String getSecret2() {
        return secret2;
    }

    public void saveKeyStore(String alias, String secret) {
        map = new HashMap<>();
        byte[] iv, encrypted;
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
            encrypted = cipher.doFinal(secret.getBytes("UTF-8"));

            map.put("encrypted", encrypted);
            map.put("iv", iv);

            Log.d(TAG, " iv : " + Arrays.toString(iv) + " encrypted code : " + Arrays.toString(encrypted) + " hashmap : " + map.toString());
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
    }

    public String getDecryptedKey(String alias) {
        String unEncryptedData = null;
        byte[] iv = map.get("iv");
        byte[] encrypted = map.get("encrypted");
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
                    .getEntry(alias, null);
            final SecretKey secretKey = secretKeyEntry.getSecretKey();

            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            final byte[] decodedData = cipher.doFinal(encrypted);
            unEncryptedData = new String(decodedData, "UTF-8");
            Log.d(TAG, "getDecryptedKey: " +  ((KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null)).getSecretKey().toString());
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

    public HashMap<String, byte[]> getMap() {
        return map;
    }

    public void setMap(HashMap<String, byte[]> map) {
        this.map = map;
    }
}
