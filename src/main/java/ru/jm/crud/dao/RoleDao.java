package ru.jm.crud.dao;

import ru.jm.crud.model.UserRole;

import java.util.ArrayList;


public interface RoleDao {

    String add(UserRole userRole);
    UserRole getRole(Integer id);
    UserRole getRole(String role);
    ArrayList<UserRole> getRoles(String roles);
    ArrayList<UserRole> getRoles();

}
