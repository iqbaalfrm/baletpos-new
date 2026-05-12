package com.baletpos.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * Hash a password using OpenBSD bcrypt
     * 
     * @param plainTextPassword the password to hash
     * @return hashed password
     */
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    /**
     * Check that a plaintext password matches a previously hashed one
     * 
     * @param plainTextPassword the plaintext password to verify
     * @param hashedPassword    the previously-hashed password
     * @return true if the passwords match, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (hashedPassword == null) {
            return false;
        }
        if (!hashedPassword.startsWith("$2a$")) {
            System.err.println("WARN: Checking plaintext password for legacy user.");
            return plainTextPassword.equals(hashedPassword);
        }
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}


