package com.github.leopoko.solclassic.compat;

import com.illusivesoulworks.diet.api.DietApi;
import com.illusivesoulworks.diet.api.type.IDietResult;
import com.illusivesoulworks.diet.api.type.IDietSuite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.illusivesoulworks.diet.client.DietClientEvents.renderItemTooltip;

public class DietHelper {
    public static IDietResult getPlayerNutrition(Player player, ItemStack stack) {
        // DietApiインスタンスを取得
        DietApi dietApi = DietApi.getInstance();

        // プレイヤーの栄養情報を取得
        IDietResult dietSuite = dietApi.get(player, stack);

        return dietSuite;
    }
}
