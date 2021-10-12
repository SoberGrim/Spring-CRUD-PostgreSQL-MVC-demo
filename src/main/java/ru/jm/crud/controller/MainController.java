package ru.jm.crud.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import ru.jm.crud.model.User;
import ru.jm.crud.model.UserRole;
import ru.jm.crud.service.RoleService;
import ru.jm.crud.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;


@Controller
@RequestMapping("/")
public class MainController {
    final UserService service;
    final RoleService roleService;
    int modalWindowId = 0;

    @Autowired
    public MainController(UserService service, RoleService roleService) {
        this.service = service;
        this.roleService = roleService;
    }

    @GetMapping("login")
    public String loginPage(@ModelAttribute("user") User user, Model model)
    {
        model.addAttribute("modalWindowId", modalWindowId);
        return "login";
    }

    @GetMapping("logout401")
    public String logout401Page()
    {
        return "logout401";
    }

    @GetMapping("logout402")
    public String logout402Page()
    {
        return "logout402";
    }

    @GetMapping("/basicauth")
    public String basicAuthPage()
    {
        return "basicauth";
    }

//    @GetMapping("error")
//    public String errorPage()
//    {
//        return "index";
//    }
//
//    @GetMapping("errors")
//    public String errorsPage()
//    {
//        return "index";
//    }
//    @GetMapping("")
//    public String welcome0(Principal pr, Authentication authentication, Model model) {
//        model.addAttribute("principal", getPrincipal(pr,authentication));
//        model.addAttribute("user", getPrincipal(pr,authentication));
//        return "guest_old";
//    }

    @GetMapping("guest")
    public String welcome(Principal pr, Authentication authentication, Model model) {
        model.addAttribute("principal", getPrincipal(pr,authentication));
        model.addAttribute("user", getPrincipal(pr,authentication));
        return "guest";
    }

    @GetMapping("user")
    public String user(Principal pr, Authentication authentication, Model model) {
        model.addAttribute("principal", getPrincipal(pr,authentication));
        model.addAttribute("user", getPrincipal(pr,authentication));
        return "user";
    }

    @PostMapping("/register")
    public String create(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {
        modalWindowId = 1;

        model.addAttribute("modalWindowId", modalWindowId);

        if (service.getByUsername(user.getUsername()) != null) {
            bindingResult.addError(new FieldError("username", "username", "Username already taken"));
        }

        if (service.getByEmail(user.getEmail()) != null) {
            bindingResult.addError(new FieldError("email", "email", "User with this email already exists"));
        }

        if (bindingResult.hasErrors()) {
            return "login";
        }

        user.addRole(roleService.getRole("ROLE_GUEST"));
        service.update(user);
        modalWindowId = 0;
        return "redirect:/login";
    }

    private User getPrincipal(Principal pr, Authentication authentication) {
        User principal = service.getByUsername(pr.getName());
        if (principal == null) {
            principal = new User();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String prUsername = userDetails.getUsername();
            principal.setEmail("deleted");
            principal.setUsername(prUsername);
            ArrayList<GrantedAuthority> authArr = new ArrayList<>(userDetails.getAuthorities());
            for (GrantedAuthority auth : authArr) {
                principal.addRole(new UserRole(auth.getAuthority()));
            }
        }
        return principal;
    }
}
