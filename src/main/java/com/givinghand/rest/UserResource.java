package com.givinghand.rest;

import com.givinghand.dto.*;
import com.givinghand.service.UserService;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.Map;

@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private UserService userService;

    @POST
    @Path("/register")
    @PermitAll
    public Response register(RegisterDTO dto) {
        try {
            userService.register(dto);
            Map<String, String> res = new HashMap<>();
            res.put("message", "User registered successfully.");
            return Response.status(Response.Status.CREATED).entity(res).build();
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(LoginDTO dto) {
        try {
            String token = userService.login(dto);
            Map<String, String> res = new HashMap<>();
            res.put("message", "Login successful.");
            res.put("token", token);
            return Response.ok(res).build();
        } catch (SecurityException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Invalid email or password.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(err).build();
        }
    }

    @PUT
    @Path("/profile/{userId}")
    @RolesAllowed({"DONOR", "ORGANIZATION"})
    public Response updateProfile(@PathParam("userId") Long userId, ProfileDTO dto) {
        try {
            userService.updateProfile(userId, dto);
            Map<String, String> res = new HashMap<>();
            res.put("message", "Profile updated successfully.");
            return Response.ok(res).build();
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
    }
}