package com.fastspring.pizzaapi.service.auth;

import com.fastspring.pizzaapi.dto.auth.LoginRequest;
import com.fastspring.pizzaapi.dto.auth.LoginResponse;
import com.fastspring.pizzaapi.dto.auth.SignUpResponse;
import com.fastspring.pizzaapi.dto.auth.SignupRequest;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

    Mono<SignUpResponse> signUp(final SignupRequest signupRequest);

    Mono<LoginResponse> loginUser(final LoginRequest loginRequest);
}
