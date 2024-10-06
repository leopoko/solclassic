package com.github.leopoko.solclassic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

import static com.github.leopoko.solclassic.FoodEventHandler.getFoodData;


public class AppleSkinEventHandler {
    // AppleSkinのFoodValuesEventを使って、ツールチップにカスタムの回復量を表示
    @SubscribeEvent()
    public void onFoodValuesEvent(FoodValuesEvent event) {
        Player player = event.player;
        ItemStack itemStack = event.itemStack;

        // カスタムの食べ物回復量を計算
        FoodValues customFoodValues = getCustomFoodValues(player, itemStack);

        // AppleSkinに対してカスタムの食べ物回復量を上書き
        event.modifiedFoodValues = customFoodValues;
        event.defaultFoodValues = customFoodValues;
    }

    // サーバープレイヤーとアイテムスタックに基づいてカスタムの回復量を計算する
    private static FoodValues getCustomFoodValues(Player player, ItemStack stack) {
        // 例: 既存のFoodPropertiesを使ってカスタムの回復量を計算
        int nutrition = stack.getItem().getFoodProperties().getNutrition();
        float saturation = stack.getItem().getFoodProperties().getSaturationModifier();

        // 回数に応じた回復度を計算
        var FoodData = getFoodData(stack, player);
        int customFoodLevel = FoodData.getFirst();
        float customSaturationLevel = FoodData.getSecond();

        // カスタムの回復量を新しいFoodValuesとして返す
        return new FoodValues(customFoodLevel, customSaturationLevel);
    }
}
