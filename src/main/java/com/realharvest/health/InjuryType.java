package com.realharvest.health;

public enum InjuryType {
    NONE("Здорово", 0xFF3FA34D),
    CUT("Порез (нужен бинт)", 0xFFD62828),
    FRACTURE("Перелом (нужна шина)", 0xFFF77F00);

    private final String label;
    private final int color;

    InjuryType(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() { return label; }
    public int getColor() { return color; }
}
