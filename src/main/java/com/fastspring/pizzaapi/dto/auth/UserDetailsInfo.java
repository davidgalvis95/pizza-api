package com.fastspring.pizzaapi.dto.auth;

import com.fastspring.pizzaapi.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserDetailsInfo implements UserDetails {

    private UUID id;
    private String username;
    private String password;
    private List<GrantedAuthority> authorities;
    private static final String ROLE_PREFIX = "ROLE_";

    public UserDetailsInfo(User user) {
        id = user.getId();
        username = user.getEmail();
        password = user.getPassword();
        authorities = user.getRoles().stream()
                .map(r -> ROLE_PREFIX + r.name())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
}
