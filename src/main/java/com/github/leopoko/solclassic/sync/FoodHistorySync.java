package com.github.leopoko.solclassic.sync;


import com.github.leopoko.solclassic.FoodEventHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;

import java.util.LinkedList;

public class FoodHistorySync {
    // サーバー側で食事履歴をクライアントに送信
    public static void syncFoodHistory(ServerPlayer player) {
        LinkedList<ItemStack> foodHistory = FoodEventHandler.getFoodHistory(player);

        NetworkHandler.CHANNEL.sendTo(new SyncFoodHistoryPacket(foodHistory), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);

    }
}
