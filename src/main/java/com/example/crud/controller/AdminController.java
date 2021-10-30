package com.example.crud.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.crud.model.User;
import com.example.crud.model.UserRole;
import com.example.crud.service.RoleService;
import com.example.crud.service.UserService;

import java.security.Principal;
import java.util.ArrayList;


@Controller
@Secured("ROLE_ADMIN")
@RequestMapping("/admin")
public class AdminController {
    final UserService service;
    final RoleService roleService;


    @Autowired
    public AdminController(UserService service, RoleService roleService) {
        this.service = service;
        this.roleService = roleService;
    }


    @GetMapping("")
    public String index(@RequestParam(name = "page", required = false, defaultValue = "1") String strPageNum,
                           Principal pr, Authentication authentication,
                           Model model) {

        model.addAttribute("roles", roleService.getRoles());
        model.addAttribute("principal", getPrincipal(pr, authentication));
        model.addAttribute("isFilterActive", service.isFilterSet());
        return "admin";
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
