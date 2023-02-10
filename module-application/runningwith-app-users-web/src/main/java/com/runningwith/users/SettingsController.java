package com.runningwith.users;

import com.runningwith.users.form.PasswordForm;
import com.runningwith.users.form.Profile;
import com.runningwith.users.validator.PasswordFormValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.runningwith.users.form.Profile.toProfile;
import static com.runningwith.utils.WebUtils.REDIRECT;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SettingsController {

    public static final String URL_SETTINGS_PROFILE = "/settings/profile";
    public static final String VIEW_SETTINGS_PROFILE = "settings/profile";
    public static final String PASSWORD_FORM = "passwordForm";
    public static final String URL_SETTINGS_PASSWORD = "/settings/password";
    public static final String VIEW_SETTINGS_PASSWORD = "settings/password";

    private final UsersService usersService;

    @InitBinder(PASSWORD_FORM)
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @GetMapping(URL_SETTINGS_PROFILE)
    public String profileUpdateView(@CurrentUser UsersEntity usersEntity, Model model) {

        model.addAttribute("profile", toProfile(usersEntity));
        model.addAttribute("nickname", usersEntity.getNickname());

        return VIEW_SETTINGS_PROFILE;
    }

    @PostMapping(URL_SETTINGS_PROFILE)
    public String updateProfile(@CurrentUser UsersEntity usersEntity, @Validated Profile profile, BindingResult bindingResult,
                                Model model, RedirectAttributes attributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("nickname", usersEntity.getNickname());
            return VIEW_SETTINGS_PROFILE;
        }

        usersService.updateProfile(usersEntity, profile);
        attributes.addFlashAttribute("message", "프로필 수정 완료");
        return REDIRECT + URL_SETTINGS_PROFILE;
    }

    @GetMapping(URL_SETTINGS_PASSWORD)
    public String updatePasswordView(@CurrentUser UsersEntity usersEntity, Model model) {
        model.addAttribute("user", usersEntity);
        model.addAttribute(new PasswordForm());
        return VIEW_SETTINGS_PASSWORD;
    }

    @PostMapping(URL_SETTINGS_PASSWORD)
    public String updatePassword(@CurrentUser UsersEntity usersEntity, @Validated PasswordForm passwordForm, BindingResult bindingResult
            , Model model, RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", usersEntity);
            return VIEW_SETTINGS_PASSWORD;
        }

        usersService.updatePassword(usersEntity, passwordForm);
        attributes.addFlashAttribute("message", "비밀번호 변경 완료");
        return REDIRECT + URL_SETTINGS_PASSWORD;
    }

}