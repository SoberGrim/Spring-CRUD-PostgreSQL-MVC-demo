package ru.jm.crud.controller;

import org.springframework.security.access.annotation.Secured;
import ru.jm.crud.model.User;
import ru.jm.crud.model.UserDTO;
import ru.jm.crud.service.RoleService;
import ru.jm.crud.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;


@RestController
@Secured("ROLE_ADMIN")
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
    List<User> userList() {
        return service.getAllUsers(false);
    }


    @PostMapping("/new")
    public UserDTO createNewUser(@RequestBody @Valid UserDTO tempUser, BindingResult bindingResult) {

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


    @PatchMapping("/edit")
    public UserDTO editUser(@RequestBody @Valid UserDTO tmpUser, BindingResult bindingResult) {

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


    @DeleteMapping("/delete")
    String deleteUserById(@RequestBody String idStr) {
        Long id = idStr.matches("\\d+")?Long.parseLong(idStr):0;
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
}
