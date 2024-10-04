package com.github.leopoko.solclassic;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.*;

@Mod.EventBusSubscriber(modid = Solclassic.MODID)
public class FoodEventHandler {

    // プレイヤーの食事履歴を管理するためのマップ (UUIDで管理)
    private static final Map<UUID, LinkedList<ItemStack>> playerFoodHistory = new HashMap<>();

    // カスタムの回復量を適用するかどうかを判定
    public static boolean shouldModifyFood(ItemStack stack) {
        return stack.isEdible();  // ここでカスタム処理が必要かを判定
    }

    // カスタムの食事処理を行う
    public static void modifyFoodData(ServerPlayer player, ItemStack stack) {
        UUID playerUUID = player.getUUID();

        // プレイヤーの履歴を取得（存在しない場合は新たに作成）
        LinkedList<ItemStack> foodHistory = playerFoodHistory.getOrDefault(playerUUID, new LinkedList<>());

        // 食べた回数を数える
        long sameFoodCount = getTimesEatenLong(player, stack);
        int sameFoodCountShort = getTimesEatenShort(player, stack);

        FoodProperties food = stack.getItem().getFoodProperties(stack, player);
        if (food != null) {
            // 回数に応じた回復度を計算
            float modifier = calculateModifier((int) sameFoodCount, sameFoodCountShort);
            int customFoodLevel = Math.max(1, (int) (food.getNutrition() * modifier));
            float customSaturationLevel = Math.max(0.1f, food.getSaturationModifier() * modifier);

            // プレイヤーの満腹度を更新
            player.getFoodData().eat(customFoodLevel, customSaturationLevel);

            // プレイヤーに通知
            if (isDevelopmentEnvironment()) {
                player.sendSystemMessage(Component.literal(
                        "食べ物: " + stack.getHoverName().getString() +
                                " | 回復する食糧値: " + customFoodLevel +
                                " | 回復する隠し食糧値: " + customSaturationLevel +
                                " | 同じ食べ物を食べた回数: " + (sameFoodCount + 1)
                ));
            }

            // 食べ物履歴に追加 (10個まで)
            addFoodToHistory(playerUUID, foodHistory, stack);
        }
    }

    // デバッグモードかどうかを判定するメソッド
    private static boolean isDevelopmentEnvironment() {
        // FMLEnvironment.dist()がDEVかどうかで開発環境を判定
        return !FMLEnvironment.production;
    }

    // 食べ物履歴に追加、最大10件まで保持
    private static void addFoodToHistory(UUID playerUUID, LinkedList<ItemStack> foodHistory, ItemStack eatenItem) {
        int maxHistorySize = SolClassicConfig.CONFIG.maxFoodHistorySize.get();
        if (foodHistory.size() >= maxHistorySize-1) {
            foodHistory.removeFirst();  // 古いものを削除
        }
        foodHistory.addLast(eatenItem);  // 新しいものを追加
        playerFoodHistory.put(playerUUID, foodHistory);  // 更新
    }

    // プレイヤーの食事履歴を取得する
    public static LinkedList<ItemStack> getFoodHistory(Player player) {
        return playerFoodHistory.getOrDefault(player.getUUID(), new LinkedList<>());
    }

    // プレイヤーの食事履歴を設定する
    public static void setFoodHistory(Player player, LinkedList<ItemStack> foodHistory) {
        playerFoodHistory.put(player.getUUID(), foodHistory);
    }

    // プレイヤーの食事履歴をリセットする（サーバー側）
    public static void resetFoodHistory(ServerPlayer player) {
        setFoodHistory(player, new LinkedList<>());
    }

    // 同じ食べ物を何回食べたかを取得
    public static int getTimesEatenLong(Player player, ItemStack stack) {
        LinkedList<ItemStack> foodHistory = getFoodHistory(player);
        return (int) foodHistory.stream()
                .filter(item -> item.getItem() == stack.getItem())
                .count();
    }

    public static int getTimesEatenShort(Player player, ItemStack stack) {
        LinkedList<ItemStack> foodHistory = getFoodHistory(player);
        int maxShortFoodHistorySize = SolClassicConfig.CONFIG.maxShortFoodHistorySize.get();
        int startIndex = Math.max(0, foodHistory.size() - maxShortFoodHistorySize);
        return (int) foodHistory.subList(startIndex, foodHistory.size()).stream()
                .filter(item -> item.getItem() == stack.getItem())
                .count();
    }

    // 回数に応じた回復度の計算
    static float calculateModifier(int timesEaten, int timesEatenShort) {
        List<? extends Double> decayModifiers = SolClassicConfig.CONFIG.foodDecayModifiers.get();
        double shortmodifier = SolClassicConfig.CONFIG.ShortfoodDecayModifiers.get();

        float modifier = 1.0f;
        if (timesEatenShort < decayModifiers.size()) {
            modifier = decayModifiers.get(timesEatenShort).floatValue();
        } else {
            // 減衰リストを超える回数では最後の係数を適用
            modifier = decayModifiers.get(decayModifiers.size() - 1).floatValue();
        }

        modifier = (float) (modifier - shortmodifier * timesEaten);

        modifier = Math.max(0.0f, modifier);

        return modifier;
    }
}