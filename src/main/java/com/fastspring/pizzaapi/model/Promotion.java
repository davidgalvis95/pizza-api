package com.fastspring.pizzaapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fastspring.pizzaapi.model.enums.DescriptiveCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("promotion")
public class Promotion implements Persistable<UUID> {
    @Id
    private UUID code;

    @Column("descriptive_code")
    private DescriptiveCode descriptiveCode;

    private String description;

    private Boolean active;

    @Transient
    @JsonIgnore
    @Override
    public UUID getId() {
        return code;
    }

    @Transient
    @JsonIgnore
    private boolean newRecord;

    @Transient
    @JsonIgnore
    @Override
    public boolean isNew() {
        return newRecord;
    }
}
