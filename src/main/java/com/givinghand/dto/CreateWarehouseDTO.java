package com.givinghand.dto;

/**
 * Carries the minimal warehouse creation payload.
 * Endpoint using this DTO is /api/warehouse/create.
 * Important notes: the warehouse name is required so organizations can distinguish their virtual stores.
 */
public class CreateWarehouseDTO {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
