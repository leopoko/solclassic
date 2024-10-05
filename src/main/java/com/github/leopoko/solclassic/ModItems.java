package com.github.leopoko.solclassic;

import com.github.leopoko.solclassic.item.LargeLunchbagItem;
import com.github.leopoko.solclassic.item.LunchbagItem;
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
            () -> new LargeLunchbagItem(new Item.Properties().stacksTo(1)));

}
