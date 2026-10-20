package com.eswar.uscistracker.controller;

import com.eswar.uscistracker.domain.*;
import com.eswar.uscistracker.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private final UserService userService;
    private final CaseService caseService;

    public HomeController(UserService userService, CaseService caseService) {
        this.userService = userService;
        this.caseService = caseService;
    }

    @GetMapping("/")
    String dashboard(Authentication auth, Model model) {
        AppUser user = userService.requireByUsername(auth.getName());
        List<TrackedCase> cases = caseService.listForUser(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("cases", cases);
        model.addAttribute("total", cases.size());
        model.addAttribute("approved", cases.stream().filter(c -> c.getFriendlyStatus() == FriendlyStatus.APPROVED).count());
        model.addAttribute("processing", cases.stream().filter(c -> c.getFriendlyStatus() == FriendlyStatus.PROCESSING).count());
        model.addAttribute("attention", cases.stream().filter(c ->
            c.getFriendlyStatus() == FriendlyStatus.ACTION_REQUIRED ||
            c.getFriendlyStatus() == FriendlyStatus.INVALID_RECEIPT).count());
        return "dashboard";
    }
}
