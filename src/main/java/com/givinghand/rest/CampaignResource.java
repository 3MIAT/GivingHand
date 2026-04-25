package com.givinghand.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.givinghand.dto.CreateCampaignDTO;
import com.givinghand.dto.StatusUpdateDTO;
import com.givinghand.model.Campaign;
import com.givinghand.model.CampaignItem;
import com.givinghand.service.CampaignService;


@Path("/api/campaigns")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CampaignResource {

    @Inject
    private CampaignService campaignService;

    @POST
    @Path("/create")
    @RolesAllowed("ORGANIZATION")
    public Response createCampaign(CreateCampaignDTO dto) {
        try {
            Campaign campaign = campaignService.createCampaign(dto);
            Map<String, Object> response = new LinkedHashMap<String, Object>();
            response.put("message", "Campaign created successfully.");
            response.put("campaign_id", campaign.getId());
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (SecurityException e) {
            return buildError(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @PUT
    @Path("/{id}/status")
    @RolesAllowed("ORGANIZATION")
    public Response updateStatus(@PathParam("id") Long id, StatusUpdateDTO dto) {
        try {
            campaignService.updateCampaignStatus(id, dto == null ? null : dto.getStatus());
            return Response.ok(singleMessage("Campaign status updated successfully.")).build();
        } catch (SecurityException e) {
            return buildError(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @PUT
    @Path("/{id}/items")
    @RolesAllowed("ORGANIZATION")
    public Response updateItems(@PathParam("id") Long id, CreateCampaignDTO dto) {
        try {
            campaignService.updateCampaignItems(id, dto);
            return Response.ok(singleMessage("Campaign items updated successfully.")).build();
        } catch (SecurityException e) {
            return buildError(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @GET
    @RolesAllowed({ "DONOR", "ORGANIZATION" })
    public Response getCampaigns(@QueryParam("category") String category) {
        List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
        for (Campaign campaign : campaignService.listOpenCampaigns(category)) {
            response.add(toCampaignMap(campaign));
        }
        return Response.ok(response).build();
    }

    private Map<String, Object> toCampaignMap(Campaign campaign) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("campaign_id", campaign.getId());
        map.put("title", campaign.getTitle());
        map.put("description", campaign.getDescription());
        map.put("category", campaign.getCategory());
        map.put("status", campaign.getStatus().getApiValue());

        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (CampaignItem item : campaign.getNeededItems()) {
            Map<String, Object> itemMap = new LinkedHashMap<String, Object>();
            itemMap.put("item_name", item.getItemName());
            itemMap.put("target_quantity", item.getTargetQuantity());
            itemMap.put("received_quantity", item.getReceivedQuantity());
            items.add(itemMap);
        }
        map.put("needed_items", items);
        return map;
    }

    private Response buildError(Response.Status status, String message) {
        return Response.status(status).entity(singleMessage(message)).build();
    }

    private Map<String, Object> singleMessage(String message) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("message", message);
        return map;
    }
}
