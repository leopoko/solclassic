package com.github.leopoko.solclassic.mixin;

import com.github.leopoko.solclassic.item.WickerBasketItem;
import com.illusivesoulworks.diet.api.DietApi;
import com.illusivesoulworks.diet.api.type.IDietGroup;
import com.illusivesoulworks.diet.common.DietApiImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(DietApiImpl.class)
public class DietMixin {

    @Inject(method = "getGroups", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    public void getGroupsMixin(Player player, ItemStack input, CallbackInfoReturnable<Set<IDietGroup>> cir) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(input.getItem());
        if (itemId.toString().equals("solclassic:wicker_basket")) {
            WickerBasketItem wicekrBasket = (WickerBasketItem) input.getItem();
            var foodData = wicekrBasket.getMostNutritiousFood(input, player);

            if (foodData == null) {
                Set<IDietGroup> groups = new HashSet<>();
                cir.setReturnValue(groups);
                return;
            }

            ItemStack targetStack = foodData.getFirst();
            input = targetStack;
        }
    }

    @ModifyVariable(method = "getGroups", at = @At("HEAD"), remap = false, ordinal = 0, argsOnly = true)
    private ItemStack modifyFoodData(ItemStack input, Player player) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(input.getItem());
        if (itemId.toString().equals("solclassic:wicker_basket")) {
            WickerBasketItem wicekrBasket = (WickerBasketItem) input.getItem();
            var foodData = wicekrBasket.getMostNutritiousFood(input, player);

            if (foodData == null) {
                return input;
            }

            ItemStack targetStack = foodData.getFirst();
            return targetStack;
        }
        return input;
    }
}
