package com.supportbot.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CodeGenerator {
    private static final String ALPH = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
    private final SecureRandom rnd = new SecureRandom();

    public String newPublicCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPH.charAt(rnd.nextInt(ALPH.length())));
        }
        return sb.toString();
    }
}