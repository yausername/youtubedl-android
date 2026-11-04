package com.yausername.youtubedl_android_example;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Stores only this app's short-lived backend access token, encrypted with an Android Keystore key. */
public final class SessionManager {
    private static final String PREFS = "authorized_playback_session";
    private static final String KEY_ALIAS = "authorized_playback_access_token_v1";
    private static final String TOKEN_CIPHERTEXT = "access_token_ciphertext";
    private static final String TOKEN_IV = "access_token_iv";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final int GCM_TAG_BITS = 128;

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Call after your own login flow returns a backend access token. */
    public synchronized void saveAccessToken(String accessToken) throws GeneralSecurityException {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("accessToken must not be empty");
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] ciphertext = cipher.doFinal(accessToken.getBytes(StandardCharsets.UTF_8));
        preferences.edit()
                .putString(TOKEN_IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
                .putString(TOKEN_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                .apply();
    }

    /** Returns null when no valid token is stored; corrupted/invalidated data is removed. */
    public synchronized String getAccessToken() {
        String encodedIv = preferences.getString(TOKEN_IV, null);
        String encodedCiphertext = preferences.getString(TOKEN_CIPHERTEXT, null);
        if (encodedIv == null || encodedCiphertext == null) return null;

        try {
            byte[] iv = Base64.decode(encodedIv, Base64.NO_WRAP);
            byte[] ciphertext = Base64.decode(encodedCiphertext, Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            String token = new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
            return token.trim().isEmpty() ? null : token;
        } catch (GeneralSecurityException | IllegalArgumentException error) {
            clear();
            return null;
        }
    }

    public synchronized void clear() {
        preferences.edit().remove(TOKEN_IV).remove(TOKEN_CIPHERTEXT).apply();
    }

    private SecretKey getOrCreateKey() throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            java.security.Key existingKey = keyStore.getKey(KEY_ALIAS, null);
            if (existingKey instanceof SecretKey) return (SecretKey) existingKey;

            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build();
            generator.init(spec);
            return generator.generateKey();
        } catch (java.io.IOException error) {
            throw new GeneralSecurityException("Unable to access Android Keystore", error);
        } catch (java.security.KeyStoreException | java.security.cert.CertificateException error) {
            throw new GeneralSecurityException("Unable to access Android Keystore", error);
        }
    }
}
