package com.givinghand.dto;

/**
 * Carries the inventory item payload shown in the warehouse add API sample.
 * Endpoint using this DTO is /api/warehouse/{id}/add.
 * Important notes: item_name, quantity, and category match the PDF field names exactly.
 */
public class WarehouseInventoryDTO {

    private String item_name;
    private Integer quantity;
    private String category;

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
