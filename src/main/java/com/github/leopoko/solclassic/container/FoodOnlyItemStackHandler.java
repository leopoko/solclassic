package com.github.leopoko.solclassic.container;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class FoodOnlyItemStackHandler extends ItemStackHandler {
    public FoodOnlyItemStackHandler(int size) {
        super(size);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        // スタックが食料かどうかをチェック
        return stack.getItem().isEdible();
        //return false;
    }
}
