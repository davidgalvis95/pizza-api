package com.fastspring.pizzaapi.service.auth;

import com.fastspring.pizzaapi.dto.auth.UserDetailsInfo;
import com.fastspring.pizzaapi.model.UserRole;
import com.fastspring.pizzaapi.repository.UserRepository;
import com.fastspring.pizzaapi.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;

@Component
public class UserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    @Autowired
    public UserDetailsService(final UserRepository userRepository, final UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }
    @Override
    public Mono<UserDetails> findByUsername(final String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("user not found " + email)))
                .flatMap(user -> userRoleRepository.findUserRolesByUserId(user.getId())
                        .map(UserRole::getRole)
                        .collectList()
                        .map(roles -> {
                            user.setRoles(new HashSet<>(roles));
                            return user;
                        }))
                .map(UserDetailsInfo::new);
    }
}
