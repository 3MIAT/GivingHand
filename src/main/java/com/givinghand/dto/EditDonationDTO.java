package com.givinghand.dto;

/**
 * Carries the editable fields for a committed donation.
 * Endpoint using this DTO is /api/donations/{id}/edit.
 * Important notes: only committed donations may use this payload successfully.
 */
public class EditDonationDTO {

    private String item_name;
    private Integer quantity;

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
}
