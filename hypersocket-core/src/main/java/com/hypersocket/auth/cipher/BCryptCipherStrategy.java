package com.hypersocket.auth.cipher;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class BCryptCipherStrategy extends AbstractCipherStrategy implements CipherStrategy{

    private static final int BCRYPT_SALT_LEN = 16;

    @Override
    public String getEncoded(char[] value, int iterations) {
        return getEncoded(value, genSalt(BCRYPT_SALT_LEN), iterations);
    }

    @Override
    public String getEncoded(char[] value, byte[] salt, int iterations) {
        return OpenBSDBCrypt.generate(value,  salt, iterations);
    }

    @Override
    public boolean match(char[] ciphered, char[] value) {
        return OpenBSDBCrypt.checkPassword(new String(ciphered), value);
    }
}
