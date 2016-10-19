package com.hypersocket.auth.cipher;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

public class PBKDF2CipherStrategy extends AbstractCipherStrategy implements CipherStrategy{

    private static final int SALT_LEN = 16;

    private final int keyLength;
    private final Algorithm algorithm;

    public PBKDF2CipherStrategy(int keyLength, Algorithm algorithm){
        this.keyLength = keyLength;
        this.algorithm = algorithm;
    }

    @Override
    public String getEncoded(char[] value, int iterations) {
        return getEncoded(value, genSalt(SALT_LEN), iterations);
    }

    @Override
    public String getEncoded(char[] value, byte[] salt, int iterations) {
        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(getDigest());
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(value), salt, iterations);
        KeyParameter key = (KeyParameter)generator.generateDerivedMacParameters(this.keyLength);
        byte[] keyData = key.getKey();
        return String.format("%d][%s][%s", iterations,
                Base64.encodeBase64String(salt),
                Base64.encodeBase64String(keyData));
    }

    @Override
    public boolean match(char[] ciphered, char[] value) {
        String cipheredValue = new String(ciphered);
        String[] parts = cipheredValue.split("\\]\\[");
        Integer iterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.decodeBase64(parts[1]);

        String hash = getEncoded(value, salt, iterations);
        return hash.equals(cipheredValue);
    }

    public ExtendedDigest getDigest(){
        switch (this.algorithm){
            case SHA_256: return new SHA256Digest();
            case SHA_384: return new SHA384Digest();
            case SHA_512: return new SHA512Digest();
        }

        throw new IllegalStateException("Algorithm did not match any !");
    }

    public enum Algorithm{
        SHA_256, SHA_384, SHA_512;
    }
}
