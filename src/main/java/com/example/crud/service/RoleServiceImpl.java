package com.example.crud.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.crud.model.UserRole;
import com.example.crud.dao.RoleDao;

import java.util.ArrayList;


@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    final RoleDao dao;

    @Autowired
    public RoleServiceImpl(RoleDao dao) {
        this.dao = dao;
    }

    @Override
    public String add(UserRole userRole) {
        return dao.add(userRole);
    }

    @Override
    public UserRole getRole(Integer id) {
        return dao.getRole(id);
    }

    @Override
    public UserRole getRole(String role) {
        return dao.getRole(role);
    }

    @Override
    public ArrayList<UserRole> getRoles(String roles) {
        return dao.getRoles(roles);
    }

    @Override
    public ArrayList<UserRole> getRoles() {
        return dao.getRoles();
    }

}
