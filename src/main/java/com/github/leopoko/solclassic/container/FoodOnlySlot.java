package com.github.leopoko.solclassic.container;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FoodOnlySlot extends Slot {
    public FoodOnlySlot(Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // スタックが食料かどうかをチェック
        boolean isEdible = stack.getItem().isEdible();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (itemId.toString().equals("solclassic:wicker_basket")) {
            isEdible = false;
        }
        return isEdible;
    }
}
