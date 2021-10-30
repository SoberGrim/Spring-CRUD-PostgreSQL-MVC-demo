package com.example.crud.dao;

import com.example.crud.model.UserRole;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.*;


@Repository
public class RoleDaoImpl implements RoleDao {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public String add(UserRole userRole) {
        entityManager.persist(userRole);
        return "ok";
    }
    @Override
    public UserRole getRole(Integer id) {
        TypedQuery<UserRole> result = entityManager.createQuery("FROM UserRole WHERE id=:id", UserRole.class).setParameter("id", id);
        return result.getResultList().isEmpty() ? null : result.getSingleResult();
    }

    @Override
    public UserRole getRole(String userRole) {
        TypedQuery<UserRole> result = entityManager.createQuery("FROM UserRole WHERE role=:userRole", UserRole.class).setParameter("userRole", userRole);
        return result.getResultList().isEmpty() ? null : result.getSingleResult();
    }

    @Override
    public ArrayList<UserRole> getRoles(String roles) {
        ArrayList<UserRole> list = new ArrayList<>();
        if (roles.contains("ADMIN")) {
            list.add(getRole("ROLE_ADMIN"));
        }
        if (roles.contains("USER")) {
            list.add(getRole("ROLE_USER"));
        }
        if (roles.contains("GUEST")) {
            list.add(getRole("ROLE_GUEST"));
        }
        return list;
    }

    @Override
    public ArrayList<UserRole> getRoles() {
        ArrayList<UserRole> list = new ArrayList<>(entityManager.createQuery("FROM UserRole ORDER BY id", UserRole.class).getResultList());
        System.out.println("Role list received from sql: " + list);
        return list;
    }
}
