package com.quan.phnloinhn.src.Model;

import java.util.Map;
public class LonganVariant {
    private String name;
    private String origin;
    private String productivity;
    private String description;
    private String tips;
    private Map<String, GrowingMethod> growingMethods;

    public LonganVariant() {}

    // Getters & setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrigin() { return origin; }
    public String getProductivity() { return productivity; }
    public String getDescription() { return description; }
    public String getTips() { return tips; }
    public Map<String, GrowingMethod> getGrowingMethods() { return growingMethods; }
    public void setGrowingMethods(Map<String, GrowingMethod> growingMethods) { this.growingMethods = growingMethods; }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setProductivity(String productivity) {
        this.productivity = productivity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}


