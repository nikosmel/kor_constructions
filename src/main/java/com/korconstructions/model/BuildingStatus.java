package com.korconstructions.model;

public enum BuildingStatus {
    PLANNING("Σχεδιασμός"),
    IN_PROGRESS("Σε Εξέλιξη"),
    COMPLETED("Ολοκληρωμένο");

    private final String greekName;

    BuildingStatus(String greekName) {
        this.greekName = greekName;
    }

    public String getGreekName() {
        return greekName;
    }
}
