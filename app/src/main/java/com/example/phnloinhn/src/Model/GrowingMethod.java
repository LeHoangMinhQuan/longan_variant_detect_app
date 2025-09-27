package com.example.phnloinhn.src.Model;

// Growing method model
// Growing method model
// GrowingMethod.java
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

    // Getters & setters

    public String getBranch_pruning() {
        return branch_pruning;
    }

    public void setBranch_pruning(String branch_pruning) {
        this.branch_pruning = branch_pruning;
    }

    public String getFruit_pruning() {
        return fruit_pruning;
    }

    public void setFruit_pruning(String fruit_pruning) {
        this.fruit_pruning = fruit_pruning;
    }

    public String getPlant_distance() {
        return plant_distance;
    }

    public void setPlant_distance(String plant_distance) {
        this.plant_distance = plant_distance;
    }

    public String getPlant_time() {
        return plant_time;
    }

    public void setPlant_time(String plant_time) {
        this.plant_time = plant_time;
    }

    public String getFertilizer() { return fertilizer; }
    public void setFertilizer(String fertilizer) { this.fertilizer = fertilizer; }


    public String getPesticide() { return pesticide; }
    public void setPesticide(String pesticide) { this.pesticide = pesticide; }

    public String getSoil() { return soil; }
    public void setSoil(String soil) { this.soil = soil; }

    public String getOther() { return other; }
    public void setOther(String other) { this.other = other; }

}


