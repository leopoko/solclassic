package com.github.leopoko.solclassic.container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// ロックされたスロット - 特定のアイテムをつかめないようにする
public class LockedSlot extends Slot {
    public LockedSlot(Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(Player player) {
        // このスロットからアイテムを取ることを禁止する
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // このスロットにアイテムを置くことを禁止する
        return false;
    }
}
