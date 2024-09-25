package com.github.leopoko.foodvariation;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
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

        // 同じ食べ物を何回食べたかによって回復量を減少させるロジックを追加
        int timesEaten = FoodEventHandler.getTimesEatenLong(player, stack);
        int timesEatenShort = FoodEventHandler.getTimesEatenShort(player, stack);

        // カスタムの回復量を調整 (timesEatenに基づく回復度の減少)
        float modifier = FoodEventHandler.calculateModifier(timesEaten, timesEatenShort);
        int customNutrition = Math.max(1, (int) (nutrition * modifier));
        float customSaturation = Math.max(0.1f, saturation * modifier);

        // カスタムの回復量を新しいFoodValuesとして返す
        return new FoodValues(customNutrition, customSaturation);
    }
}
