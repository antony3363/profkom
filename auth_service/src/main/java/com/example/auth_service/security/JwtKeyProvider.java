package com.example.auth_service.security;

import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Генерирует RSA-пару при старте сервиса (эфемерная, in-memory). Известное
 * ограничение MVP: перезапуск сервиса инвалидирует все ранее выданные access-токены,
 * т.к. ключ меняется. Для продакшена ключ нужно персистировать (например, в Vault/
 * секрет-хранилище), а не генерировать заново на каждый старт.
 */
@Component
public class JwtKeyProvider {

    private final KeyPair keyPair;

    public JwtKeyProvider() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            this.keyPair = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA algorithm not available", e);
        }
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
}
