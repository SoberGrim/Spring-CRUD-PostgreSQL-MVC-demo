package ru.jm.crud.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import ru.jm.crud.model.User;
import ru.jm.crud.model.UserDTO;
import ru.jm.crud.model.UserRole;
import ru.jm.crud.service.UserService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Objects;



public class Utils {

    static void checkLoginEmailBusy(UserDTO userdto, BindingResult bindingResult, UserService service) {
        User editedUser = service.getByUsername(userdto.getUsername());
        if ((editedUser != null) && (!editedUser.getId().toString().equals(userdto.getId()))) {
            bindingResult.addError(new FieldError("username", "username", "Username already taken"));
        }
        editedUser = service.getByEmail(userdto.getEmail());
        if ((editedUser != null) && (!editedUser.getId().toString().equals(userdto.getId()))) {
            bindingResult.addError(new FieldError("email", "email", "User with this email already exists"));
        }
    }

    static UserDTO parseBindingErrors(BindingResult bindingResult) {
        UserDTO userError = new UserDTO();
        userError.setId(
                (bindingResult.getFieldErrorCount("id")>0)?
                        Objects.requireNonNull(bindingResult.getFieldError("id")).getDefaultMessage():"");
        userError.setUsername(
                (bindingResult.getFieldErrorCount("username")>0)?
                        Objects.requireNonNull(bindingResult.getFieldError("username")).getDefaultMessage():"");
        userError.setPassword(
                (bindingResult.getFieldErrorCount("password")>0)?
                        Objects.requireNonNull(bindingResult.getFieldError("password")).getDefaultMessage():"");
        userError.setEmail(
                (bindingResult.getFieldErrorCount("email")>0)?
                        Objects.requireNonNull(bindingResult.getFieldError("email")).getDefaultMessage():"");
        userError.setAge(
                (bindingResult.getFieldErrorCount("age")>0)?
                        Objects.requireNonNull(bindingResult.getFieldError("age")).getDefaultMessage():"");
        userError.setFirstname(
                (bindingResult.getFieldErrorCount("firstname")>0)?
                        Objects.requireNonNull(bindingResult.getFieldError("firstname")).getDefaultMessage():"");
        userError.setLastname(
                (bindingResult.getFieldErrorCount("lastname")>0)?
                        Objects.requireNonNull(bindingResult.getFieldError("lastname")).getDefaultMessage():"");

        return userError;
    }

    static User getPrincipal(Principal pr, Authentication authentication, UserService service) {
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
