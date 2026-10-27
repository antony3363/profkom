package com.example.auth_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Генерирует RSA-пару при старте сервиса (эфемерная, in-memory). Известное
 * ограничение MVP: перезапуск сервиса инвалидирует все ранее выданные access-токены,
 * т.к. ключ меняется. Для продакшена ключ нужно персистировать (например, в Vault/
 * секрет-хранилище), а не генерировать заново на каждый старт.
 */
@Component
public class JwtKeyProvider {

//    private final KeyPair keyPair;
//
//    public JwtKeyProvider() {
//        try {
//            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
//            generator.initialize(2048);
//            this.keyPair = generator.generateKeyPair();
//        } catch (NoSuchAlgorithmException e) {
//            throw new IllegalStateException("RSA algorithm not available", e);
//        }
//    }
//
//    public KeyPair getKeyPair() {
//        return keyPair;
//    }



    private final KeyPair keyPair;
    public JwtKeyProvider(@Value("${jwt.private-key-path:}") String privateKeyPath,
                          @Value("${jwt.public-key-path:}") String publicKeyPath) {
        this.keyPair = (privateKeyPath.isBlank() || publicKeyPath.isBlank())
                ? generateEphemeral()
                : loadFromFiles(privateKeyPath, publicKeyPath);
    }
    public KeyPair getKeyPair() {
        return keyPair;
    }
    private KeyPair generateEphemeral() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA algorithm not available", e);
        }
    }
    private KeyPair loadFromFiles(String privateKeyPath, String publicKeyPath) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = factory.generatePrivate(
                    new PKCS8EncodedKeySpec(decodePem(privateKeyPath, "PRIVATE KEY")));
            PublicKey publicKey = factory.generatePublic(
                    new X509EncodedKeySpec(decodePem(publicKeyPath, "PUBLIC KEY")));
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JWT RSA key pair from " + privateKeyPath, e);
        }
    }
    private byte[] decodePem(String path, String label) throws Exception {
        String pem = Files.readString(Path.of(path))
                .replace("-----BEGIN " + label + "-----", "")
                .replace("-----END " + label + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(pem);
    }



}
