package com.givinghand.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Stores the inventory lines inside a warehouse, including current stock and the baseline used for low-stock alerts.
 * Endpoints using this entity are /api/warehouse/{id}/add, /api/warehouse/{id}, and /api/inventory/allocate.
 * Important notes: original_quantity grows when stock is added so the 10% alert threshold remains meaningful after restocking.
 */
@Entity
@Table(name = "warehouse_items")
public class WarehouseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String category;

    @Column(name = "original_quantity", nullable = false)
    private int originalQuantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public void setOriginalQuantity(int originalQuantity) {
        this.originalQuantity = originalQuantity;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }
}
