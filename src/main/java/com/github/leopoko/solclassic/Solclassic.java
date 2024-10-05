package com.github.leopoko.solclassic;

import com.github.leopoko.solclassic.sync.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Solclassic.MODID)
public class Solclassic {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "solclassic";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public Solclassic() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::appleSkinInit);
        modEventBus.addListener(this::setup);

        ModItems.ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new FoodEventHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerDataHandler());
        MinecraftForge.EVENT_BUS.register(new FoodHistoryManager());
        MinecraftForge.EVENT_BUS.register(new TooltipEventHandler());
        MinecraftForge.EVENT_BUS.register(new SolClassicCommands());

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SolClassicConfig.SERVER_CONFIG);
    }

    private void appleSkinInit(final FMLClientSetupEvent event) {
        LOGGER.info("AppleSkin check");
        if (ModList.get().isLoaded("appleskin")) {
            //デバック用ログ
            LOGGER.info("AppleSkin is loaded");
            MinecraftForge.EVENT_BUS.register(new AppleSkinEventHandler());
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        // ネットワークパケットの登録
        NetworkHandler.registerPackets();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void onBuildCreativeModeTabs(BuildCreativeModeTabContentsEvent event) {
            // Lunchbagを特定のクリエイティブタブに追加
            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(ModItems.BASKET.get());
                event.accept(ModItems.LARGEBASKET.get());
            }
        }
    }
}
