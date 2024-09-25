package com.github.leopoko.foodvariation;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FoodHistoryManager {private static final String FOOD_HISTORY_TAG = "FoodHistory";

    // プレイヤーに食事履歴を保存
    public static void saveFoodHistory(Player player, LinkedList<ItemStack> foodHistory) {

        CompoundTag playerData = player.getPersistentData();
        ListTag foodHistoryTag = new ListTag();

        // 食べ物履歴をNBTに変換して保存
        for (ItemStack stack : foodHistory) {
            CompoundTag stackTag = new CompoundTag();
            stack.save(stackTag);  // ItemStackをNBTに変換
            foodHistoryTag.add(stackTag);
        }

        // プレイヤーのNBTデータに保存
        playerData.put(FOOD_HISTORY_TAG, foodHistoryTag);
        }

    // プレイヤーから食事履歴を読み込む
    public static LinkedList<ItemStack> loadFoodHistory(Player player) {
        LinkedList<ItemStack> foodHistory = new LinkedList<>();
        CompoundTag playerData = player.getPersistentData();

        // プレイヤーのデータから食事履歴を取得
        if (playerData.contains(FOOD_HISTORY_TAG)) {
            ListTag foodHistoryTag = playerData.getList(FOOD_HISTORY_TAG, Tag.TAG_COMPOUND);
            for (Tag tag : foodHistoryTag) {
                CompoundTag stackTag = (CompoundTag) tag;
                ItemStack stack = ItemStack.of(stackTag);  // NBTからItemStackに変換
                foodHistory.add(stack);
            }
        }

        return foodHistory;
    }
}