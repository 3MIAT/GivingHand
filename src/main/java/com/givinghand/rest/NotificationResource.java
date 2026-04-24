package com.givinghand.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.givinghand.model.NotificationEvent;
import com.givinghand.service.NotificationService;

/**
 * Exposes the notification history REST API backed by stored JMS events.
 * Endpoint: GET /api/notifications.
 * Important notes: the response body is a JSON array of objects containing only event_type, message, and timestamp.
 */
@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @Inject
    private NotificationService notificationService;

    @GET
    @RolesAllowed({ "DONOR", "ORGANIZATION" })
    public Response getNotifications() {
        List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
        for (NotificationEvent event : notificationService.getNotifications()) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("event_type", event.getEventType());
            map.put("message", event.getMessage());
            map.put("timestamp", event.getTimestamp());
            response.add(map);
        }
        return Response.ok(response).build();
    }
}
