package com.github.leopoko.solclassic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;

public class PlayerFoodData {
    private static final String FOOD_HISTORY_TAG = "FoodHistory";

    // 食事履歴を保存
    public static void saveFoodData(ServerPlayer player, LinkedList<ItemStack> foodHistory) {
        CompoundTag data = player.getPersistentData().getCompound(player.getUUID().toString());

        // 食事履歴を保存
        CompoundTag foodHistoryTag = new CompoundTag();
        for (int i = 0; i < foodHistory.size(); i++) {
            foodHistoryTag.put("item" + i, foodHistory.get(i).save(new CompoundTag()));
        }
        data.put(FOOD_HISTORY_TAG, foodHistoryTag);
        player.getPersistentData().put(player.getUUID().toString(), data);
    }

    // 食事履歴を読み込み
    public static LinkedList<ItemStack> loadFoodData(ServerPlayer player) {
        CompoundTag data = player.getPersistentData().getCompound(player.getUUID().toString());

        // 食事履歴の読み込み
        LinkedList<ItemStack> foodHistory = new LinkedList<>();
        if (data.contains(FOOD_HISTORY_TAG)) {
            CompoundTag foodHistoryTag = data.getCompound(FOOD_HISTORY_TAG);
            for (int i = 0; foodHistoryTag.contains("item" + i); i++) {
                foodHistory.add(ItemStack.of(foodHistoryTag.getCompound("item" + i)));
            }
        }

        return foodHistory;
    }
}
