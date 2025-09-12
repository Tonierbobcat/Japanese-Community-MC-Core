package com.loficostudios.japaneseMinecraft.shop;



public class CostModifier {

    private final double value;
    private final Operation operation;
    private final String id;
    public CostModifier(String id, double value, Operation operation) {
        this.id = id;
        this.value = value;
        this.operation = operation;
    }

    public double getValue() {
        return value;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getId() {
        return id;
    }
}
