package com.hypersocket.auth.cipher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CipherStrategySpringConfiguration {

    public static final int KEY_LENGTH = 160;

    @Qualifier(CipherStrategy.BCRYPT_QUALIFIER)
    @Bean(name = CipherStrategy.BCRYPT_QUALIFIER)
    public CipherStrategy bcrypt(){
        return new BCryptCipherStrategy();
    }

    @Qualifier(CipherStrategy.PBKDF2_256_QUALIFIER)
    @Bean(name = CipherStrategy.PBKDF2_256_QUALIFIER)
    public CipherStrategy pbkdf2Sha256(){
       return new PBKDF2CipherStrategy(KEY_LENGTH, PBKDF2CipherStrategy.Algorithm.SHA_256);
    }

    @Qualifier(CipherStrategy.PBKDF2_384_QUALIFIER)
    @Bean(name = CipherStrategy.PBKDF2_384_QUALIFIER)
    public CipherStrategy pbkdf2Sha384(){
        return new PBKDF2CipherStrategy(KEY_LENGTH, PBKDF2CipherStrategy.Algorithm.SHA_384);
    }

    @Qualifier(CipherStrategy.PBKDF2_512_QUALIFIER)
    @Bean(name = CipherStrategy.PBKDF2_512_QUALIFIER)
    public CipherStrategy pbkdf2Sha512(){
        return new PBKDF2CipherStrategy(KEY_LENGTH, PBKDF2CipherStrategy.Algorithm.SHA_512);
    }
}
