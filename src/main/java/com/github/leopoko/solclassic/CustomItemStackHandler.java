package com.github.leopoko.solclassic;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class CustomItemStackHandler extends ItemStackHandler {

    public CustomItemStackHandler(int size) {
        super(size);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        // 食料アイテムのみスロットに入れられるように制限
        return stack.getItem().isEdible();
    }
}
