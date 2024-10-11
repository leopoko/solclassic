package com.github.leopoko.solclassic.container;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FoodOnlyContainer extends SimpleContainer {
    private final FoodOnlyItemStackHandler handler;
    private int size;

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
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        // 適していないアイテムの場合は何もせず終了
        if (!handler.isItemValid(index, stack) && !stack.isEmpty()) {
            return;
        }

        // 適したアイテムの場合、SimpleContainerとItemStackHandlerの両方に設定
        super.setItem(index, stack);
        handler.setStackInSlot(index, stack); // ItemStackHandlerにも反映
    }


}