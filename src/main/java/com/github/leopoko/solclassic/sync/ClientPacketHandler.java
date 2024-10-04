package com.github.leopoko.solclassic.sync;

import com.github.leopoko.solclassic.FoodEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;

public class ClientPacketHandler {

    // クライアント側のプレイヤーに食事履歴を設定し、チャットにメッセージを表示
    public static void handleFoodHistoryPacket(LinkedList<ItemStack> foodHistory) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            // クライアント側で食事履歴を更新
            FoodEventHandler.setFoodHistory(player, foodHistory);
        }
    }
}
