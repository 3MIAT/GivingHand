package com.givinghand.model;

/**
 * Represents the allowed campaign lifecycle values used by the campaign management endpoints.
 * Endpoints using this enum include /api/campaigns/create, /api/campaigns/{id}/status, and /api/campaigns.
 * Important notes: request parsing is case-insensitive, while API responses return the title-case values from the PDF.
 */
public enum CampaignStatus {
    OPEN("Open"),
    PAUSED("Paused"),
    COMPLETED("Completed");

    private final String apiValue;

    CampaignStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static CampaignStatus fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Campaign status is required.");
        }
        for (CampaignStatus status : values()) {
            if (status.apiValue.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Campaign status must be Open, Paused, or Completed.");
    }
}
