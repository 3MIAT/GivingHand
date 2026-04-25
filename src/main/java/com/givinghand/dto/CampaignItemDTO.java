package com.givinghand.dto;


public class CampaignItemDTO {

    private String item_name;
    private Integer target_quantity;

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public Integer getTarget_quantity() {
        return target_quantity;
    }

    public void setTarget_quantity(Integer target_quantity) {
        this.target_quantity = target_quantity;
    }
}
