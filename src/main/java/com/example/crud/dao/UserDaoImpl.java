package com.example.crud.dao;

import com.example.crud.model.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.example.crud.model.UserRole;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.UnaryOperator;


@Repository
public class UserDaoImpl implements UserDao {
    private Long filterId;
    private String filterName;
    private String filterLastname;
    private String filterAge;
    private String filterEmail;
    private String filterUsername;
    List<UserRole> filterRoles;
    private boolean filterStrict;
    private boolean isFilterActive = false;
    private List<User> userCache;

    UnaryOperator<String> conv = (name) -> {
        char[] chArray = name.toCharArray();
        byte[] byteArray = new byte[chArray.length];
        for (int i = 0; i < chArray.length; i++) {
            byteArray[i] = (byte) chArray[i];
        }
        return new String(byteArray, StandardCharsets.UTF_8);
    };

    @PersistenceContext
    EntityManager entityManager;

    public <T extends User> Collection<T> bulkSave(Collection<T> entities) {
        final List<T> savedEntities = new ArrayList<>(entities.size());
        int i = 0;
        System.out.println("filling DB with users...");
        for (T t : entities) {
            savedEntities.add(persistOrMerge(t));
            i++;
            if (i % 100 == 0) {
                // Flush a batch of inserts and release memory.
                entityManager.flush();
                entityManager.clear();
            }
        }
        System.out.println("filling DB done");
        return savedEntities;
    }

    private <T extends User> T persistOrMerge(T t) {
        if (t.getId() == null) {
            entityManager.persist(t);
            return t;
        } else {
            return entityManager.merge(t);
        }
    }


    @Override
    public String add(User user) {
        entityManager.persist(user);
        return "ok";
    }

    @Override
    public String add(String username, String password, String firstName, String lastName, String age, String email, UserRole... roles) {
        entityManager.persist(new User(username, password, firstName, lastName, age, email, roles));
        return "ok";
    }

    @Override
    public void update(User user) {
        System.out.println("UserDaoImpl user: "+user);
        User mergedUser = entityManager.merge(user);
        System.out.println("UserDaoImpl mergedUser: "+user);
        user.cloneUser(mergedUser);
    }

