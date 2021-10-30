package com.example.crud.service;

import com.example.crud.model.UserRole;

import java.util.ArrayList;


public interface RoleService {

    String add(UserRole userRole);
    UserRole getRole(Integer id);
    UserRole getRole(String role);
    ArrayList<UserRole> getRoles(String roles);
    ArrayList<UserRole> getRoles();

}