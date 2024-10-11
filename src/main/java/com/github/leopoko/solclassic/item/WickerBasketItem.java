package com.github.leopoko.solclassic.item;

import com.github.leopoko.solclassic.FoodEventHandler;
import com.github.leopoko.solclassic.container.FoodOnlyChestMenu;
import com.github.leopoko.solclassic.container.FoodOnlyContainer;
import com.github.leopoko.solclassic.container.FoodOnlyItemStackHandler;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Logger;

public class WickerBasketItem extends Item {

    static Logger LOGGER = Logger.getLogger(WickerBasketItem.class.getName());

    private static final String INVENTORY_TAG = "basket_inventory";
    public static final int SLOT_COUNT = 9;


    public WickerBasketItem(Properties properties) {
        super(properties);
    }
    // カスタムのコンストラクタを使って食料アイテムのプロパティを設定
    /*
    public WickerBasketItem() {
        Properties properties = new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(0.6f).build());
        super(properties);
    }
    */

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.wicker_basket.description1"));
        tooltip.add(Component.translatable("tooltip.wicker_basket.description2"));
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isCrouching()) {
            if (!level.isClientSide && player instanceof ServerPlayer) {
                ItemStack stack = player.getItemInHand(hand);
                ServerPlayer serverPlayer = (ServerPlayer) player;

                int slotIndex = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1;

                // アイテムのインベントリを読み込み
                FoodOnlyItemStackHandler itemHandler = getItemStackHandler(stack);
                MenuProvider containerProvider = getMenuProvider(itemHandler, slotIndex, stack);

                NetworkHooks.openScreen(serverPlayer, containerProvider);
            }
        }
        else
        {
            ItemStack stack = player.getItemInHand(hand);
            FoodOnlyItemStackHandler itemHandler = getItemStackHandler(stack);
            // バッグ内が空かどうかをチェック
            boolean isEmpty = true;
            for (int i = 0; i < SLOT_COUNT; i++) {
                if (!itemHandler.getStackInSlot(i).isEmpty() && itemHandler.getStackInSlot(i).isEdible()) {
                    isEmpty = false;
                    break;
                }
            }

            if (!isEmpty){
                player.startUsingItem(hand);
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    /**
     * バッグ内から最も回復量が高い食べ物を探す関数
     * @param stack 食料用のアイテムスタック
     * @param player プレイヤー
     * @return 最も回復量が高い食べ物のItemStackとそのスロット番号を含むPairオブジェクト
     */
    public Pair<ItemStack, Integer> getMostNutritiousFood(ItemStack stack, Player player) {
        ItemStack bestFood = ItemStack.EMPTY;
        FoodOnlyItemStackHandler itemHandler = getItemStackHandler(stack);
        float highestSaturation = 0.0f;
        int bestSlot = -1;

        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack foodStack = itemHandler.getStackInSlot(i);
            if (!foodStack.isEmpty() && foodStack.isEdible()) {
                FoodProperties foodProperties = foodStack.getItem().getFoodProperties(foodStack, player);
                if (foodProperties != null) {
                    // FoodEventHandlerを使ってカスタムの回復量を取得
                    Pair<Integer, Float> foodData = FoodEventHandler.getFoodData(foodStack, player);
                    float nutritionAndSaturation = foodData.getFirst() + foodData.getSecond();

                    // 最も回復量が高い食べ物を記録
                    if (nutritionAndSaturation > highestSaturation) {
                        bestFood = foodStack;
                        highestSaturation = nutritionAndSaturation;
                        bestSlot = i;
                    }
                }
            }
        }

        if (!bestFood.isEmpty() && bestSlot != -1) {
            return Pair.of(bestFood, bestSlot);
        }
        return null;
    }

    /**
     * 指定されたスロットからアイテムを1つ減らす関数
     * @param stack アイテムスタックハンドラ
     * @param slotIndex アイテムを減らすスロット番号
     */
    public void consumeItemFromSlot(ItemStack stack, int slotIndex) {
        FoodOnlyItemStackHandler itemHandler = getItemStackHandler(stack);
        ItemStack stackInSlot = itemHandler.getStackInSlot(slotIndex);
        if (!stackInSlot.isEmpty()) {
            stackInSlot.shrink(1); // アイテムを1つ減らす
            itemHandler.setStackInSlot(slotIndex, stackInSlot); // 減らした結果をハンドラに反映
            saveItemStackHandler(stack, itemHandler); // ハンドラをNBTに保存
        }
    }

    private @NotNull MenuProvider getMenuProvider(FoodOnlyItemStackHandler itemHandler, int slotIndex, ItemStack stack) {
        FoodOnlyContainer container = new FoodOnlyContainer(itemHandler);

        MenuProvider containerProvider = new SimpleMenuProvider(
                (id, playerInventory, playerEntity) -> new FoodOnlyChestMenu(MenuType.GENERIC_9x1, id, playerInventory, container, 1, slotIndex) {
                    @Override
                    public void removed(Player player) {
                        super.removed(player);
                        if (!player.level().isClientSide) {
                            // インベントリを閉じた時に保存
                            saveContainerToItemStack(stack, container);
                        }
                    }
                },
                Component.translatable("container.wicker_basket")
        );
        return containerProvider;
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
