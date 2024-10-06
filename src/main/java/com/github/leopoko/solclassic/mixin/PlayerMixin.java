package com.github.leopoko.solclassic.mixin;

import com.github.leopoko.solclassic.FoodEventHandler;
import com.github.leopoko.solclassic.item.WickerBasketItem;
import com.mojang.datafixers.util.Pair;
import com.sun.jna.platform.win32.BaseTSD;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.logging.Logger;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Shadow public abstract boolean isLocalPlayer();

    @Shadow public abstract void awardStat(Stat<?> p_36247_);

    @Shadow public abstract void playSound(SoundEvent p_36137_, float p_36138_, float p_36139_);

    Logger LOGGER = Logger.getLogger(PlayerMixin.class.getName());

    @Inject(method = "eat", at = @At("HEAD"), cancellable = true)
    private void modifyFoodData(Level level, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if ((Player) (Object) this instanceof ServerPlayer serverPlayer) {
            // サーバープレイヤーが食べ物を食べたときにカスタム処理を行う
            if (FoodEventHandler.shouldModifyFood(stack)) {
                ItemStack originalStack = stack.copy();
                int slotindex = -1;
                boolean isWickerBasket = itemId.toString().equals("solclassic:wicker_basket");
                // カスタムの食事回復量を適用
                Pair<ItemStack, Integer> foodDataTmp = FoodEventHandler.modifyFoodData(serverPlayer, stack);
                stack = foodDataTmp.getFirst();
                slotindex = foodDataTmp.getSecond();
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

                if (itemId == null) {
                    cir.setReturnValue(stack);
                    return;
                }
                else if (!serverPlayer.getAbilities().instabuild && !itemId.toString().equals("solclassic:wicker_basket")) {
                    stack.shrink(1);
                }

                serverPlayer.gameEvent(GameEvent.EAT);

                if (isWickerBasket) {
                    WickerBasketItem wickerBasket = (WickerBasketItem) originalStack.getItem();
                    if (slotindex == -1) {
                        cir.setReturnValue(originalStack);
                        return;
                    }
                    wickerBasket.consumeItemFromSlot(originalStack, slotindex);
                    cir.setReturnValue(originalStack);
                }
                else {
                    cir.setReturnValue(stack);
                }
            }
        }
        else {
            // クライアントプレイヤーが食べ物を食べたときにカスタム処理を行う
            Player player = (Player) (Object) this;
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);

            if (stack.isEdible()){
                level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), player.getEatingSound(stack), SoundSource.NEUTRAL, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
                Item item = stack.getItem();
                if (item.isEdible()) {
                    for (Pair<MobEffectInstance, Float> pair : stack.getFoodProperties((LivingEntity) (Object) this).getEffects()) {
                        if (!level.isClientSide && pair.getFirst() != null && level.random.nextFloat() < pair.getSecond()) {
                            player.addEffect(new MobEffectInstance(pair.getFirst()));
                        }
                    }
                }

                if (itemId == null) {
                    cir.setReturnValue(stack);
                    return;
                }
                else if (!player.getAbilities().instabuild && !itemId.toString().equals("solclassic:wicker_basket")) {
                    stack.shrink(1);
                }

                player.gameEvent(GameEvent.EAT);

                cir.setReturnValue(stack);
            }
        }
    }
}
