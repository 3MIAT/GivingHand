package com.givinghand.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Stores a donor's commitment to provide a specific campaign item and tracks its lifecycle.
 * Endpoints using this entity are /api/donations/commit, /api/donations/{id}/status, /api/donations/{id}/edit, and /api/donations/{id}/cancel.
 * Important notes: only committed donations may be edited or cancelled, and distributed donations are added to donor history.
 */
@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public User getDonor() {
        return donor;
    }

    public void setDonor(User donor) {
        this.donor = donor;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public void setStatus(DonationStatus status) {
        this.status = status;
    }
}
