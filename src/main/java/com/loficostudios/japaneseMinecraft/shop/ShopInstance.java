package com.loficostudios.japaneseMinecraft.shop;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ShopInstance<Impl extends ShopItem<Impl>> implements Cloneable {

    private Map<String, CostModifier> modifiers = new HashMap<>();

    double cost;

    final Impl item;

    public ShopInstance(double cost, Impl item) {
        this.cost = cost;
        this.item = item;
    }

    public Impl getItem() {
        return item;
    }

    public boolean isStackable() {
        return item instanceof Stackable;
    }

    public void addModifier(CostModifier modifier) {
        modifiers.put(modifier.getId(), modifier);
    }

    public void removeModifier(String id) {
        modifiers.remove(id);
    }

    public double getBaseCost() {
        return cost;
    }

    public Collection<CostModifier> getModifiers() {
        return Collections.unmodifiableCollection(modifiers.values());
    }

    public double getCost() {
        double cost = this.cost;
        for (CostModifier value : modifiers.values())
            if (value.getOperation().equals(Operation.ADD))
                cost += value.getValue();
        for (CostModifier value : modifiers.values())
            if (value.getOperation().equals(Operation.MULTIPLY))
                cost *= value.getValue();
        return cost;
    }


    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ShopInstance<Impl> clone() {
        ShopInstance<Impl> cloned = new ShopInstance<>(this.cost, this.item);
        cloned.modifiers = new HashMap<>(this.modifiers);
        return cloned;
    }
}
