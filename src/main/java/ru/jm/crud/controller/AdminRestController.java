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
    public UserDTO indexEditUser(@RequestBody @Valid UserDTO tmpUser, BindingResult bindingResult) {

        String idStr = tmpUser.getId();
        Long id = idStr.matches("\\d+")?Long.parseLong(idStr):0;
        User user = service.getById(id);

        checkLoginEmailBusy(tmpUser, bindingResult);
        UserDTO userErrorDTO = checkBindingErrors(bindingResult);

        if (bindingResult.hasErrors()) {
            userErrorDTO.setErrorsPresent(true);
            System.out.println(userErrorDTO);
        } else {
            user.merge(tmpUser, roleService.getRoles(tmpUser.getRoleStr()));
            service.update(user);
        }

        return userErrorDTO;
    }

    @PostMapping("/new")
    public UserDTO indexNewUser(@RequestBody @Valid UserDTO tempUser, BindingResult bindingResult) {

        checkLoginEmailBusy(tempUser, bindingResult);
        UserDTO userErrorDTO = checkBindingErrors(bindingResult);

        if (bindingResult.hasErrors()) {
            userErrorDTO.setErrorsPresent(true);
        } else {
            User user = new User();
            user.merge(tempUser, roleService.getRoles(tempUser.getRoleStr()));
            service.update(user);
        }

        return userErrorDTO;
    }

    @GetMapping("/delete={id}")
    String deleteUserById(@PathVariable("id") Long id) {
        service.delete(id);
        return "success";
    }

    private void checkLoginEmailBusy(UserDTO userdto, BindingResult bindingResult) {
        User editedUser = service.getByUsername(userdto.getUsername());

        if ((editedUser != null) && (!editedUser.getId().toString().equals(userdto.getId()))) {
            bindingResult.addError(new FieldError("username", "username", "Username already taken"));
        }
        editedUser = service.getByEmail(userdto.getEmail());
        if ((editedUser != null) && (!editedUser.getId().toString().equals(userdto.getId()))) {
            bindingResult.addError(new FieldError("email", "email", "User with this email already exists"));
        }
    }

    private UserDTO checkBindingErrors(BindingResult bindingResult) {
        UserDTO userError = new UserDTO();
        userError.setId(
                (bindingResult.getFieldErrorCount("id")>0)?
                        bindingResult.getFieldError("id").getDefaultMessage():"");
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

        return userError;
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
