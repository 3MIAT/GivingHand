package com.givinghand.dto;

import java.util.List;


public class CreateCampaignDTO {

    private String title;
    private String description;
    private String category;
    private List<CampaignItemDTO> needed_items;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<CampaignItemDTO> getNeeded_items() {
        return needed_items;
    }

    public void setNeeded_items(List<CampaignItemDTO> needed_items) {
        this.needed_items = needed_items;
    }
}
