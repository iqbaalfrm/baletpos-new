package com.baletpos.util;

public class HashGenerator {
    public static void main(String[] args) {
        String pass = "admin123";
        String hash = PasswordUtil.hashPassword(pass);
        System.out.println("Password: " + pass);
        System.out.println("Hash: " + hash);

        // Cek verifikasi
        boolean check = PasswordUtil.checkPassword(pass, hash);
        System.out.println("Check: " + check);
    }
}


