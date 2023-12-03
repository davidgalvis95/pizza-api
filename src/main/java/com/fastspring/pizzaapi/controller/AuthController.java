package com.fastspring.pizzaapi.controller;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.dto.auth.LoginRequest;
import com.fastspring.pizzaapi.dto.auth.LoginResponse;
import com.fastspring.pizzaapi.dto.auth.SignUpResponse;
import com.fastspring.pizzaapi.dto.auth.SignupRequest;
import com.fastspring.pizzaapi.service.auth.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/user")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<StandardResponse<LoginResponse>>> login(@RequestBody LoginRequest loginRequest) {
        return authenticationService.loginUser(loginRequest)
                .map(response -> ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<LoginResponse>builder()
                                .payload(response)
                                .message("User " +response.getEmail()+ " logged in")
                                .build()
                        ));
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<StandardResponse<SignUpResponse>>> signup(@RequestBody SignupRequest signupRequest) {
        return authenticationService.signUp(signupRequest)
                .map(response -> ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(StandardResponse.<SignUpResponse>builder()
                                .payload(response)
                                .message("User " +response.getEmail()+ " created with id " + response.getUserId())
                                .build()
                        ));
    }

//    @GetMapping("/get")
//    public Mono<ResponseEntity<StandardResponse<SignUpResponse>>> signup(@RequestParam String userEmail) {
//        return authenticationService.findUserByEmail(signupRequest)
//                .map(response -> ResponseEntity.status(HttpStatus.ACCEPTED)
//                        .body(StandardResponse.<SignUpResponse>builder()
//                                .payload(response)
//                                .message("User " +response.getEmail()+ " created with id " + response.getUserId())
//                                .build()
//                        ));
//    }
}
