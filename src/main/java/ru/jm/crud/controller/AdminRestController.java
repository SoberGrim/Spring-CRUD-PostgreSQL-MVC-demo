package ru.jm.crud.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.jm.crud.model.User;
import ru.jm.crud.model.UserDTO;
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
import java.util.Set;


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

    @Secured({"ROLE_ADMIN"})
    @GetMapping("/users")
    List<User> all() throws JsonProcessingException {
        List<User> list = service.getAllUsers(false);

      //  String result = new ObjectMapper().writeValueAsString(usr);
     //   System.out.println("@GetMapping: "+result);
        return list;
    }

//    @GetMapping("/users")
//    List<User> allP() {
//        List<User> list = service.getAllUsers(false);
//
//        //  String result = new ObjectMapper().writeValueAsString(usr);
//        //   System.out.println("@GetMapping: "+result);
//        return list;
//    }

//    @PostMapping("/delete={id}")
//    public String deleteUserById(@PathVariable("id") Long id) {
//        service.delete(id);
//        return "done";
//    }

    @PostMapping("/edit")
    public String indexEditUser(@RequestBody UserDTO tmpUser) {
        System.out.println(tmpUser);
        String idStr = tmpUser.getId();
        Long id = idStr.matches("\\d+")?Long.parseLong(idStr):0;
        User user = service.getById(id);

        System.out.println("x1 "+user);

        user.merge(tmpUser, roleService.getRoles(tmpUser.getRoleStr()));
        System.out.println("x2 "+user);
        service.update(user);

        return "success";
    }

    @PostMapping("/new")
    public UserDTO indexNewUser(@RequestBody @Valid UserDTO tempUser, BindingResult bindingResult) {

        UserDTO userError = new UserDTO();
        userError.setUsername(
                (bindingResult.getFieldErrorCount("username")>0)?
                        bindingResult.getFieldError("username").getDefaultMessage():"");
        userError.setPassword(
                (bindingResult.getFieldErrorCount("password")>0)?
                        bindingResult.getFieldError("password").getDefaultMessage():"");
        userError.setEmail(
                (bindingResult.getFieldErrorCount("email")>0)?
                        bindingResult.getFieldError("email").getDefaultMessage():"");
        userError.setAge(
                (bindingResult.getFieldErrorCount("age")>0)?
                        bindingResult.getFieldError("age").getDefaultMessage():"");
        userError.setFirstname(
                (bindingResult.getFieldErrorCount("firstname")>0)?
                        bindingResult.getFieldError("firstname").getDefaultMessage():"");
        userError.setLastname(
                (bindingResult.getFieldErrorCount("lastname")>0)?
                        bindingResult.getFieldError("lastname").getDefaultMessage():"");
        System.out.println(userError);

        if (bindingResult.hasErrors()) {
            userError.setId("-1");
        } else {
            System.out.println(tempUser);
            User user = new User();
            user.merge(tempUser, roleService.getRoles(tempUser.getRoleStr()));
            service.update(user);
        }

        return userError;
    }

    @GetMapping("/delete={id}")
    String deleteUserById(@PathVariable("id") Long id) {
        service.delete(id);
        return "success";
    }

    private void viewInput(@RequestParam(name = "page", required = false, defaultValue = "1") String strPageNum, Principal pr, Authentication authentication, Model model) {
        int page = getPageNum(strPageNum);
        model.addAttribute("pageNum", page);
        model.addAttribute("users", service.getFilterUsers(page != 1));
        model.addAttribute("roles", roleService.getRoles());
        model.addAttribute("principal", getPrincipal(pr, authentication));
        model.addAttribute("isFilterActive", service.isFilterSet());
    }

    private String viewOutput(User user, BindingResult bindingResult, Integer[] roleIds) {
        setUserRoles(user, roleIds);
        checkUserFields(user, bindingResult);
        if (bindingResult.hasErrors()) {
            System.out.println("bindingResult.hasErrors: " + user);
            return "index";
        }
        System.out.println("updating user:"+user);
        service.update(user);
        return "redirect:/admin";
    }

    private void checkUserFields(User user, BindingResult bindingResult) {
        User editedUser = service.getByUsername(user.getUsername());
        if ((editedUser != null) && (!editedUser.getId().equals(user.getId()))) {
            bindingResult.addError(new FieldError("username", "username", "Username already taken"));
        }
        editedUser = service.getByEmail(user.getEmail());
        if ((editedUser != null) && (!editedUser.getId().equals(user.getId()))) {
            bindingResult.addError(new FieldError("email", "email", "User with this email already exists"));
        }
    }

    private int getPageNum(String strParam) {
        return strParam.matches("\\d+") ? Integer.parseInt(strParam) : 1;
    }

    private void setUserRoles(User user, Integer[] roleIds) {
        if (roleIds != null) {
            for (Integer i : roleIds) {
                user.addRole(roleService.getRole(i));
            }
        }
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

    //    JSONObject  jo = usr.toJSON();//.toString();
    // return usr.toJSON().toString();
}
