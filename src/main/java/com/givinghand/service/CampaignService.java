package com.givinghand.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.givinghand.dto.CampaignItemDTO;
import com.givinghand.dto.CreateCampaignDTO;
import com.givinghand.model.Campaign;
import com.givinghand.model.CampaignItem;
import com.givinghand.model.CampaignStatus;
import com.givinghand.model.Role;
import com.givinghand.model.User;


@Stateless
public class CampaignService {

    @PersistenceContext(unitName = "givinghandPU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    public Campaign createCampaign(CreateCampaignDTO dto) {
        User organization = getCurrentOrganization();
        validateCampaignPayload(dto, true);

        Campaign campaign = new Campaign();
        campaign.setTitle(dto.getTitle().trim());
        campaign.setDescription(dto.getDescription().trim());
        campaign.setCategory(dto.getCategory().trim());
        campaign.setStatus(CampaignStatus.OPEN);
        campaign.setOrganization(organization);
        em.persist(campaign);

        applyCampaignItems(campaign, dto.getNeeded_items(), false);
        em.flush();
        return campaign;
    }

    public void updateCampaignStatus(Long campaignId, String statusValue) {
        Campaign campaign = getOwnedCampaign(campaignId);
        campaign.setStatus(CampaignStatus.fromValue(statusValue));
        em.merge(campaign);
    }

    public void updateCampaignItems(Long campaignId, CreateCampaignDTO dto) {
        Campaign campaign = getOwnedCampaign(campaignId);
        validateCampaignPayload(dto, false);
        applyCampaignItems(campaign, dto.getNeeded_items(), true);
        em.merge(campaign);
    }

    public List<Campaign> listOpenCampaigns(String category) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT c FROM Campaign c LEFT JOIN FETCH c.neededItems WHERE c.status = :status");
        if (category != null && !category.trim().isEmpty()) {
            jpql.append(" AND LOWER(c.category) = :category");
        }
        jpql.append(" ORDER BY c.id");

        javax.persistence.TypedQuery<Campaign> query = em.createQuery(jpql.toString(), Campaign.class)
                .setParameter("status", CampaignStatus.OPEN);
        if (category != null && !category.trim().isEmpty()) {
            query.setParameter("category", category.trim().toLowerCase());
        }
        return query.getResultList();
    }

    public Campaign findCampaign(Long campaignId) {
        try {
            Campaign campaign = em.createQuery(
                    "SELECT DISTINCT c FROM Campaign c LEFT JOIN FETCH c.neededItems WHERE c.id = :id", Campaign.class)
                    .setParameter("id", campaignId).getSingleResult();
            campaign.getNeededItems().size();
            return campaign;
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Campaign not found.");
        }
    }

    private void applyCampaignItems(Campaign campaign, List<CampaignItemDTO> items, boolean preserveReceived) {
        List<CampaignItem> currentItems = campaign.getNeededItems();
        List<CampaignItem> existingItems = new ArrayList<CampaignItem>(currentItems);
        currentItems.clear();

        for (CampaignItemDTO dto : items) {
            CampaignItem item = findExistingItem(existingItems, dto.getItem_name());
            if (item == null) {
                item = new CampaignItem();
                item.setReceivedQuantity(0);
            }

            item.setCampaign(campaign);
            item.setItemName(dto.getItem_name().trim());
            item.setTargetQuantity(dto.getTarget_quantity().intValue());

            if (!preserveReceived) {
                item.setReceivedQuantity(0);
            } else if (item.getReceivedQuantity() > item.getTargetQuantity()) {
                throw new IllegalArgumentException(
                        "Target quantity for " + item.getItemName() + " cannot be less than received quantity.");
            }

            currentItems.add(item);
        }
    }

    private CampaignItem findExistingItem(List<CampaignItem> items, String itemName) {
        for (CampaignItem item : items) {
            if (item.getItemName().equalsIgnoreCase(itemName.trim())) {
                return item;
            }
        }
        return null;
    }

    private void validateCampaignPayload(CreateCampaignDTO dto, boolean requireMainFields) {
        if (dto == null) {
            throw new IllegalArgumentException("Campaign data is required.");
        }
        if (requireMainFields) {
            if (isBlank(dto.getTitle()) || isBlank(dto.getDescription()) || isBlank(dto.getCategory())) {
                throw new IllegalArgumentException("Title, description, and category are required.");
            }
        }
        if (dto.getNeeded_items() == null || dto.getNeeded_items().isEmpty()) {
            throw new IllegalArgumentException("At least one needed item is required.");
        }

        Set<String> names = new HashSet<String>();
        for (CampaignItemDTO item : dto.getNeeded_items()) {
            if (item == null || isBlank(item.getItem_name()) || item.getTarget_quantity() == null) {
                throw new IllegalArgumentException("Each needed item must include item_name and target_quantity.");
            }
            if (item.getTarget_quantity().intValue() <= 0) {
                throw new IllegalArgumentException("Target quantity must be greater than zero.");
            }
            String key = item.getItem_name().trim().toLowerCase();
            if (!names.add(key)) {
                throw new IllegalArgumentException("Duplicate item names are not allowed in the need list.");
            }
        }
    }

    private Campaign getOwnedCampaign(Long campaignId) {
        Campaign campaign = findCampaign(campaignId);
        User current = getCurrentOrganization();
        if (!campaign.getOrganization().getId().equals(current.getId())) {
            throw new SecurityException("You can only manage your own campaigns.");
        }
        return campaign;
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
