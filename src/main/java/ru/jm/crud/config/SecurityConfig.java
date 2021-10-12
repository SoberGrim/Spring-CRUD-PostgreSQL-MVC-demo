package ru.jm.crud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.jm.crud.service.UserService;

import javax.annotation.Resource;
import java.util.function.BiPredicate;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)

public class SecurityConfig {

    static BiPredicate<Authentication, String> inRole =
            (a, s) -> a.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(s));

    static public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    @Configuration
    @Order(2)
    public static class SecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Resource(name = "UserService")
        private UserService userService;

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userService::getByLogin).passwordEncoder(passwordEncoder());
            //        auth.inMemoryAuthentication().withUser("ADMIN").password(passwordEncoder().encode("ADMIN")).roles("ADMIN");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http
                    .authorizeRequests()
                    .antMatchers("/css/*","/js/*","/favicon.png").permitAll()
                    .antMatchers("/register","/login","/logout401","/logout402","/slogin").permitAll()
                    .antMatchers("/admin/","/admin").access("hasRole('ROLE_ADMIN')")
                    .antMatchers("/user/","/user").access("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
                    .antMatchers("/guest/","/guest").access("hasAnyRole('ROLE_GUEST','ROLE_USER','ROLE_ADMIN')");
                    //.anyRequest().authenticated();


            http
                    .formLogin().loginPage("/login").successHandler((request, response, auth) ->
                            response.sendRedirect(inRole.test(auth, "ROLE_ADMIN") ? "/admin"
                                    : inRole.test(auth, "ROLE_USER") ? "/user" : "/guest"))
                    .loginProcessingUrl("/login")
                    .usernameParameter("j_username").passwordParameter("j_password")
                    .and()
                    .logout().logoutUrl("/logout").logoutSuccessUrl("/noauth")
                    .and()
                    .csrf().disable();
        }
    }


    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Resource(name = "UserService")
        private UserService userService;

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(userService::getByLogin)
                .passwordEncoder(passwordEncoder());
        }


        protected void configure(HttpSecurity http) throws Exception {
            //http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

            http.authorizeRequests()
                    .antMatchers("/css/*","/js/*").permitAll()
                    .antMatchers("/favicon.png").permitAll();

            http
                    .antMatcher("/basicauth/**")
                    .authorizeRequests()
                    .antMatchers("/basicauth/","/basicauth").access("hasAnyRole('ROLE_GUEST','ROLE_USER','ROLE_ADMIN')")
                    .antMatchers("/admin/","/admin").access("hasRole('ROLE_ADMIN')")
                    .antMatchers("/user/","/user").access("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
                    .antMatchers("/guest/","/guest").access("hasAnyRole('ROLE_GUEST','ROLE_USER','ROLE_ADMIN')")
                    .and()
                    .httpBasic()
                    .and()
                    .sessionManagement().disable();

        }
    }
}