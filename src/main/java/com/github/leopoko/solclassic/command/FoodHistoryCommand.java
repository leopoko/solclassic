package com.github.leopoko.solclassic.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.github.leopoko.solclassic.FoodEventHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber
public class FoodHistoryCommand {

    // コマンドの登録
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("foodhistory")
                                .then(Commands.argument("target", EntityArgument.players())
                                .executes(FoodHistoryCommand::execute))
        );
    }

    // コマンドの実行部分
    private static int execute(CommandContext<CommandSourceStack> context) {
        Collection<ServerPlayer> targetPlayers;
        try {
            targetPlayers = EntityArgument.getPlayers(context, "target");
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("No valid player targets found."));
            return 0;
        }

        // 各ターゲットプレイヤーの食事履歴を表示
        for (ServerPlayer targetPlayer : targetPlayers) {
            List<ItemStack> foodHistory = FoodEventHandler.getFoodHistoryForPlayer(targetPlayer);
            if (foodHistory.isEmpty()) {
                context.getSource().sendSuccess(() -> Component.literal("No food history found for player: " + targetPlayer.getName().getString()), false);
            } else {
                context.getSource().sendSuccess(() -> Component.literal("Food history for " + targetPlayer.getName().getString() + ":"), false);
                for (ItemStack food : foodHistory) {
                    context.getSource().sendSuccess(() -> Component.literal("- " + food.getHoverName().getString()), false);
                }
            }
        }

        return 1;
    }
}
