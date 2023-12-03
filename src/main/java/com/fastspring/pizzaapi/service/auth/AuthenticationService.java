package com.fastspring.pizzaapi.service.auth;

import com.fastspring.pizzaapi.dto.auth.UserDetailsInfo;
import com.fastspring.pizzaapi.dto.auth.LoginRequest;
import com.fastspring.pizzaapi.dto.auth.LoginResponse;
import com.fastspring.pizzaapi.dto.auth.SignUpResponse;
import com.fastspring.pizzaapi.dto.auth.SignupRequest;
import com.fastspring.pizzaapi.model.User;
import com.fastspring.pizzaapi.model.UserRole;
import com.fastspring.pizzaapi.model.enums.Role;
import com.fastspring.pizzaapi.repository.UserRepository;
import com.fastspring.pizzaapi.repository.UserRoleRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AuthenticationService {

    private final ReactiveAuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final PasswordEncoder encoder;

    private final UserRoleRepository userRoleRepository;

    private final UserRepository userRepository;

    public AuthenticationService(final JwtService jwtService,
                                 final PasswordEncoder encoder,
                                 final UserRoleRepository userRoleRepository,
                                 final UserRepository userRepository,
                                 final UserDetailsService userDetailsService) {

        this.authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        this.jwtService = jwtService;
        this.encoder = encoder;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
    }

    public Mono<SignUpResponse> signUp(final SignupRequest signupRequest) {

        final boolean requestHasAdminRole = signupRequest.getRoles()
                .stream()
                .anyMatch(role -> role.equals(Role.ADMIN));
        if (requestHasAdminRole) {
            return Mono.error(
                    new BadCredentialsException("Users cannot be created as ADMIN using this endpoint, " +
                            "create the user as USER and ask an already admin user to mark that user as ADMIN")
            );
        }

        return userRepository.findByEmail(signupRequest.getEmail())
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(user -> user.isPresent() ?
                        Mono.error(new BadCredentialsException("User already exists")) :
                        saveUserAndReturnResponse(signupRequest));
    }

    public Mono<LoginResponse> loginUser(final LoginRequest loginRequest) {
        return authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
                )
                .map(auth -> checkAuthAndBuildResponse(loginRequest, auth));
    }

    private Mono<SignUpResponse> saveUserAndReturnResponse(final SignupRequest signupRequest) {
        return userRepository.save(User.builder()
                        .id(UUID.randomUUID())
                        .email(signupRequest.getEmail())
                        .roles(signupRequest.getRoles())
                        .password(encoder.encode(signupRequest.getPassword()))
                        .newRecord(true)
                        .build())
                .flatMap(user -> {
                    final List<UserRole> userRoles = signupRequest.getRoles().stream()
                            .map(role -> UserRole.builder()
                                    .id(UUID.randomUUID())
                                    .userId(user.getId())
                                    .newRecord(true)
                                    .role(role)
                                    .build())
                            .toList();
                    return userRoleRepository.saveAll(userRoles).collectList()
                            .map(roles -> SignUpResponse.builder()
                                    .userId(user.getId())
                                    .email(user.getEmail())
                                    .roles(signupRequest.getRoles())
                                    .build());
                });
    }

    private LoginResponse checkAuthAndBuildResponse(final LoginRequest loginRequest, final Authentication auth) {
        if (auth.isAuthenticated()) {
            final UserDetailsInfo userDetails = (UserDetailsInfo) auth.getPrincipal();
            final Set<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            final String jwt = jwtService.generateToken(loginRequest.getEmail(), Map.of("auth", roles));
            return LoginResponse.builder()
                    .accessToken(jwt)
                    .tokenType("Bearer")
                    .roles(roles.stream().map(role -> role.substring(5)).collect(Collectors.toSet()))
                    .email(userDetails.getUsername())
                    .id(userDetails.getId())
                    .build();
        } else {
            throw new BadCredentialsException("invalid user request");
        }
    }
}
