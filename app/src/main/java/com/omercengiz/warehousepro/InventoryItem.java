package com.omercengiz.warehousepro;

public class InventoryItem {
    private int id;
    private String name;
    private String weight;
    private int quantity;
    private String notes;

    public InventoryItem(int id, String name, String weight, int quantity, String notes) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.quantity = quantity;
        this.notes = notes;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWeight() {
        return weight;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}