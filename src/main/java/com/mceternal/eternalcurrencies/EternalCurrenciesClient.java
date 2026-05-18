package com.mceternal.eternalcurrencies;

import com.mceternal.eternalcurrencies.gui.CurrencyShopScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class EternalCurrenciesClient {

    private EternalCurrenciesClient() {}

    public static final String SHOP_KEY_NAME = "key."+ EternalCurrencies.MODID +".open_shop";
    public static final KeyMapping KEY_OPEN_SHOP = new KeyMapping(SHOP_KEY_NAME, GLFW.GLFW_KEY_APOSTROPHE, "keys."+ EternalCurrencies.MODID +".category");

    public static void init() {
        if(EternalCurrencies.FTBQ_LOADED) {
            //QuestsIntegrationClient.setupRewardGuiProviders();
            //QuestsIntegrationClient.setupTaskGuiProviders();
        }

        MinecraftForge.EVENT_BUS.addListener(EternalCurrenciesClient::onClientTick);
    }

    public static void openShopScreen() {
        CurrencyShopScreen shopScreen = new CurrencyShopScreen();
        Minecraft.getInstance().setScreen(shopScreen);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.ClientTickEvent.Phase.END && KEY_OPEN_SHOP.consumeClick() && Minecraft.getInstance().isWindowActive())
            openShopScreen();
    }
}
