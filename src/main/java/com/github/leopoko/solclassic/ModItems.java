package com.github.leopoko.solclassic;

import com.github.leopoko.solclassic.item.OldLargeLunchbagItem;
import com.github.leopoko.solclassic.item.LunchbagItem;
import com.github.leopoko.solclassic.item.WickerBasketItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Solclassic.MODID);

    // Lunchbagのアイテム登録
    public static final RegistryObject<Item> BASKET = ITEMS.register("basket",
            () -> new LunchbagItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> LARGEBASKET = ITEMS.register("large_basket",
            () -> new OldLargeLunchbagItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> WICKERBASKET = ITEMS.register("wicker_basket",
            () -> new WickerBasketItem(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder().nutrition(5).saturationMod(0.5f).build())));

}
