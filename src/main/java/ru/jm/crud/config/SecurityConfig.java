package ru.jm.crud.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.jm.crud.service.UserService;

import java.util.function.BiPredicate;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    private final UserService userService;
    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }


    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService::getByUsername)
                .passwordEncoder(passwordEncoder());
        //auth.inMemoryAuthentication().withUser("ADMIN").password(passwordEncoder()
        //       .encode("ADMIN")).roles("ROLE_ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        BiPredicate<Authentication, String> inRole =
                (a, s) -> a.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(s));

        http
                .formLogin()
                .loginPage("/login")
                .successHandler((request, response, auth) ->
                        response.sendRedirect(inRole.test(auth, "ROLE_ADMIN") ? "/admin"
                                : inRole.test(auth, "ROLE_USER") ? "/user" : "/welcome"))
                .loginProcessingUrl("/login")
                .usernameParameter("j_username")
                .passwordParameter("j_password")
                .permitAll();


        http.logout()
                // разрешаем делать логаут всем
                .permitAll()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .and().csrf().disable();

        http
                .authorizeRequests()
                //страницы аутентификаци доступна всем
                .antMatchers("/register").anonymous();

        http
                .authorizeRequests()
                .antMatchers("/register").permitAll()
                .antMatchers("/*.js","/*.css").permitAll();

        http
                .authorizeRequests()
                //страницы аутентификаци доступна всем
                .antMatchers("/login").anonymous()
                // защищенные URL
                .antMatchers("/admin/","/admin").access("hasRole('ROLE_ADMIN')")
                .antMatchers("/user/","/user").access("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
                .antMatchers("/welcome/","/welcome").access("hasAnyRole('ROLE_GUEST','ROLE_USER','ROLE_ADMIN')")
                .anyRequest().authenticated();
    }
}
