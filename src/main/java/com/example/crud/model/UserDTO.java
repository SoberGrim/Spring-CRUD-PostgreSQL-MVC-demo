package com.example.crud.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Component
@NoArgsConstructor
public class UserDTO {

    private String id;

    @NotNull
    @NotBlank(message = "Name should not be empty")
    @Size(max = 60, message = "Name should be less than 60 characters")
    private String firstname;

    @NotNull
    @NotBlank(message = "Lastname should not be empty")
    @Size(max = 120, message = "Lastname should be less than 120 characters")
    private String lastname;

    @NotNull
    @NotBlank(message = "Age should not be empty")
    @Pattern(regexp = "^[1]?[0-9]?[0-9]$", message = "Age should 1 to 199 years")
    @Size(max = 3)
    private String age;

    @NotNull
    @NotBlank(message = "Email should not be empty")
    @Pattern(regexp = "^[^@]+@[^@]+\\.[^@]+$", message = "Email format invalid, example: \"adress@email.com\"")
    @Size(min = 5, max = 120, message = "Email should be between 5 and 120 characters")
    private String email;

    @NotNull
    @Size(min = 4, message = "Username should be at least 4 characters")
    @Size(max = 60, message = "Username should be no more than 60 characters")
    private String username;

    @NotNull
    @Size(min = 4, message = "Password minimum length is 4 symbols")
    @Size(max = 60, message = "Password maximum length is 60 symbols")
    private String password;

    private String roleStr;

    private boolean errorsPresent;
}
