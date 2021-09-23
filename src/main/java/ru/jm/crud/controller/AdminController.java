package ru.jm.crud.controller;

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
import java.util.Arrays;
import java.util.List;


@Controller
@RequestMapping("/admin")
public class AdminController {
    final UserService service;
    final RoleService roleService;
    int modalWindowId = 0;

    @Autowired
    public AdminController(UserService service, RoleService roleService) {
        this.service = service;
        this.roleService = roleService;
    }

    @GetMapping("")
    public String indexGet(@RequestParam(name = "page", required = false, defaultValue = "1") String strPageNum,
                           Principal pr, Authentication authentication,
                           Model model) {

        viewInput(strPageNum, pr, authentication, model);
        model.addAttribute("user", new User());
        return "index";
    }

    @PatchMapping("")
    public String indexPatch(@RequestParam(name = "page", required = false, defaultValue = "1") String strPageNum,
                             @ModelAttribute("user") @Valid User user, BindingResult bindingResult,
                             @RequestParam(value = "index", required = false) Integer[] roleIds,
                             Principal pr, Authentication authentication,
                             Model model) {

        modalWindowId = 1;
        viewInput(strPageNum, pr, authentication, model);
        return viewOutput(user, bindingResult, roleIds);
    }

    @PostMapping("")
    public String indexPost(@RequestParam(name = "page", required = false, defaultValue = "1") String strPageNum,
                            @ModelAttribute("user") @Valid User user, BindingResult bindingResult,
                            @RequestParam(value = "index", required = false) Integer[] roleIds,
                            Principal pr, Authentication authentication,
                            Model model) {

        modalWindowId = 2;
        viewInput(strPageNum, pr, authentication, model);
        return viewOutput(user, bindingResult, roleIds);
    }

    private void viewInput(@RequestParam(name = "page", required = false, defaultValue = "1") String strPageNum, Principal pr, Authentication authentication, Model model) {
        model.addAttribute("modalWindowId", modalWindowId);
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

        service.update(user);
        modalWindowId = 0;
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

//    @GetMapping("/new")
//    public String addNewUserPage(Model model) {
//        ArrayList<UserRole> roles = roleService.getRoles();
//        model.addAttribute("roles", roles);
//        User user = new User();
//        model.addAttribute("user", user);
//        modalWindowId = 0;
//        return "create";
//    }

//    @PostMapping("/new")
//    public String createUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult,
//                             @RequestParam(value = "index", required = false) Integer[] index,
//                             Model model) {
//        ArrayList<UserRole> roles = roleService.getRoles();
//        model.addAttribute("roles", roles);
//
//        if (bindingResult.hasErrors()) {
//            System.out.println(user);
//            return "create";
//        }
//
//        if (service.getByUsername(user.getUsername()) != null) {
//            bindingResult.addError(new FieldError("username", "username", "Username already taken"));
//            user.setUsername("");
//            return "create";
//        }
//
//        if (service.getByEmail(user.getEmail()) != null) {
//            bindingResult.addError(new FieldError("email", "email", "User with this email already exists"));
//            user.setEmail("");
//            return "create";
//        }
//
//        if (index != null) {
//            for (Integer i : index) {
//                user.addRole(roleService.getRole(i));
//            }
//        }
//
//        service.update(user);
//        modalWindowId = 0;
//        return "redirect:/admin";
//    }

    @GetMapping("filter")
    public String filterPage(@ModelAttribute("user") User user, Model model) {
        ArrayList<UserRole> roles = roleService.getRoles();
        model.addAttribute("roles", roles);
        modalWindowId = 0;
        return "filter";
    }

    @GetMapping("search")
    public String searchPage(@ModelAttribute("user") User user, Model model) {
        ArrayList<UserRole> roles = roleService.getRoles();
        model.addAttribute("roles", roles);
        modalWindowId = 0;
        return "search";
    }

    @PatchMapping("filter")
    public String filterApply(@ModelAttribute("user") User user,
                              @RequestParam(value = "index", required = false) Integer[] index) {
        System.out.println("Setting filter");
        modalWindowId = 3;
        if (index != null) {
            for (Integer i : index) {
                user.addRole(roleService.getRole(i));
            }
            System.out.println("Filtered roles set:"+user.getUserRoles());
        }
        service.setFilter(user, true);
        return "redirect:/admin";
    }

    @PatchMapping("search")
    public String searchApply(@ModelAttribute("user") User user,
                              @RequestParam(value = "index", required = false) Integer[] index) {
        System.out.println("Setting search filter");
        modalWindowId = 4;
        if (index != null) {
            for (Integer i : index) {
                user.addRole(roleService.getRole(i));
            }
        }
        service.setFilter(user, false);
        return "redirect:/admin";
    }

    @GetMapping("removeFilter")
    public String removeFilter() {
        service.removeFilter();
        System.out.println("Removed filter");
        modalWindowId = 0;
        return "redirect:/admin";
    }

    @GetMapping("/delete={id}")
    public String deleteUserById(@PathVariable("id") Long id) {
        service.delete(id);
        modalWindowId = 0;
        return "redirect:/admin";
    }
}
