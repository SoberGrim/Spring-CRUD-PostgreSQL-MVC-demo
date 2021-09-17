package ru.jm.crud.model;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Nationalized;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.UnaryOperator;


@NoArgsConstructor
@Entity
@Table(name = "users", schema = "test")
public class User implements UserDetails {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private String checkAndCorrectEncoding(String str) {
        UnaryOperator<String> conv = (name) -> {
            char[] chArray = name.toCharArray();
            byte[] byteArray = new byte[chArray.length];
            for (int i = 0; i < chArray.length; i++) {
                byteArray[i] = (byte) chArray[i];
            }
            return new String(byteArray, StandardCharsets.UTF_8);
        };

        return ((str.indexOf('Ð') >= 0) || (str.indexOf('Ñ') >= 0)) ?
                conv.apply(str) : str;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "username", nullable = false, unique = true, length = 60)
    @Size(min = 4, message = "Username should be at least 4 characters")
    @Size(max = 60, message = "Username should be no more than 60 characters")
    private String username;

    @NotNull
    @Column(name = "password", nullable = false, length = 60)
    @Size(min = 4, message = "Password minimum length is 4 symbols")
    @Size(max = 60, message = "Password maximum length is 60 symbols")
    private String password;

    @NotNull
    @Column(name = "firstname", nullable = false, length = 60, columnDefinition = "NVARCHAR(60)")
    @NotBlank(message = "Name should not be empty")
    @Size(max = 60, message = "Name should be less than 60 characters")
    @Nationalized
    private String firstname;

    @NotNull
    @Column(name = "lastname", nullable = false, length = 120)
    @NotBlank(message = "Lastname should not be empty")
    @Size(max = 120, message = "Lastname should be less than 120 characters")
    @Nationalized
    private String lastname;

    @NotNull
    @Column(name = "age", nullable = false)
    @NotBlank(message = "Age should not be empty")
    @Pattern(regexp = "^[1]?[0-9]?[0-9]$", message = "Age format invalid")
    @Size(max = 3)
    private String age;

    @NotNull
    @Column(name = "email", nullable = false, unique = true, length = 120)
    @NotBlank(message = "Lastname should not be empty")
    @Pattern(regexp = "^[^@]+@[^@]+\\.[^@]+$", message = "Email format invalid, example: \"adress@email.com\"")
    @Size(min = 5, max = 120, message = "Email should be between 5 and 120 characters")
    private String email;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @Fetch(FetchMode.JOIN)
    @JoinTable(
            name = "user_roles",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")}
    )
    private List<UserRole> userRoles = new ArrayList<>();

    public User(String username, String password, String firstname, String lastname, String age, String email, List<UserRole> userRoles) {
        setUsername(username);
        setPassword(password);
        setFirstname(firstname);
        setLastname(lastname);
        setAge(age);
        setEmail(email);
        setUserRoles(userRoles);
    }

    public User(String username, String password, String name, String lastname, String age, String email, UserRole... userRoles) {
        this(username, password, name, lastname, age, email, Arrays.asList(userRoles));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = checkAndCorrectEncoding(username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password.startsWith("$2a$") && password.length() == 60) {
            System.out.println("encrypted password set: " + password);
            this.password = password;
        } else {
            int len = password.length();
            if ((len >= 4) && (len <= 60)) {
                System.out.println("plain password set: " + password + Arrays.toString(password.getBytes()));
                this.password = passwordEncoder.encode(password);
            } else {
                System.out.println("password not set, too long ot too short");
                this.password = password;
            }
        }
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = checkAndCorrectEncoding(firstname);
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = checkAndCorrectEncoding(lastname);
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = checkAndCorrectEncoding(email);
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public String getUserRole() {
        List<UserRole> roles = getUserRoles();
        StringBuilder strRoles = new StringBuilder();
        if (roles.size() > 0) {
            for (UserRole role:roles) {
                strRoles.append(role.getRole().replace("ROLE_", " "));
            }
            strRoles.deleteCharAt(0);
        } else {
            strRoles.append("NONE");
        }

        System.out.println("Returned role: "+strRoles);
        return strRoles.toString();
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles;
    }

    public void cloneUser(User user) {
        setId(user.id);
        setFirstname(user.firstname);
        setLastname(user.lastname);
        setAge(user.age);
        setEmail(user.email);
        setUsername(user.username);
        setPassword(user.password);
        setUserRoles(user.userRoles);
    }

    public boolean hasRole(UserRole role) {
        System.out.println("user contains " + role + "? ");
        return userRoles.contains(role);
    }

    public void addRole(UserRole role) {
        userRoles.add(role);
    }

}
