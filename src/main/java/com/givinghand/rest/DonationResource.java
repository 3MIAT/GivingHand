package com.givinghand.rest;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.givinghand.dto.CommitDonationDTO;
import com.givinghand.dto.EditDonationDTO;
import com.givinghand.dto.StatusUpdateDTO;
import com.givinghand.model.Donation;
import com.givinghand.service.DonationService;

/**
 * Exposes the donation commitment and lifecycle REST APIs for donors and organizations.
 * Endpoints: POST /api/donations/commit, PUT /api/donations/{id}/status, PUT /api/donations/{id}/edit, DELETE /api/donations/{id}/cancel.
 * Important notes: edit and cancel are allowed only while the donation is still Committed.
 */
@Path("/api/donations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DonationResource {

    @Inject
    private DonationService donationService;

    @POST
    @Path("/commit")
    @RolesAllowed("DONOR")
    public Response commitDonation(CommitDonationDTO dto) {
        try {
            Donation donation = donationService.commitDonation(dto);
            Map<String, Object> response = new LinkedHashMap<String, Object>();
            response.put("message", "Commitment recorded. Please drop off items at the warehouse.");
            response.put("donation_id", donation.getId());
            return Response.ok(response).build();
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
            donationService.updateDonationStatus(id, dto == null ? null : dto.getStatus());
            return Response.ok(singleMessage("Donation status updated successfully.")).build();
        } catch (SecurityException e) {
            return buildError(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @PUT
    @Path("/{id}/edit")
    @RolesAllowed("DONOR")
    public Response editDonation(@PathParam("id") Long id, EditDonationDTO dto) {
        try {
            donationService.editDonation(id, dto);
            return Response.ok(singleMessage("Donation commitment updated successfully.")).build();
        } catch (SecurityException e) {
            return buildError(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}/cancel")
    @RolesAllowed("DONOR")
    public Response cancelDonation(@PathParam("id") Long id) {
        try {
            donationService.cancelDonation(id);
            return Response.ok(singleMessage("Donation commitment cancelled successfully.")).build();
        } catch (SecurityException e) {
            return buildError(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }
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
