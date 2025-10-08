package com.quan.phnloinhn.src.Model;
public class GrowingMethod {
    private String branch_pruning;
    private String fertilizer;
    private String fruit_pruning;
    private String pesticide;
    private String plant_distance;
    private String plant_time;
    private String soil;
    private String other;

    public GrowingMethod() {} // Firestore requires no-arg constructor

    public GrowingMethod(String branch_pruning, String fertilizer, String fruit_pruning, String pesticide, String plant_distance, String plant_time, String soil, String other) {
        this.branch_pruning = branch_pruning;
        this.fertilizer = fertilizer;
        this.fruit_pruning = fruit_pruning;
        this.pesticide = pesticide;
        this.plant_distance = plant_distance;
        this.plant_time = plant_time;
        this.soil = soil;
        this.other = other;
    }

    // Getters & setters
    public String getBranch_pruning() {
        return branch_pruning;
    }

    public String getFruit_pruning() {
        return fruit_pruning;
    }
    public String getPlant_distance() {
        return plant_distance;
    }

    public String getPlant_time() {
        return plant_time;
    }

    public String getFertilizer() { return fertilizer; }
    public String getPesticide() { return pesticide; }
    public String getSoil() { return soil; }
    public String getOther() { return other; }
}


