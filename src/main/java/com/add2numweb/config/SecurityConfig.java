package com.add2numweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final String technicianUsername;
    private final String technicianPassword;

    public SecurityConfig(
            @Value("${app.security.technician.username}") String technicianUsername,
            @Value("${app.security.technician.password}") String technicianPassword) {
        this.technicianUsername = technicianUsername;
        this.technicianPassword = technicianPassword;
    }

    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername(technicianUsername)
                        .password("{noop}" + technicianPassword)
                        .roles("TECHNICIAN")
                        .build());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/calculate", "/api/calculate").permitAll()
                        .requestMatchers("/api/workorders").hasAuthority("ROLE_TECHNICIAN")
                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> {});
        return http.build();
    }
}
