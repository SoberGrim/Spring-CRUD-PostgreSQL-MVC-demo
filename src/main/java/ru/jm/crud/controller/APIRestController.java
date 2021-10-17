package ru.jm.crud.controller;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestTemplate;
import ru.jm.crud.model.HTTPRequest;
import ru.jm.crud.model.User;
import ru.jm.crud.model.UserDTO;
import ru.jm.crud.service.RoleService;
import ru.jm.crud.service.UserService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static ru.jm.crud.controller.Utils.*;


@RestController
@RequestMapping("/api")
public class APIRestController {
    final UserService service;
    final RoleService roleService;

    @Autowired
    public APIRestController(UserService service, RoleService roleService) {
        this.service = service;
        this.roleService = roleService;
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/users")
    List<User> userList() {
        return service.getFilterUsers(false);
    }

    @PostMapping("/register")
    UserDTO register(@RequestBody @Valid UserDTO tempUser, BindingResult bindingResult) {
        tempUser.setRoleStr("GUEST");
        return createNewUser(tempUser, bindingResult);
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/new")
    UserDTO createNewUser(@RequestBody @Valid UserDTO tempUser, BindingResult bindingResult) {
        checkLoginEmailBusy(tempUser, bindingResult, service);
        UserDTO userErrorDTO = parseBindingErrors(bindingResult);
        if (bindingResult.hasErrors()) {
            userErrorDTO.setErrorsPresent(true);
        } else {
            User user = new User();
            user.merge(tempUser, roleService.getRoles(tempUser.getRoleStr()));
            service.update(user);
        }

        return userErrorDTO;
    }


    @Secured("ROLE_ADMIN")
    @PatchMapping("/edit")
    UserDTO editUser(@RequestBody @Valid UserDTO tmpUser, BindingResult bindingResult) {

        String idStr = tmpUser.getId();
        Long id = idStr.matches("\\d+")?Long.parseLong(idStr):0;
        User user = service.getById(id);

        checkLoginEmailBusy(tmpUser, bindingResult, service);
        UserDTO userErrorDTO = parseBindingErrors(bindingResult);

        if (bindingResult.hasErrors()) {
            userErrorDTO.setErrorsPresent(true);
            System.out.println("UserFields have errors: "+userErrorDTO);
        } else {
            user.merge(tmpUser, roleService.getRoles(tmpUser.getRoleStr()));
            service.update(user);
        }

        return userErrorDTO;
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/search")
    void searchUser(@RequestBody UserDTO tmpUser) {
        User user = new User();
        user.merge(tmpUser, roleService.getRoles(tmpUser.getRoleStr()));
        service.setFilter(user, false);
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/filter")
    void filterUser(@RequestBody UserDTO tmpUser) {
        User user = new User();
        user.merge(tmpUser, roleService.getRoles(tmpUser.getRoleStr()));
        service.setFilter(user, true);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/removefilter")
    void removeFilter() {
        service.removeFilter();
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/delete")
    void deleteUserById(@RequestBody String idStr) {
        Long id = idStr.matches("\\d+")?Long.parseLong(idStr):0;
        service.delete(id);
    }


    String cookies="";
    @PostMapping("/proxy")
    String proxy(@RequestBody HTTPRequest request) {
        HttpMethod httpMethod =
                (Objects.equals(request.method, "GET"))? HttpMethod.GET :
                (Objects.equals(request.method, "PUT"))? HttpMethod.PUT :
                (Objects.equals(request.method, "DELETE"))? HttpMethod.DELETE : HttpMethod.POST;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.COOKIE, cookies);

        HttpEntity<String> entity = new HttpEntity<>(request.postData, headers);
        ResponseEntity<String> respEntity = new RestTemplate().exchange(request.url, httpMethod, entity, String.class);

        String tmpCookies = respEntity.getHeaders().getFirst("set-cookie");
        if (tmpCookies != null) cookies = tmpCookies;

        return respEntity.getBody();
    }
}






