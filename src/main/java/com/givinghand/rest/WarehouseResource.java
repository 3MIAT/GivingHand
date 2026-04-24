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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.givinghand.dto.AllocateInventoryDTO;
import com.givinghand.dto.CreateWarehouseDTO;
import com.givinghand.dto.WarehouseInventoryDTO;
import com.givinghand.model.Warehouse;
import com.givinghand.model.WarehouseItem;
import com.givinghand.service.WarehouseService;

/**
 * Exposes warehouse and inventory REST APIs, including the JTA-protected allocation endpoint.
 * Endpoints: POST /api/warehouse/create, POST /api/warehouse/{id}/add, POST /api/inventory/allocate, GET /api/warehouse/{id}.
 * Important notes: allocate is atomic and returns the exact success message shown in the PDF sample.
 */
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WarehouseResource {

    @Inject
    private WarehouseService warehouseService;

    @POST
    @Path("/warehouse/create")
    @RolesAllowed("ORGANIZATION")
    public Response createWarehouse(CreateWarehouseDTO dto) {
        try {
            Warehouse warehouse = warehouseService.createWarehouse(dto);
            Map<String, Object> response = new LinkedHashMap<String, Object>();
            response.put("message", "Warehouse created successfully.");
            response.put("warehouse_id", warehouse.getId());
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (SecurityException e) {
            return buildError(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @POST
    @Path("/warehouse/{id}/add")
    @RolesAllowed("ORGANIZATION")
    public Response addInventory(@PathParam("id") Long id, WarehouseInventoryDTO dto) {
        try {
            warehouseService.addInventory(id, dto);
            return Response.ok(singleMessage("Inventory updated successfully.")).build();
        } catch (SecurityException e) {
            return buildError(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @POST
    @Path("/inventory/allocate")
    @RolesAllowed("ORGANIZATION")
    public Response allocateInventory(AllocateInventoryDTO dto) {
        try {
            warehouseService.allocate(dto);
            return Response.ok(singleMessage("Resources allocated from warehouse to campaign successfully.")).build();
        } catch (SecurityException e) {
            return buildError(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @GET
    @Path("/warehouse/{id}")
    @RolesAllowed("ORGANIZATION")
    public Response getWarehouse(@PathParam("id") Long id) {
        try {
            Warehouse warehouse = warehouseService.getWarehouse(id);
            return Response.ok(toWarehouseMap(warehouse)).build();
        } catch (SecurityException e) {
            return buildError(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    private Map<String, Object> toWarehouseMap(Warehouse warehouse) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("warehouse_id", warehouse.getId());
        map.put("name", warehouse.getName());

        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (WarehouseItem item : warehouse.getItems()) {
            Map<String, Object> itemMap = new LinkedHashMap<String, Object>();
            itemMap.put("item_name", item.getItemName());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("category", item.getCategory());
            items.add(itemMap);
        }
        map.put("items", items);
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
