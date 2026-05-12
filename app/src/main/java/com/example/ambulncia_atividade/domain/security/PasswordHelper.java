package com.example.ambulncia_atividade.domain.security;

import android.util.Base64;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHelper {

    // Aumentamos o Salt para 32 bytes para maior entropia
    private static final int SALT_LENGTH = 32;
    // 10.000 iterações: deixa o brute-force inviável, mas é rápido o suficiente pro celular
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    public static String hashPassword(String password, String saltBase64) {
        try {
            // Decodifica o salt de volta para bytes
            byte[] saltBytes = Base64.decode(saltBase64, Base64.NO_WRAP);

            // Configura o PBKDF2 com HMAC SHA-256
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            // Gera o hash forte
            byte[] hashBytes = skf.generateSecret(spec).getEncoded();

            // Limpa a senha da memória (Boa prática contra memory dump)
            spec.clearPassword();

            return Base64.encodeToString(hashBytes, Base64.NO_WRAP);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Falha crítica na criptografia da senha", e);
        }
    }

    public static boolean checkPassword(String plain, String hash, String salt) {
        return hashPassword(plain, salt).equals(hash);
    }
}