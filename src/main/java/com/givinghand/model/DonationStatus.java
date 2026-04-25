package com.givinghand.model;


public enum DonationStatus {
    COMMITTED("Committed"),
    RECEIVED("Received"),
    DISTRIBUTED("Distributed");

    private final String apiValue;

    DonationStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static DonationStatus fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Donation status is required.");
        }
        for (DonationStatus status : values()) {
            if (status.apiValue.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Donation status must be Committed, Received, or Distributed.");
    }
}
