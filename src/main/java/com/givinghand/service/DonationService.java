package com.givinghand.service;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.givinghand.dto.CommitDonationDTO;
import com.givinghand.dto.EditDonationDTO;
import com.givinghand.model.Campaign;
import com.givinghand.model.CampaignItem;
import com.givinghand.model.CampaignStatus;
import com.givinghand.model.Donation;
import com.givinghand.model.DonationStatus;
import com.givinghand.model.NotificationEventType;
import com.givinghand.model.Role;
import com.givinghand.model.User;


@Stateless
public class DonationService {

    @PersistenceContext(unitName = "givinghandPU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    @javax.inject.Inject
    private NotificationService notificationService;

    public Donation commitDonation(CommitDonationDTO dto) {
        if (dto == null || dto.getCampaign_id() == null || isBlank(dto.getItem_name()) || dto.getQuantity() == null) {
            throw new IllegalArgumentException("campaign_id, item_name, and quantity are required.");
        }
        if (dto.getQuantity().intValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        User donor = getCurrentDonor();
        Campaign campaign = getCampaignWithItems(dto.getCampaign_id());
        if (campaign.getStatus() != CampaignStatus.OPEN) {
            throw new IllegalArgumentException("Donations can only be committed to open campaigns.");
        }

        CampaignItem item = findCampaignItem(campaign.getNeededItems(), dto.getItem_name());
        int remainingNeed = calculateRemainingNeed(campaign, item.getItemName(), null, item.getTargetQuantity(),
                item.getReceivedQuantity());
        if (dto.getQuantity().intValue() > remainingNeed) {
            throw new IllegalArgumentException("Requested quantity exceeds remaining campaign need.");
        }

        Donation donation = new Donation();
        donation.setCampaign(campaign);
        donation.setDonor(donor);
        donation.setItemName(item.getItemName());
        donation.setQuantity(dto.getQuantity().intValue());
        donation.setStatus(DonationStatus.COMMITTED);
        em.persist(donation);
        em.flush();
        return donation;
    }

    public void updateDonationStatus(Long donationId, String statusValue) {
        Donation donation = getDonation(donationId);
        User organization = getCurrentOrganization();
        if (!donation.getCampaign().getOrganization().getId().equals(organization.getId())) {
            throw new SecurityException("You can only manage donations for your own campaigns.");
        }

        DonationStatus newStatus = DonationStatus.fromValue(statusValue);
        if (!isValidTransition(donation.getStatus(), newStatus)) {
            throw new IllegalArgumentException("Invalid donation status transition.");
        }

        donation.setStatus(newStatus);
        em.merge(donation);

        if (newStatus == DonationStatus.RECEIVED) {
            notificationService.sendNotification(NotificationEventType.DONATION_RECEIVED,
                    "Your " + donation.getQuantity() + " " + donation.getItemName()
                            + " have been received by the organization.");
        } else if (newStatus == DonationStatus.DISTRIBUTED) {
            addContributionHistory(donation);
        }
    }

    public void editDonation(Long donationId, EditDonationDTO dto) {
        if (dto == null || isBlank(dto.getItem_name()) || dto.getQuantity() == null) {
            throw new IllegalArgumentException("item_name and quantity are required.");
        }
        if (dto.getQuantity().intValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        Donation donation = getDonation(donationId);
        User donor = getCurrentDonor();
        if (!donation.getDonor().getId().equals(donor.getId())) {
            throw new SecurityException("You can only edit your own donations.");
        }
        if (donation.getStatus() != DonationStatus.COMMITTED) {
            throw new IllegalArgumentException("Only committed donations can be edited.");
        }

        Campaign campaign = getCampaignWithItems(donation.getCampaign().getId());
        CampaignItem item = findCampaignItem(campaign.getNeededItems(), dto.getItem_name());
        int remainingNeed = calculateRemainingNeed(campaign, item.getItemName(), donation.getId(), item.getTargetQuantity(),
                item.getReceivedQuantity());
        if (dto.getQuantity().intValue() > remainingNeed) {
            throw new IllegalArgumentException("Requested quantity exceeds remaining campaign need.");
        }

        donation.setItemName(item.getItemName());
        donation.setQuantity(dto.getQuantity().intValue());
        em.merge(donation);
    }

    public void cancelDonation(Long donationId) {
        Donation donation = getDonation(donationId);
        User donor = getCurrentDonor();
        if (!donation.getDonor().getId().equals(donor.getId())) {
            throw new SecurityException("You can only cancel your own donations.");
        }
        if (donation.getStatus() != DonationStatus.COMMITTED) {
            throw new IllegalArgumentException("Only committed donations can be cancelled.");
        }
        em.remove(donation);
    }

    private void addContributionHistory(Donation donation) {
        User donor = donation.getDonor();
        donor.getContributionHistory().add("Distributed " + donation.getQuantity() + " " + donation.getItemName()
                + " to campaign " + donation.getCampaign().getTitle() + ".");
        em.merge(donor);
    }

    private boolean isValidTransition(DonationStatus current, DonationStatus next) {
        return (current == DonationStatus.COMMITTED && next == DonationStatus.RECEIVED)
                || (current == DonationStatus.RECEIVED && next == DonationStatus.DISTRIBUTED);
    }

    private int calculateRemainingNeed(Campaign campaign, String itemName, Long excludedDonationId, int targetQuantity,
            int receivedQuantity) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COALESCE(SUM(d.quantity), 0) FROM Donation d WHERE d.campaign = :campaign AND LOWER(d.itemName) = :itemName");
        if (excludedDonationId != null) {
            jpql.append(" AND d.id <> :donationId");
        }

        javax.persistence.Query query = em.createQuery(jpql.toString()).setParameter("campaign", campaign)
                .setParameter("itemName", itemName.toLowerCase());
        if (excludedDonationId != null) {
            query.setParameter("donationId", excludedDonationId);
        }

        Number totalCommitted = (Number) query.getSingleResult();
        return targetQuantity - receivedQuantity - totalCommitted.intValue();
    }

    private Donation getDonation(Long donationId) {
        try {
            Donation donation = em.createQuery(
                    "SELECT d FROM Donation d JOIN FETCH d.campaign c JOIN FETCH c.organization JOIN FETCH d.donor WHERE d.id = :id",
                    Donation.class).setParameter("id", donationId).getSingleResult();
            donation.getCampaign().getTitle();
            return donation;
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Donation not found.");
        }
    }

    private Campaign getCampaignWithItems(Long campaignId) {
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

    private CampaignItem findCampaignItem(List<CampaignItem> items, String itemName) {
        for (CampaignItem item : items) {
            if (item.getItemName().equalsIgnoreCase(itemName.trim())) {
                return item;
            }
        }
        throw new IllegalArgumentException("Campaign item not found.");
    }

    private User getCurrentDonor() {
        User user = getCurrentUser();
        if (user.getRole() != Role.DONOR) {
            throw new SecurityException("Only donor users can perform this action.");
        }
        return user;
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
