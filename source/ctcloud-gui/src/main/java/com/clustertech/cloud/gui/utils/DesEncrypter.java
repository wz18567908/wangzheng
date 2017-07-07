package com.clustertech.cloud.gui.utils;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

final public class DesEncrypter {
    private Logger logger = Logger.getLogger(this.getClass());
    private static DesEncrypter desEncrypter = null;
    private Cipher ecipher = null;
    private Cipher dcipher = null;

    public DesEncrypter(String passPhrase) {
        try {
            KeySpec keySpec = new DESKeySpec(passPhrase.getBytes());
            SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);

            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");

            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "DesEncrypter", passPhrase));
        }
    }

    public String encrypt(String str) {
        try {
            byte[] utf8 = str.getBytes("UTF8");
            byte[] enc = ecipher.doFinal(utf8);
            return new String(Base64.encodeBase64(enc));
        }
        catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "encrypt", str));
        }
        return null;
    }

    public String decrypt(String str) {
        try {
            byte[] dec = Base64.decodeBase64(str);
            byte[] utf8 = dcipher.doFinal(dec);
            return new String(utf8, "UTF8");
        }
        catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "encrypt", str));
        }
        return null;
    }

    public synchronized static DesEncrypter getInstance() {
        if (desEncrypter == null) {
            desEncrypter = new DesEncrypter(".-'/W2@ce03=fky#vw6&H");
        }
        return desEncrypter;
    }
}
