package com.realharvest.health;

public enum BodyPart {
    HEAD("Голова"),
    TORSO("Торс"),
    LEFT_ARM("Левая рука"),
    RIGHT_ARM("Правая рука"),
    LEFT_LEG("Левая нога"),
    RIGHT_LEG("Правая нога");

    private final String displayName;

    BodyPart(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
