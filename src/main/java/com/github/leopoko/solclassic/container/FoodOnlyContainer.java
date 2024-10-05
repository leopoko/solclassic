package com.github.leopoko.solclassic.container;


import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class FoodOnlyContainer extends SimpleContainer {
    private final FoodOnlyItemStackHandler handler;

    public FoodOnlyContainer(FoodOnlyItemStackHandler handler) {
        super(handler.getSlots());
        this.handler = handler;

        // ItemStackHandlerからSimpleContainerにアイテムをコピー
        for (int i = 0; i < handler.getSlots(); i++) {
            this.setItem(i, handler.getStackInSlot(i));
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        // FoodOnlyItemStackHandlerの制限を使用
        return handler.isItemValid(slot, stack);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        // 適していないアイテムの場合は何もせず終了
        if (!handler.isItemValid(index, stack)) {
            return;
        }

        // 適したアイテムの場合、SimpleContainerとItemStackHandlerの両方に設定
        super.setItem(index, stack);
        handler.setStackInSlot(index, stack); // ItemStackHandlerにも反映
    }
}