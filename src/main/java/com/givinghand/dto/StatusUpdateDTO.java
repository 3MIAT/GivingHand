package com.givinghand.dto;

/**
 * Carries a status value for campaign and donation status update operations.
 * Endpoints using this DTO are /api/campaigns/{id}/status and /api/donations/{id}/status.
 * Important notes: the value is parsed case-insensitively against the allowed lifecycle names.
 */
public class StatusUpdateDTO {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
