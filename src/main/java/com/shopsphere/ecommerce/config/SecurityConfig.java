package com.shopsphere.ecommerce.config;

import com.shopsphere.ecommerce.controller.AuthController;
import com.shopsphere.ecommerce.model.Role;
import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            User user = userRepository.findByEmailIgnoreCase(email.trim())
                    .orElseThrow(() -> new UsernameNotFoundException("No account found with email: " + email));
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
            );
        };
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String email = authentication.getName();
            User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
            HttpSession session = request.getSession();
            if (user != null) {
                session.setAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY, user);
            }

            if (user != null && user.getRole() == Role.ROLE_ADMIN) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            }

            SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
            if (savedRequest != null) {
                response.sendRedirect(savedRequest.getRedirectUrl());
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/", "/category/**", "/search/**", "/product/**").permitAll()
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/admin/**", "/api/orders/**").hasRole("ADMIN")
                .requestMatchers("/cart/**", "/checkout/**", "/order/**", "/profile/**", "/wishlist/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            );

        return http.build();
    }
}
