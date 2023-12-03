package com.fastspring.pizzaapi.model;

import com.fastspring.pizzaapi.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("users")
public class User implements Persistable<UUID> {

    @Id
    private UUID id;

    private String email;

    private String password;

    @Transient
    private Set<Role> roles;

    @Transient
    private boolean newRecord;

    @Override
    public boolean isNew() {
        return newRecord;
    }
}