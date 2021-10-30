package com.example.crud.dao;

import com.example.crud.model.User;
import com.example.crud.model.UserRole;

import java.util.Collection;
import java.util.List;


public interface UserDao {

    String add(User user);
    <T extends User> Collection<T> bulkSave(Collection<T> entities);
    String add(String username, String password, String firstName, String lastName, String age, String email, UserRole... roles);
    User getById(Long id);
    User getByUsername(String username);
    User getByLogin(String username);
    List<User> getByName(String firstname);
    List<User> getByName(String firstname, String lastname);
    List<User> getByLastName(String lastname);
    User getByEmail(String email);
    List<User> getByAge(String age);
    List<User> getAllUsers(boolean fromCache);
    List<User> getFilterUsers(boolean fromCache);
    void setFilter(User user, boolean strict);
    boolean isFilterSet();


    void removeFilter();
    void update(User user);
    void delete(Long id);

}
