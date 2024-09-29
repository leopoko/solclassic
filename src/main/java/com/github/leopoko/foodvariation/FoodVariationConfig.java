package com.github.leopoko.foodvariation;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = Foodvariation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FoodVariationConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ServerConfig CONFIG;

    static {
        Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_CONFIG = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    public static class ServerConfig {

        // 保存する食事履歴の最大数（デフォルト: 5）
        public final ForgeConfigSpec.IntValue maxFoodHistorySize;
        public final ForgeConfigSpec.IntValue maxShortFoodHistorySize;

        public final ForgeConfigSpec.DoubleValue ShortfoodDecayModifiers;

        // 減衰係数（デフォルト: [1.0, 0.90, 0.75, 0.50, 0.05]）
        public final ForgeConfigSpec.ConfigValue<List<? extends Double>> foodDecayModifiers;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Food Variation Settings");

            // 食事履歴の保存回数
            maxFoodHistorySize = builder
                    .comment("Maximum number of food history entries to track")
                    .defineInRange("maxFoodHistorySize", 100, 5, 300);

            maxShortFoodHistorySize = builder
                    .comment("Maximum number of food short history entries to track")
                    .defineInRange("maxShortFoodHistorySize", 5, 1, 100);

            ShortfoodDecayModifiers = builder
                    .comment("Short decay modifiers for food recovery")
                    .defineInRange("ShortfoodDecayModifiers", 0.01, 0.0, 1.0);

            // 減衰係数のリスト
            foodDecayModifiers = builder
                    .comment("List of decay modifiers for food recovery, applied sequentially")
                    .defineList("foodDecayModifiers", Arrays.asList(1.0, 0.90, 0.75, 0.50, 0.05), o -> o instanceof Double);

            builder.pop();
        }
    }
}