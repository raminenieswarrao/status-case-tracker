package com.eswar.uscistracker.controller;

import com.eswar.uscistracker.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {
    private final UserService userService;

    @Value("${uscis.mode:mock}")
    private String uscisMode;

    public GlobalModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("navUser")
    public Object navUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getName())) return null;
        return userService.requireByUsername(authentication.getName());
    }

    @ModelAttribute("uscisMode")
    public String uscisMode() {
        return uscisMode;
    }
}
