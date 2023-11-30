package com.fastspring.pizzaapi.model;

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
@Table("inventory")
public class Inventory implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("product_id")
    private UUID productId;

    @Column("available_quantity")
    private Integer availableQuantity;

    @Transient
    private Product product;

    @Transient
    private boolean newRecord;

    @Override
    @Transient
    public boolean isNew() {
        return this.newRecord;
    }
}
