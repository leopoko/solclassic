package com.github.leopoko.solclassic.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FoodOnlySlot extends Slot {
    public FoodOnlySlot(Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // スタックが食料かどうかをチェック
        return stack.getItem().isEdible();
    }
}
