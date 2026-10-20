package com.eswar.uscistracker.controller;

import com.eswar.uscistracker.domain.AppUser;
import com.eswar.uscistracker.domain.FriendlyStatus;
import com.eswar.uscistracker.domain.TrackedCase;
import com.eswar.uscistracker.service.CaseService;
import com.eswar.uscistracker.service.UserService;
import com.eswar.uscistracker.web.CaseForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cases")
public class CaseController {

    private final UserService userService;
    private final CaseService caseService;

    public CaseController(
            UserService userService,
            CaseService caseService) {

        this.userService = userService;
        this.caseService = caseService;
    }

    @GetMapping("/new")
    String newCase(Model model) {

        model.addAttribute(
                "form",
                new CaseForm()
        );

        model.addAttribute(
                "editing",
                false
        );

        return "case-form";
    }

    @PostMapping
    String create(
            @Valid @ModelAttribute("form")
            CaseForm form,

            BindingResult binding,

            Authentication auth,

            Model model,

            RedirectAttributes redirect) {

        if (binding.hasErrors()) {

            model.addAttribute(
                    "editing",
                    false
            );

            return "case-form";
        }

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        try {

            TrackedCase trackedCase =
                    caseService.create(
                            user,
                            form
                    );

            if (trackedCase.getFriendlyStatus()
                    == FriendlyStatus.INVALID_RECEIPT) {

                redirect.addFlashAttribute(
                        "error",
                        "The case was saved, but USCIS reported this receipt number as invalid. Automatic tracking has stopped until you correct the number."
                );

            } else {

                redirect.addFlashAttribute(
                        "success",
                        "Case added and checked successfully."
                );
            }

            return "redirect:/cases/"
                    + trackedCase.getId();

        } catch (RuntimeException ex) {

            model.addAttribute(
                    "editing",
                    false
            );

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            return "case-form";
        }
    }

    @GetMapping("/{id}")
    String detail(
            @PathVariable Long id,
            Authentication auth,
            Model model) {

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        TrackedCase trackedCase =
                caseService.requireOwned(
                        id,
                        user.getId()
                );

        model.addAttribute(
                "caseItem",
                trackedCase
        );

        model.addAttribute(
                "history",
                caseService.historyFor(
                        trackedCase
                )
        );

        return "case-detail";
    }

    @GetMapping("/{id}/edit")
    String edit(
            @PathVariable Long id,
            Authentication auth,
            Model model) {

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        TrackedCase trackedCase =
                caseService.requireOwned(
                        id,
                        user.getId()
                );

        CaseForm form =
                new CaseForm();

        form.setCaseName(
                trackedCase.getCaseName()
        );

        form.setApplicantName(
                trackedCase.getApplicantName()
        );

        form.setNotificationEmail(
                trackedCase.getNotificationEmail()
        );

        form.setReceiptNumber(
                trackedCase.getReceiptNumber()
        );

        model.addAttribute(
                "form",
                form
        );

        model.addAttribute(
                "editing",
                true
        );

        model.addAttribute(
                "caseId",
                id
        );

        return "case-form";
    }

    @PostMapping("/{id}")
    String update(
            @PathVariable Long id,

            @Valid @ModelAttribute("form")
            CaseForm form,

            BindingResult binding,

            Authentication auth,

            Model model,

            RedirectAttributes redirect) {

        if (binding.hasErrors()) {

            model.addAttribute(
                    "editing",
                    true
            );

            model.addAttribute(
                    "caseId",
                    id
            );

            return "case-form";
        }

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        try {

            TrackedCase trackedCase =
                    caseService.update(
                            id,
                            user,
                            form
                    );

            redirect.addFlashAttribute(
                    "success",
                    "Case details updated."
            );

            return "redirect:/cases/"
                    + trackedCase.getId();

        } catch (RuntimeException ex) {

            model.addAttribute(
                    "editing",
                    true
            );

            model.addAttribute(
                    "caseId",
                    id
            );

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            return "case-form";
        }
    }

    @PostMapping("/{id}/refresh")
    String refresh(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirect) {

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        try {

            caseService.refreshOwned(
                    id,
                    user
            );

            redirect.addFlashAttribute(
                    "success",
                    "USCIS status refreshed."
            );

        } catch (RuntimeException ex) {

            redirect.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/cases/" + id;
    }

    @PostMapping("/{id}/pause")
    String pause(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirect) {

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        caseService.pause(
                id,
                user
        );

        redirect.addFlashAttribute(
                "success",
                "Automatic USCIS tracking paused."
        );

        return "redirect:/cases/" + id;
    }

    @PostMapping("/{id}/resume")
    String resume(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirect) {

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        try {

            caseService.resume(
                    id,
                    user
            );

            redirect.addFlashAttribute(
                    "success",
                    "Automatic USCIS tracking resumed."
            );

        } catch (IllegalArgumentException ex) {

            redirect.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/cases/" + id;
    }

    /*
     * ==========================================================
     * EMAIL SETTINGS
     * ==========================================================
     */

    @PostMapping("/{id}/notifications")
    String updateNotificationSettings(
            @PathVariable Long id,

            @RequestParam(
                    defaultValue = "false"
            )
            boolean dailySummaryEnabled,

            @RequestParam(
                    defaultValue = "false"
            )
            boolean statusChangeEmailEnabled,

            Authentication auth,

            RedirectAttributes redirect) {

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        caseService.updateNotificationSettings(
                id,
                user,
                dailySummaryEnabled,
                statusChangeEmailEnabled
        );

        redirect.addFlashAttribute(
                "success",
                "Email notification preferences saved."
        );

        return "redirect:/cases/" + id;
    }

    @PostMapping("/{id}/notifications/pause")
    String pauseNotifications(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirect) {

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        caseService.pauseEmailNotifications(
                id,
                user
        );

        redirect.addFlashAttribute(
                "success",
                "Email notifications paused. USCIS tracking will continue."
        );

        return "redirect:/cases/" + id;
    }

    @PostMapping("/{id}/notifications/resume")
    String resumeNotifications(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirect) {

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        caseService.resumeEmailNotifications(
                id,
                user
        );

        redirect.addFlashAttribute(
                "success",
                "Email notifications resumed."
        );

        return "redirect:/cases/" + id;
    }

    @PostMapping("/{id}/delete")
    String delete(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirect) {

        AppUser user =
                userService.requireByUsername(
                        auth.getName()
                );

        caseService.delete(
                id,
                user
        );

        redirect.addFlashAttribute(
                "success",
                "Case removed."
        );

        return "redirect:/";
    }
}