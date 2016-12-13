package com.hypersocket.auth.cipher;

public interface CipherStrategy {

    String BCRYPT_QUALIFIER= "BCRYPT";
    String PBKDF2_256_QUALIFIER= "PBKDF2-SHA-256";
    String PBKDF2_384_QUALIFIER= "PBKDF2-SHA-384";
    String PBKDF2_512_QUALIFIER= "PBKDF2-SHA-512";

    String getEncoded(char[] value, int iterations);
    String getEncoded(char[] value, byte[] salt, int iterations);
    boolean match(char[] ciphered, char[] value);
}
