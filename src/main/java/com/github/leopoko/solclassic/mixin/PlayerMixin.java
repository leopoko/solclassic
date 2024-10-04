package com.github.leopoko.solclassic.mixin;

import com.github.leopoko.solclassic.FoodEventHandler;
import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "eat", at = @At("HEAD"), cancellable = true)
    private void modifyFoodData(Level level, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if ((Player) (Object) this instanceof ServerPlayer serverPlayer) {
            // サーバープレイヤーが食べ物を食べたときにカスタム処理を行う
            if (FoodEventHandler.shouldModifyFood(stack)) {
                // カスタムの食事回復量を適用
                FoodEventHandler.modifyFoodData(serverPlayer, stack);

                // デフォルトの食事処理をキャンセルし、スタックを返す

                serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                level.playSound((Player) null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);

                if ((Player) (Object) this instanceof ServerPlayer) {
                    CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
                }

                Item item = stack.getItem();
                if (item.isEdible()) {
                    for (Pair<MobEffectInstance, Float> pair : stack.getFoodProperties((LivingEntity) (Object) this).getEffects()) {
                        if (!level.isClientSide && pair.getFirst() != null && level.random.nextFloat() < pair.getSecond()) {
                            serverPlayer.addEffect(new MobEffectInstance(pair.getFirst()));
                        }
                    }
                }

                if (!serverPlayer.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                serverPlayer.gameEvent(GameEvent.EAT);

                cir.setReturnValue(stack);
            }
        }
    }
}
