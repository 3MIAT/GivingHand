package com.givinghand.dto;

import java.util.List;

/**
 * Carries the campaign creation and item-update payload described in the project PDF.
 * Endpoints using this DTO are /api/campaigns/create and /api/campaigns/{id}/items.
 * Important notes: needed_items is required and each item must include item_name and target_quantity.
 */
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
