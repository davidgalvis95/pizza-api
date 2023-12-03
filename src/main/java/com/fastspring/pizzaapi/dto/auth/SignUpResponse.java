package com.fastspring.pizzaapi.dto.auth;

import com.fastspring.pizzaapi.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpResponse {
    UUID userId;
    String email;
    Set<Role> roles;
}
