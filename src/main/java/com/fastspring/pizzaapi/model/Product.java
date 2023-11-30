package com.fastspring.pizzaapi.model;

import com.fastspring.pizzaapi.model.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.domain.Persistable;


import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("product")
public class Product implements Persistable<UUID> {
    @Id
    @Column("product_id")
    private UUID productId;

    private String name;

    private ProductType type;

    @Transient
    private boolean newRecord;

    @Override
    @Transient
    public boolean isNew() {
        return this.newRecord;
    }

    @Override
    public UUID getId() {
        return productId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final Product other = (Product) obj;
        return Objects.equals(this.productId, other.productId)
                && Objects.equals(this.name, other.name)
                && this.type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, type);
    }
}
