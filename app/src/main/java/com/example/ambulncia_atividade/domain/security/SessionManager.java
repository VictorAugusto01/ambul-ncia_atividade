package com.example.ambulncia_atividade.domain.security;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {
    private static final String PREF_NAME = "sos_leitos_secure_prefs";

    public static SharedPreferences getSecurePrefs(Context context) {
        try {
            // Cria a chave Mestra usando criptografia AES256-GCM
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Retorna o SharedPreferences blindado
            return EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Falha crítica ao gerar cofre de sessão", e);
        }
    }
    public static byte[] getDatabasePassphrase(Context context) {
        SharedPreferences prefs = getSecurePrefs(context);
        String key = prefs.getString("db_key", null);

        if (key == null) {
            // Gera uma chave de 256-bits (32 bytes) segura se ela não existir
            byte[] newKey = new byte[32];
            new java.security.SecureRandom().nextBytes(newKey);
            key = android.util.Base64.encodeToString(newKey, android.util.Base64.DEFAULT);
            prefs.edit().putString("db_key", key).apply();
        }

        return android.util.Base64.decode(key, android.util.Base64.DEFAULT);
    }
}

