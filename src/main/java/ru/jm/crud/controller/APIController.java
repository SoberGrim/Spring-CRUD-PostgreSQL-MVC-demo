package ru.jm.crud.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.jm.crud.model.User;
import ru.jm.crud.model.UserRole;
import ru.jm.crud.service.RoleService;
import ru.jm.crud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.ArrayList;


@Controller
@RequestMapping("/api")
public class APIController {
    final UserService service;
    final RoleService roleService;

    @Autowired
    public APIController(UserService service, RoleService roleService) {
        this.service = service;
        this.roleService = roleService;
    }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("/admin")
    String getAdmin(Model model) {
        model.addAttribute("roles", roleService.getRoles());
        model.addAttribute("isFilterActive", service.isFilterSet());
        return "adminHTML";
    }

    @Secured({"ROLE_ADMIN","ROLE_USER"})
    @GetMapping("/user")
    String getUser(Principal pr, Authentication authentication, Model model) {
        model.addAttribute("user", getPrincipal(pr, authentication));
        return "userHTML";
    }

    @Secured({"ROLE_ADMIN","ROLE_USER","ROLE_GUEST"})
    @GetMapping("/guest")
    String getGuest(Principal pr, Authentication authentication, Model model) {
        model.addAttribute("user", getPrincipal(pr, authentication));
        return "guestHTML";
    }


    private User getPrincipal(Principal pr, Authentication authentication) {
        User principal = service.getByUsername(pr.getName());
        if (principal == null) {
            principal = new User();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            principal.setEmail("deleted");
            ArrayList<GrantedAuthority> authArr = new ArrayList<>(userDetails.getAuthorities());
            for (GrantedAuthority auth : authArr) {
                principal.addRole(new UserRole(auth.getAuthority()));
            }
        }
        return principal;
    }
}
