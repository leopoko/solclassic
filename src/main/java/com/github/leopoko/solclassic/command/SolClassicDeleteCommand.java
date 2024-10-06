package com.github.leopoko.solclassic.command;

import com.github.leopoko.solclassic.FoodEventHandler;
import com.github.leopoko.solclassic.sync.NetworkHandler;
import com.github.leopoko.solclassic.sync.SyncFoodHistoryPacket;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

import java.util.Collection;
import java.util.LinkedList;


@Mod.EventBusSubscriber
public class SolClassicDeleteCommand {

    // コマンドの登録
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("resetfoodhistory")
                .requires(source -> source.hasPermission(2)) // OP権限が必要
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(SolClassicDeleteCommand::executeResetFoodHistory)));
    }

    // コマンドの実行処理
    private static int executeResetFoodHistory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        LinkedList<ItemStack> empltyfoodHistory = new LinkedList<>();
        for (ServerPlayer player : players) {
            // プレイヤーの食事履歴をリセット
            FoodEventHandler.resetFoodHistory(player);

            // クライアントにパケットを送信してリセットを同期
            SyncFoodHistoryPacket packet = new SyncFoodHistoryPacket(empltyfoodHistory);
            NetworkHandler.CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);

            // コマンド実行者に成功メッセージを送信（Supplier<Component>を使用）
            context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + "'s food history has been reset."), true);

            return 1; // 成功したことを示す
        }
        return 0; // 失敗したことを示す
    }
}
