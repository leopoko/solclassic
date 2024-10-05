package com.github.leopoko.solclassic.item;

import com.github.leopoko.solclassic.container.FoodOnlyChestMenu;
import com.github.leopoko.solclassic.container.FoodOnlyContainer;
import com.github.leopoko.solclassic.container.FoodOnlyItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.logging.Logger;

public class LunchbagItem extends Item {

    static Logger LOGGER = Logger.getLogger(LunchbagItem.class.getName());

    private static final String INVENTORY_TAG = "basket_inventory";
    public static final int SLOT_COUNT = 9;

    public LunchbagItem(Properties properties) {
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
                    (id, playerInventory, playerEntity) -> new FoodOnlyChestMenu(MenuType.GENERIC_9x1, id, playerInventory, container, 1, slotIndex){
                        @Override
                        public void removed(Player player) {
                            super.removed(player);
                            if (!player.level().isClientSide) {
                                // インベントリを閉じた時に保存
                                saveContainerToItemStack(stack, container);
                            }
                        }
                    },
                    Component.translatable("container.basket")
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
