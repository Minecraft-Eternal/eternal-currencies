package com.mceternal.eternalcurrencies;

import com.mceternal.eternalcurrencies.command.EternalCurrenciesCommands;
import com.mceternal.eternalcurrencies.data.*;
import com.mceternal.eternalcurrencies.data.shop.entry.ECShopEntryTypes;
import com.mceternal.eternalcurrencies.data.shop.requirement.ECShopRequirementTypes;
import com.mceternal.eternalcurrencies.integration.ftbquests.QuestsIntegration;
import com.mceternal.eternalcurrencies.item.EternalCurrenciesItems;
import com.mceternal.eternalcurrencies.item.ItemCurrencyCheque;
import com.mceternal.eternalcurrencies.network.PacketHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EternalCurrencies.MODID)
public class EternalCurrencies {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "eternalcurrencies";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final ResourceLocation CURRENCIES_CAP_NAME = resource("currencies");

    public static final ResourceLocation CURRENCY_COINS = resource("coins");

    public static boolean FTBQ_LOADED = ModList.get().isLoaded("ftbquests");


    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    
    public static final RegistryObject<CreativeModeTab> EC_CREATIVE_TAB = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> new ItemStack(EternalCurrenciesItems.CHEQUE.get()))
                    .title(Component.translatable("mod.eternalcurrencies.name"))
                    .build());

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }

    public EternalCurrencies(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        EternalCurrenciesItems.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        modEventBus.addListener(EternalCurrenciesRegistries::registerCurrencies);

        modEventBus.addListener(EternalCurrenciesRegistries::addRegistries);

        ECShopEntryTypes.register(modEventBus);
        ECShopRequirementTypes.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        if(FTBQ_LOADED)
            QuestsIntegration.init();

        CREATIVE_MODE_TABS.register(modEventBus);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> EternalCurrenciesClient::init);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        //context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
            EternalCurrencies.LOGGER.info("registered Packets");
        });
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTab() == EC_CREATIVE_TAB.get()) {
            Optional<HolderLookup.RegistryLookup<CurrencyData>> currencyLookup = event.getParameters().holders().lookup(EternalCurrenciesRegistries.KEY_CURRENCIES);
            currencyLookup.ifPresent(registryLookup -> {
                ItemCurrencyCheque.getVariantForEachCurrency(registryLookup.listElementIds().map(ResourceKey::location))
                        .forEach(variant -> event.accept(variant, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
            });

            event.accept(EternalCurrenciesItems.DEBIT_CARD);
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }

        @SubscribeEvent
        public static void onRegisterKeybinds(RegisterKeyMappingsEvent event) {
            event.register(EternalCurrenciesClient.KEY_OPEN_SHOP);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static class ServerModEvents {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            EternalCurrenciesCommands.register(event.getDispatcher(), event.getBuildContext());

            //EternalCurrenciesRegistries.SHOP_ENTRY_TYPES.get().forEach(codec ->
            //        LOGGER.info("ShopEntry Type Registry entry: {}", codec.toString()));
        }
    }
}
