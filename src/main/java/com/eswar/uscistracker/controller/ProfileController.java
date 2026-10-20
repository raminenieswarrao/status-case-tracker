package com.eswar.uscistracker.controller;

import com.eswar.uscistracker.domain.AppUser;
import com.eswar.uscistracker.service.UserService;
import com.eswar.uscistracker.web.ChangePasswordForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    String profile(Authentication auth, Model model) {
        model.addAttribute("user", userService.requireByUsername(auth.getName()));
        return "profile";
    }

    @GetMapping("/change-password")
    String changePassword(Model model) {
        model.addAttribute("form", new ChangePasswordForm());
        return "change-password";
    }

    @PostMapping("/change-password")
    String changePassword(@Valid @ModelAttribute("form") ChangePasswordForm form,
                          BindingResult binding,
                          Authentication auth,
                          Model model,
                          RedirectAttributes redirect) {
        if (binding.hasErrors()) return "change-password";
        try {
            userService.changePassword(auth.getName(), form.getCurrentPassword(),
                form.getNewPassword(), form.getConfirmPassword());
            redirect.addFlashAttribute("success", "Password updated.");
            return "redirect:/profile";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "change-password";
        }
    }
}
