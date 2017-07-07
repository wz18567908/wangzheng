/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.commons;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

/**
 * This class encrypt a regular string to encrypted format and decrypt an
 * encrypted string to a regular string.
 */

final public class DesEncrypter {

    /**
     * An instance of Logger.
     */
    protected Logger logger = null;
    // Cipher object for encrypting.
    private Cipher ecipher = null;

    // Cipher object for decrypting.
    private Cipher dcipher = null;

    /**
     * Construct the object by using a given password phrase.
     * @param passPhrase
     *            Specifies a password phrase.
     */
    public DesEncrypter(String passPhrase, Logger logger) {
        this.logger = logger;

        try {
            // Create the key
            KeySpec keySpec = new DESKeySpec(passPhrase.getBytes());
            SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);

            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");

            // Create the ciphers
            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);

        }
        catch (Throwable th) {
            logger.error("Failed to initialize Ciphers", th);
        }
    }

    /**
     * Encrypt a string.
     * @param str
     *            String to be encrypt.
     * @return A encrpted string.
     */
    public String encrypt(String str) {
        try {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");

            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);

            // Encode bytes to base64 to get a string
            return new String(Base64.encodeBase64(enc));
        }
        catch (Throwable th) {
            logger.error("Failed to encrypt the given string", th);
        }
        return null;
    }

    /**
     * Decrypt a encrypted string.
     * @param str
     *            A encrypted string.
     * @return The original string.
     */
    public String decrypt(String str) {
        try {
            // Decode base64 to get bytes
            byte[] dec = Base64.decodeBase64(str);

            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);

            // Decode using utf-8
            return new String(utf8, "UTF8");
        }
        catch (Throwable th) {
            logger.error("Failed to decrypt the given string", th);
        }
        return null;
    }

    /**
     * Gets the instance attribute of the DesEncrypter class
     * @return The instance value
     */
    public static DesEncrypter getInstance(Logger logger) {
        if (desEncrypter == null) {
            desEncrypter = new DesEncrypter(".-'/W2@ce03=fky#vw6&H", logger);
        }
        return desEncrypter;
    }

    private static DesEncrypter desEncrypter = null;
}
