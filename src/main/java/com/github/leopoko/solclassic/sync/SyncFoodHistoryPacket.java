package com.github.leopoko.solclassic.sync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedList;
import java.util.function.Supplier;

public class SyncFoodHistoryPacket {

    private final LinkedList<ItemStack> foodHistory;

    // コンストラクタ
    public SyncFoodHistoryPacket(LinkedList<ItemStack> foodHistory) {
        this.foodHistory = foodHistory;
    }

    // パケットのエンコード
    public static void encode(SyncFoodHistoryPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.foodHistory.size());
        for (ItemStack stack : packet.foodHistory) {
            buf.writeItemStack(stack, false);
        }
    }

    // パケットのデコード
    public static SyncFoodHistoryPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        LinkedList<ItemStack> foodHistory = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            foodHistory.add(buf.readItem());
        }
        return new SyncFoodHistoryPacket(foodHistory);
    }

    // パケットのハンドリング
    public static void handle(SyncFoodHistoryPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // クライアント側でのみ実行する処理をClientPacketHandlerに委譲
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleFoodHistoryPacket(packet.foodHistory));

            // サーバーサイドでの処理が必要であればここに追加
        });
        context.setPacketHandled(true);
    }
}