package com.baletpos.util;

import com.baletpos.model.User;

public class Session {
    private static Session instance;
    private User currentUser;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return canManageStore() || canManageFinance();
    }

    public boolean isKasir() {
        return hasRole(User.Role.KASIR);
    }

    public boolean isAdminToko() {
        return hasRole(User.Role.ADMIN_TOKO);
    }

    public boolean isAdminKeuangan() {
        return hasRole(User.Role.ADMIN_KEUANGAN);
    }

    public boolean canManageStore() {
        return hasRole(User.Role.ADMIN_TOKO);
    }

    public boolean canManageFinance() {
        return hasRole(User.Role.ADMIN_KEUANGAN);
    }

    public boolean canViewReports() {
        return hasRole(User.Role.ADMIN_TOKO, User.Role.ADMIN_KEUANGAN, User.Role.KASIR);
    }

    public boolean hasRole(User.Role... roles) {
        if (!isLoggedIn()) {
            return false;
        }
        for (User.Role role : roles) {
            if (currentUser.getRole() == role) {
                return true;
            }
        }
        return false;
    }
}