    @Override
    public User getById(Long id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public User getByUsername(String username) {
        if ((username.indexOf('Ð') >= 0) || (username.indexOf('Ñ') >= 0)) {
            username = conv.apply(username);
        }
        TypedQuery<User> result = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.username LIKE :username", User.class)
                .setParameter("username", username);

        return result.getResultList().isEmpty() ? null : result.getSingleResult();
    }

    @Override
    public User getByLogin(String login) {
        User user = (login.matches("^[^@]+@[^@]+\\.[^@]+$")) ? getByEmail(login) : getByUsername(login);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + login);
        }
        return user;
    }


    @Override
    public List<User> getByName(String firstname) {
        return entityManager.createQuery(
                "SELECT u FROM User u WHERE u.firstname LIKE :firstname", User.class)
                .setParameter("firstname", firstname)
                .setMaxResults(99)
                .getResultList();
    }

    @Override
    public List<User> getByName(String firstname, String lastname) {
        return entityManager.createQuery(
                "SELECT u FROM User u WHERE u.firstname LIKE :firstname AND u.lastname LIKE :lastname", User.class)
                .setParameter("firstname", firstname)
                .setParameter("lastname", lastname)
                .setMaxResults(99)
                .getResultList();
    }

    @Override
    public List<User> getByLastName(String lastname) {
        return entityManager.createQuery(
                "SELECT u FROM User u WHERE u.lastname LIKE :lastname", User.class)
                .setParameter("lastname", lastname)
                .setMaxResults(99)
                .getResultList();
    }

    @Override
    public User getByEmail(String email) {
        TypedQuery<User> result = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.email LIKE :email", User.class)
                .setParameter("email", email);
        return result.getResultList().isEmpty() ? null : result.getSingleResult();
    }

    @Override
    public List<User> getByAge(String age) {
        return entityManager.createQuery(
                "SELECT u FROM User u WHERE u.age =:age", User.class)
                .setParameter("age", age)
                .setMaxResults(99)
                .getResultList();
    }


    @Override
    public List<User> getAllUsers(boolean fromCache) {
        if (fromCache) {
            System.out.println("getting full user list from cache");
            return this.userCache;
        }
        System.out.println("getting FULL user list from DB");
        this.userCache = entityManager.createQuery("FROM User ORDER BY id   ", User.class).getResultList();
        return new LinkedList<>(this.userCache);
    }


    @Override
    public List<User> getFilterUsers(boolean fromCache) {
        return this.isFilterActive ?
                (this.filterStrict) ? getStrictFilterUsers(fromCache) : getNonStrictFilterUsers(fromCache)
                : getAllUsers(fromCache);
    }


    private List<User> getStrictFilterUsers(boolean fromCache) {
        System.out.println("Strict filtering users in user cache");
        List<User> list = getAllUsers(fromCache);
        List<User> outList = new LinkedList<>();

        boolean found = false;
        for (User user : list) {

            if ((this.filterId != null) && (!(this.filterId==0))) {
                if (!(Objects.equals(user.getId(), this.filterId))) {
                    continue;
                }
                found = true;
            }

            if ((this.filterName != null) && (!this.filterName.equals(""))) {
                if (!this.filterName.equals(user.getFirstname())) {
                    continue;
                }
                found = true;
            }

            if ((this.filterLastname != null) && (!this.filterLastname.equals(""))) {
                if (!this.filterLastname.equals(user.getLastname())) {
                    continue;
                }
                found = true;
            }

            if ((this.filterAge != null) && (!this.filterAge.equals(""))) {
                if (!this.filterAge.equals(user.getAge())) {
                    continue;
                }
                found = true;
            }

            if ((this.filterEmail != null) && (!this.filterEmail.equals(""))) {
                if (!this.filterEmail.equals(user.getEmail())) {
                    continue;
                }
                found = true;
            }

            if ((this.filterUsername != null) && (!this.filterUsername.equals(""))) {
                if (!this.filterUsername.equals(user.getUsername())) {
                    continue;
                }
                found = true;
            }

            if ((this.filterRoles != null) && (this.filterRoles.size() != 0)) {
                boolean userHasSearchedRoles = false;
                List<UserRole> userRoles = user.getUserRoles();
                if (userRoles.size() == this.filterRoles.size()) {
                    for (UserRole userRole : userRoles) {
                        if (this.filterRoles.contains(userRole)) {
                            userHasSearchedRoles = true;
                        } else {
                            userHasSearchedRoles = false;
                            break;
                        }
                    }
                }
                found = userHasSearchedRoles;
            }

            if (found) {
                System.out.println("Filter found user " + user.getUsername());
                outList.add(user);
            }
        }

        return outList;
    }

    private List<User> getNonStrictFilterUsers(boolean fromCache) {
        System.out.println("Searching users in user cache");
        List<User> list = getAllUsers(fromCache);
        List<User> outList = new LinkedList<>();

        boolean found = false;
        for (User user : list) {

            if ((this.filterId != null) && (!(this.filterId==0))) {
                if (!user.getId().toString().contains(this.filterId.toString())) {
                    continue;
                }
                found = true;
            }

            if ((this.filterName != null) && (!this.filterName.equals(""))) {
                if (!user.getFirstname().contains(this.filterName)) {
                    continue;
                }
                found = true;
            }

            if ((this.filterLastname != null) && (!this.filterLastname.equals(""))) {
                if (!user.getLastname().contains(this.filterLastname)) {
                    continue;
                }
                found = true;
            }

            if ((this.filterAge != null) && (!this.filterAge.equals(""))) {
                if (!user.getAge().contains(this.filterAge)) {
                    continue;
                }
                found = true;
            }

            if ((this.filterEmail != null) && (!this.filterEmail.equals(""))) {
                if (!user.getEmail().contains(this.filterEmail)) {
                    continue;
                }
                found = true;
            }

            if ((this.filterUsername != null) && (!this.filterUsername.equals(""))) {
                if (!user.getUsername().contains(this.filterUsername)) {
                    continue;
                }
                found = true;
            }

            if ((this.filterRoles != null) && (this.filterRoles.size() != 0)) {
                boolean userHasSearchedRoles = true;
                List<UserRole> userRoles = user.getUserRoles();
                for (UserRole searchedRole : this.filterRoles) {
                    if (!userRoles.contains(searchedRole)) {
                        userHasSearchedRoles = false;
                        break;
                    }
                }
                found = userHasSearchedRoles;
            }

            if (found) {
                System.out.println("Search found user " + user.getUsername());
                outList.add(user);
            }
        }

        return outList;
    }


    @Override
    public void setFilter(User user, boolean strict) {
        if (user != null) {
            this.filterId = (user.getId() == null) ? 0 : user.getId();
            this.filterName = (user.getFirstname() == null) ? "" : user.getFirstname();
            this.filterLastname = (user.getLastname() == null) ? "" : user.getLastname();
            this.filterAge = (user.getAge() == null) ? "" : user.getAge();
            this.filterEmail = (user.getEmail() == null) ? "" : user.getEmail();
            this.filterUsername = (user.getUsername() == null) ? "" : user.getUsername();
            this.filterRoles = user.getUserRoles();
            this.filterStrict = strict;
            this.isFilterActive = true;
        }
    }

    @Override
    public boolean isFilterSet() {
        return this.isFilterActive;
    }

    @Override
    public void removeFilter() {
        this.filterId = 0L;
        this.filterName = null;
        this.filterLastname = null;
        this.filterAge = null;
        this.filterEmail = null;
        this.filterUsername = null;
        this.filterRoles = null;
        this.isFilterActive = false;
    }


    @Override
    public void delete(Long id) {
        entityManager.createQuery("DELETE FROM User u WHERE u.id=:id")
                .setParameter("id", id)
                .executeUpdate();
    }
}