package com.example.ambulncia_atividade.domain.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHelper {

    // Gera um "Salt" (um tempero aleatório) para garantir que senhas iguais tenham hashes diferentes
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        // Base64 é usado no Android a partir da API 26.
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    // Pega a senha limpa, junta com o Salt e gera o Hash SHA-256
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Mistura a senha com o salt
            String passwordWithSalt = password + salt;

            byte[] hashBytes = digest.digest(passwordWithSalt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash: Algoritmo não encontrado.", e);
        }
    }

    // Função para verificar no Login se a senha digitada bate com a do Banco
    public static boolean checkPassword(String plainPassword, String storedHash, String storedSalt) {
        String newHash = hashPassword(plainPassword, storedSalt);
        return newHash.equals(storedHash);
    }
}