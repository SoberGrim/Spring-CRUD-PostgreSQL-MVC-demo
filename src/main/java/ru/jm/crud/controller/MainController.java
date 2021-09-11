package ru.jm.crud.controller;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import ru.jm.crud.model.User;
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

    @Autowired
    public MainController(UserService service, RoleService roleService) {
        this.service = service;
        this.roleService = roleService;
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String loginPage() {
        return "login";
    }

    @GetMapping("welcome")
    public String welcome(Principal principal, Model model) {
        User user = new User();
        user.setFirstname(principal.getName());
        model.addAttribute("user", user);
        return "welcome";
    }

    @GetMapping("user")
    public String user(Principal principal, Model model) {
        User user = service.getByUsername(principal.getName());
        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/register")
    public String register(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "register";
    }


    @PostMapping("/register")
    public String create(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (service.getByUsername(user.getUsername()) != null) {
            bindingResult.addError(new FieldError("username", "username", "Username already taken"));
            user.setUsername("");
            return "register";
        }

        if (service.getByEmail(user.getEmail()) != null) {
            bindingResult.addError(new FieldError("email", "email", "User with this email already exists"));
            user.setEmail("");
            return "register";
        }

        user.addRole(roleService.getRole("ROLE_GUEST"));
        service.update(user);

        return "redirect:/login";
    }
}
