package ru.jm.crud.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/admin")
public class AdminRestController {
    final UserService service;
    final RoleService roleService;

    @Autowired
    public AdminRestController(UserService service, RoleService roleService) {
        this.service = service;
        this.roleService = roleService;
    }

    @GetMapping("/users")
    List<User> all() throws JsonProcessingException {
        List<User> list = service.getAllUsers(false);

      //  String result = new ObjectMapper().writeValueAsString(usr);
     //   System.out.println("@GetMapping: "+result);
        return list;
    }

    @PostMapping("/users")
    List<User> allP() {
        List<User> list = service.getAllUsers(false);

        //  String result = new ObjectMapper().writeValueAsString(usr);
        //   System.out.println("@GetMapping: "+result);
        return list;
    }



    //    JSONObject  jo = usr.toJSON();//.toString();
    // return usr.toJSON().toString();
}
