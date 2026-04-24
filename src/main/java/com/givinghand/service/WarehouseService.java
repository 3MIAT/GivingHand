package com.givinghand.service;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.jboss.ejb3.annotation.SecurityDomain;

import com.givinghand.dto.AllocateInventoryDTO;
import com.givinghand.dto.CreateWarehouseDTO;
import com.givinghand.dto.WarehouseInventoryDTO;
import com.givinghand.model.Campaign;
import com.givinghand.model.CampaignItem;
import com.givinghand.model.NotificationEventType;
import com.givinghand.model.Role;
import com.givinghand.model.User;
import com.givinghand.model.Warehouse;
import com.givinghand.model.WarehouseItem;

/**
 * Implements warehouse creation, inventory updates, warehouse lookup, and atomic resource allocation.
 * Endpoints using this service are /api/warehouse/create, /api/warehouse/{id}/add, /api/inventory/allocate, and /api/warehouse/{id}.
 * Important notes: allocate runs inside a required JTA transaction so stock and campaign received quantities succeed or fail together.
 */
@Stateless
@SecurityDomain("GivingHandRealm")
public class WarehouseService {

    @PersistenceContext(unitName = "givinghandPU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    @javax.inject.Inject
    private NotificationService notificationService;

    public Warehouse createWarehouse(CreateWarehouseDTO dto) {
        if (dto == null || isBlank(dto.getName())) {
            throw new IllegalArgumentException("Warehouse name is required.");
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setName(dto.getName().trim());
        warehouse.setOrganization(getCurrentOrganization());
        em.persist(warehouse);
        em.flush();
        return warehouse;
    }

    public void addInventory(Long warehouseId, WarehouseInventoryDTO dto) {
        if (dto == null || isBlank(dto.getItem_name()) || dto.getQuantity() == null || isBlank(dto.getCategory())) {
            throw new IllegalArgumentException("item_name, quantity, and category are required.");
        }
        if (dto.getQuantity().intValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        Warehouse warehouse = getOwnedWarehouse(warehouseId);
        WarehouseItem item = findWarehouseItem(warehouse.getItems(), dto.getItem_name());

        if (item == null) {
            item = new WarehouseItem();
            item.setWarehouse(warehouse);
            item.setItemName(dto.getItem_name().trim());
            item.setCategory(dto.getCategory().trim());
            item.setQuantity(dto.getQuantity().intValue());
            item.setOriginalQuantity(dto.getQuantity().intValue());
            warehouse.getItems().add(item);
        } else {
            item.setCategory(dto.getCategory().trim());
            item.setQuantity(item.getQuantity() + dto.getQuantity().intValue());
            item.setOriginalQuantity(item.getOriginalQuantity() + dto.getQuantity().intValue());
        }

        em.merge(warehouse);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void allocate(AllocateInventoryDTO dto) {
        if (dto == null || dto.getWarehouse_id() == null || dto.getCampaign_id() == null || isBlank(dto.getItem_name())
                || dto.getQuantity() == null) {
            throw new IllegalArgumentException("warehouse_id, campaign_id, item_name, and quantity are required.");
        }
        if (dto.getQuantity().intValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        User organization = getCurrentOrganization();
        Warehouse warehouse = getOwnedWarehouse(dto.getWarehouse_id());
        Campaign campaign = getOwnedCampaign(dto.getCampaign_id(), organization);

        WarehouseItem warehouseItem = findWarehouseItem(warehouse.getItems(), dto.getItem_name());
        if (warehouseItem == null) {
            throw new IllegalArgumentException("Warehouse item not found.");
        }
        if (warehouseItem.getQuantity() < dto.getQuantity().intValue()) {
            throw new IllegalArgumentException("Insufficient stock in warehouse.");
        }

        CampaignItem campaignItem = findCampaignItem(campaign.getNeededItems(), dto.getItem_name());
        int updatedReceived = campaignItem.getReceivedQuantity() + dto.getQuantity().intValue();
        if (updatedReceived > campaignItem.getTargetQuantity()) {
            throw new IllegalArgumentException("Allocation exceeds campaign target quantity.");
        }

        warehouseItem.setQuantity(warehouseItem.getQuantity() - dto.getQuantity().intValue());
        campaignItem.setReceivedQuantity(updatedReceived);

        em.merge(warehouse);
        em.merge(campaign);

        if (warehouseItem.getOriginalQuantity() > 0
                && warehouseItem.getQuantity() < (warehouseItem.getOriginalQuantity() * 0.1d)) {
            notificationService.sendNotification(NotificationEventType.STOCK_LOW_ALERT,
                    "Warning: " + warehouseItem.getItemName() + " stock is below 10% in Warehouse " + warehouse.getId()
                            + ".");
        }
    }

    public Warehouse getWarehouse(Long warehouseId) {
        Warehouse warehouse = getOwnedWarehouse(warehouseId);
        warehouse.getItems().size();
        return warehouse;
    }

    private Campaign getOwnedCampaign(Long campaignId, User organization) {
        try {
            Campaign campaign = em.createQuery(
                    "SELECT DISTINCT c FROM Campaign c LEFT JOIN FETCH c.neededItems JOIN FETCH c.organization WHERE c.id = :id",
                    Campaign.class).setParameter("id", campaignId).getSingleResult();
            if (!campaign.getOrganization().getId().equals(organization.getId())) {
                throw new SecurityException("You can only allocate resources to your own campaigns.");
            }
            return campaign;
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Campaign not found.");
        }
    }

    private Warehouse getOwnedWarehouse(Long warehouseId) {
        User organization = getCurrentOrganization();
        try {
            Warehouse warehouse = em.createQuery(
                    "SELECT DISTINCT w FROM Warehouse w LEFT JOIN FETCH w.items JOIN FETCH w.organization WHERE w.id = :id",
                    Warehouse.class).setParameter("id", warehouseId).getSingleResult();
            if (!warehouse.getOrganization().getId().equals(organization.getId())) {
                throw new SecurityException("You can only manage your own warehouses.");
            }
            return warehouse;
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Warehouse not found.");
        }
    }

    private WarehouseItem findWarehouseItem(List<WarehouseItem> items, String itemName) {
        for (WarehouseItem item : items) {
            if (item.getItemName().equalsIgnoreCase(itemName.trim())) {
                return item;
            }
        }
        return null;
    }

    private CampaignItem findCampaignItem(List<CampaignItem> items, String itemName) {
        for (CampaignItem item : items) {
            if (item.getItemName().equalsIgnoreCase(itemName.trim())) {
                return item;
            }
        }
        throw new IllegalArgumentException("Campaign item not found.");
    }

    private User getCurrentOrganization() {
        User user = getCurrentUser();
        if (user.getRole() != Role.ORGANIZATION) {
            throw new SecurityException("Only organization users can perform this action.");
        }
        return user;
    }

    private User getCurrentUser() {
        String email = sessionContext.getCallerPrincipal() == null ? null : sessionContext.getCallerPrincipal().getName();
        if (isBlank(email)) {
            throw new SecurityException("Authenticated user not found.");
        }
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class).setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new SecurityException("Authenticated user not found.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
