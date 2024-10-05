package com.github.leopoko.solclassic.item;

import com.github.leopoko.solclassic.container.FoodOnlyChestMenu;
import com.github.leopoko.solclassic.container.FoodOnlyContainer;
import com.github.leopoko.solclassic.container.FoodOnlyItemStackHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

import java.util.logging.Logger;

public class LargeLunchbagItem extends Item {

    static Logger LOGGER = Logger.getLogger(LargeLunchbagItem.class.getName());

    private static final String INVENTORY_TAG = "large_basket_inventory";
    public static final int SLOT_COUNT = 27;

    public LargeLunchbagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            ServerPlayer serverPlayer = (ServerPlayer) player;

            int slotIndex = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1;

            // アイテムのインベントリを読み込み
            FoodOnlyItemStackHandler itemHandler = getItemStackHandler(stack);
            FoodOnlyContainer container = new FoodOnlyContainer(itemHandler);

            // インベントリをコンテナにセット
            /*
            for (int i = 0; i < SLOT_COUNT; i++) {
                container.setItem(i, itemHandler.getStackInSlot(i));
            }
             */

            MenuProvider containerProvider = new SimpleMenuProvider(
                    (id, playerInventory, playerEntity) -> new FoodOnlyChestMenu(MenuType.GENERIC_9x3, id, playerInventory, container, 3, slotIndex){
                        @Override
                        public void removed(Player player) {
                            super.removed(player);
                            if (!player.level().isClientSide) {
                                // インベントリを閉じた時に保存
                                saveContainerToItemStack(stack, container);
                            }
                        }
                    },
                    Component.translatable("container.large_basket")
            );

            NetworkHooks.openScreen(serverPlayer, containerProvider);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    private FoodOnlyItemStackHandler getItemStackHandler(ItemStack stack) {
        // アイテムのNBTからインベントリデータを取得し、ItemStackHandlerに読み込みます
        FoodOnlyItemStackHandler handler = new FoodOnlyItemStackHandler(SLOT_COUNT);
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(INVENTORY_TAG)) {
            handler.deserializeNBT(tag.getCompound(INVENTORY_TAG));
        }
        return handler;
    }

    private void saveItemStackHandler(ItemStack stack, ItemStackHandler handler) {
        // ItemStackHandlerのデータをNBTとして保存
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(INVENTORY_TAG, handler.serializeNBT());
    }

    private void saveContainerToItemStack(ItemStack stack, SimpleContainer container) {
        // SimpleContainerの内容をItemStackHandlerに保存し、NBTに書き出す
        FoodOnlyItemStackHandler itemHandler = new FoodOnlyItemStackHandler(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            itemHandler.setStackInSlot(i, container.getItem(i));
        }
        saveItemStackHandler(stack, itemHandler);
    }
}
