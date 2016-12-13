package com.hypersocket.auth.cipher;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public abstract class AbstractCipherStrategy {

    protected byte[] genSalt(int length) {
        try {
            SecureRandom salt = SecureRandom.getInstance("SHA1PRNG");
            byte rnd[] = new byte[length];
            salt.nextBytes(rnd);
            return rnd;
        }catch (NoSuchAlgorithmException e){
            throw new IllegalStateException(e);
        }
    }
}
