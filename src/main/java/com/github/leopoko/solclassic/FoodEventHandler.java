package com.github.leopoko.solclassic;

import com.github.leopoko.solclassic.item.WickerBasketItem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

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
    public static Pair<ItemStack,Integer> modifyFoodData(ServerPlayer player, ItemStack stack) {
        UUID playerUUID = player.getUUID();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        WickerBasketItem wicekrBasket = null;
        ItemStack originalStack = stack.copy();
        int slotIndex = -1;

        // プレイヤーの履歴を取得（存在しない場合は新たに作成）
        LinkedList<ItemStack> foodHistory = playerFoodHistory.getOrDefault(playerUUID, new LinkedList<>());

        FoodProperties food = stack.getItem().getFoodProperties(stack, player);

        // wicker_basketの場合は、中身の食べ物を取り出す
        if (itemId.toString().equals("solclassic:wicker_basket")) {
            wicekrBasket = (WickerBasketItem) stack.getItem();
            var foodData = wicekrBasket.getMostNutritiousFood(stack, player);
            if (foodData == null) {
                return Pair.of(stack, slotIndex);
            }
            slotIndex = foodData.getSecond();
            stack = foodData.getFirst();
            // ボウルを返す処理
            if (stack.getItem().getCraftingRemainingItem(stack) != null) {
                ItemStack containerItem = stack.getItem().getCraftingRemainingItem(stack);
                if (!player.getInventory().add(containerItem)) {
                    player.drop(containerItem, false); // インベントリに入らない場合、地面にドロップ
                }
            }
        }

        // 食べた回数を数える
        long sameFoodCount = getTimesEatenLong(player, stack);
        int sameFoodCountShort = getTimesEatenShort(player, stack);

        if (food != null) {
            // 回数に応じた回復度を計算
            var FoodData = getFoodData(stack, player);
            int customFoodLevel = FoodData.getFirst();
            float customSaturationLevel = FoodData.getSecond();

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
            if (wicekrBasket != null) {
                wicekrBasket.consumeItemFromSlot(originalStack, slotIndex);
            }
        }
        return Pair.of(stack, slotIndex);
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

    // stackとplayerを引数に取り、その食べ物の回復度（Food, Saturation）を返す
    public static Pair<Integer, Float> getFoodData(ItemStack stack, Player player) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        // stackがwicker_basketの場合は、回復度をそのまま返す
        if (itemId.toString().equals("solclassic:wicker_basket")) {
            WickerBasketItem item = (WickerBasketItem) stack.getItem();
            var foodData = item.getMostNutritiousFood(stack, player);
            if (foodData == null) {
                return Pair.of(0, 0.0f);
            }
            stack = item.getMostNutritiousFood(stack, player).getFirst();
        }

        FoodProperties food = stack.getItem().getFoodProperties(stack, player);

        long sameFoodCount = getTimesEatenLong(player, stack);
        int sameFoodCountShort = getTimesEatenShort(player, stack);
        if (food != null) {
            float modifier = calculateModifier((int) sameFoodCount, sameFoodCountShort);
            int customFoodLevel = Math.max(1, (int) (food.getNutrition() * modifier));
            float customSaturationLevel = Math.max(0.01f, food.getSaturationModifier());
            return Pair.of(customFoodLevel, customSaturationLevel);
        }
        return Pair.of(0, 0.0f);
    }

    public static List<ItemStack> getFoodHistoryForPlayer(ServerPlayer player) {
        LinkedList<ItemStack> history = playerFoodHistory.get(player.getUUID());
        if (history == null) {
            return List.of(); // 空のリストを返す
        }
        return new LinkedList<>(history); // 履歴をコピーして返す
    }
}