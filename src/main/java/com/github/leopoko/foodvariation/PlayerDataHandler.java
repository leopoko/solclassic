package com.github.leopoko.foodvariation;

import com.github.leopoko.foodvariation.sync.NetworkHandler;
import com.github.leopoko.foodvariation.sync.SyncFoodHistoryPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

import java.util.LinkedList;

@Mod.EventBusSubscriber()
public class PlayerDataHandler {

    // プレイヤーが再生成されるとき（例: 死亡やリスポーン）
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player originalPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        // 食事履歴を引き継ぐ
        LinkedList<ItemStack> originalHistory = FoodHistoryManager.loadFoodHistory(originalPlayer);
        FoodHistoryManager.saveFoodHistory(newPlayer, originalHistory);
    }

    // プレイヤーがログアウト/セーブ時にデータを保存
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        LinkedList<ItemStack> foodHistory = FoodEventHandler.getFoodHistory(player);

        // 食事履歴をNBTに保存
        PlayerFoodData.saveFoodData((ServerPlayer) player, foodHistory);
    }

    // プレイヤーがログイン時にデータを読み込む
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        // NBTから食事履歴を読み込む
        LinkedList<ItemStack> foodHistory = PlayerFoodData.loadFoodData(player);
        FoodEventHandler.setFoodHistory(player, foodHistory);

        // サーバーからクライアントにパケットを送信して履歴を同期する
        NetworkHandler.CHANNEL.sendTo(
                new SyncFoodHistoryPacket(foodHistory),
                player.connection.connection, // プレイヤーのネットワーク接続を指定
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    // プレイヤーが食べ物を食べ終わった際に同期する
    @SubscribeEvent
    public static void onPlayerEatFood(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // クライアントサイドであれば処理を中断
            if (player.level().isClientSide()) {
                return;
            }
            ItemStack eatenItem = event.getItem();

            // 食事履歴をサーバー側で更新
            LinkedList<ItemStack> foodHistory = FoodEventHandler.getFoodHistory(player);

            // クライアントに食事履歴を同期
            NetworkHandler.CHANNEL.sendTo(
                    new SyncFoodHistoryPacket(foodHistory),
                    player.connection.connection, // プレイヤーのネットワーク接続を指定
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }
    }
}
