package com.fastspring.pizzaapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class StandardResponse<T> {
    private T payload;
    private String message;
    private String error;
}
