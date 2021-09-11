package ru.jm.crud.service;

import ru.jm.crud.model.UserRole;
import ru.jm.crud.dao.RoleDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public ArrayList<UserRole> getRoles() {
        return dao.getRoles();
    }

}
