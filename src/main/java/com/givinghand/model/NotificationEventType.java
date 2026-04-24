package com.givinghand.model;

/**
 * Defines the JMS notification event names required by the project specification.
 * Endpoints using these values are /api/notifications and any business endpoint that emits queue messages.
 * Important notes: the stored and queued JSON payloads must use these exact uppercase event_type strings.
 */
public enum NotificationEventType {
    STOCK_LOW_ALERT,
    DONATION_RECEIVED
}
