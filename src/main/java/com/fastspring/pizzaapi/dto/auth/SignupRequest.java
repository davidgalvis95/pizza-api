package com.fastspring.pizzaapi.dto.auth;

import com.fastspring.pizzaapi.model.enums.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.Set;

@Value
@Valid
public class SignupRequest {

    @NotNull
    String email;

    @NotNull
    String password;

    @NotNull
    @Size(min = 1)
    Set<Role> roles;
}
