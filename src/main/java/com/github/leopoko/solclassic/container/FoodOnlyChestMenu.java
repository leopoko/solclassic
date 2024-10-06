package com.github.leopoko.solclassic.container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class FoodOnlyChestMenu extends ChestMenu {
    private final int lockedSlotIndex;

    public FoodOnlyChestMenu(MenuType<?> menuType, int id, Inventory playerInventory, Container container, int rows, int lockedSlotIndex) {
        super(menuType, id, playerInventory, container, rows);
        this.lockedSlotIndex = lockedSlotIndex;
        // すべてのスロットを食料専用スロットに変更
        for (int i = 0; i < container.getContainerSize(); i++) {
            this.slots.set(i, new FoodOnlySlot(container, i, 8 + (i % 9) * 18, 18 + (i / 9) * 18));
        }

        // プレイヤーのインベントリスロットに対する制限
        if (lockedSlotIndex >= 0 && lockedSlotIndex < playerInventory.items.size()) {
            this.slots.set(rows * 9 + 27 + lockedSlotIndex, new LockedSlot(playerInventory, lockedSlotIndex, 8 + (lockedSlotIndex % 9) * 18, 142 + (lockedSlotIndex / 9) * 18));
        }
    }
}