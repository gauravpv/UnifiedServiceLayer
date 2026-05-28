package com.bajaj.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class EncryptionAspect {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY = ")H@McQfTjWnZr4u7x!A&C*F-JaNdRgUk";
    private static final String IV  = "w9z$C&F)J@NcRfUj";

    private EncryptionAspect() {}

    public static String encrypt(String plaintext) throws Exception {
        Cipher cipher = cipher(Cipher.ENCRYPT_MODE);
        byte[] out = cipher.doFinal(plaintext.replace('+', '~').getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(out);
    }

    public static String decrypt(String ciphertext) throws Exception {
        Cipher cipher = cipher(Cipher.DECRYPT_MODE);
        byte[] decoded = Base64.getDecoder().decode(ciphertext.replace('~', '+'));
        return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
    }

    private static Cipher cipher(int mode) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode,
                new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES"),
                new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
        return cipher;
    }
}
