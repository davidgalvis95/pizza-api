package com.fastspring.pizzaapi.model.enums;

public enum PizzaSize {
    SMALL("Small"), MEDIUM("Medium"), BIG("Big"), NOT_APPLICABLE("NoSize");

    String size;
    PizzaSize(String size) {
    }

    public String getSize() {
        return size;
    }
}
