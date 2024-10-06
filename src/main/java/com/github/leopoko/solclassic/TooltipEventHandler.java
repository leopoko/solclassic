package com.github.leopoko.solclassic;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class TooltipEventHandler {

    // ツールチップに何パーセント減少しているかを表示
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();

        // プレイヤーがnullでないか確認
        if (player == null) {
            return; // プレイヤーがnullなら処理をスキップ
        }

        ItemStack itemStack = event.getItemStack();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());

        // アイテムが食べ物であるか確認
        if (itemStack.isEdible() && !itemId.toString().equals("solclassic:wicker_basket")) {
            // 食べた回数に基づいて減少率を計算
            int timesEaten = FoodEventHandler.getTimesEatenLong(player, itemStack);
            int timesEatenShort = FoodEventHandler.getTimesEatenShort(player, itemStack);
            float modifier = FoodEventHandler.calculateModifier(timesEaten, timesEatenShort);
            int reductionPercent = (int) ((1.0f - modifier) * 100);

            // ローカライズされたテキストを使用してツールチップに減少率を追加
            if (reductionPercent > 0) {
                MutableComponent reductionText = Component.translatable("tooltip.food_reduction", reductionPercent);
                event.getToolTip().add(reductionText);
            }
        }
    }
}
