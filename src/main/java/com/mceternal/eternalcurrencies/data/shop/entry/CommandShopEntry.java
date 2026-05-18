package com.mceternal.eternalcurrencies.data.shop.entry;

import com.mceternal.eternalcurrencies.api.shop.ShopRequirement;
import com.mceternal.eternalcurrencies.api.shop.ShopEntry;
import com.mceternal.eternalcurrencies.gui.CurrencyShopScreen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;

public class CommandShopEntry extends ShopEntry<String> {

    public static final MapCodec<CommandShopEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("command").forGetter(e -> e.command))
            .and(baseShopEntryFields(inst))
            .apply(inst, CommandShopEntry::new));

    public final String command;

    public CommandShopEntry(String command, String name, Map<ResourceLocation, Long> costs, List<ShopRequirement> requirements) {
        super(name, costs, requirements);
        this.command = command;
    }

    public CommandShopEntry(String command, String name, ResourceLocation currency, long cost, List<ShopRequirement> requirements) {
        super(name, currency, cost, requirements);
        this.command = command;
    }

    @Override
    public Component getDefaultName() {
        return Component.literal(command);
    }

    @Override
    public Codec<String> contentCodec() {
        return Codec.STRING;
    }

    @Override
    public MapCodec<? extends ShopEntry> codec() {
        return CODEC;
    }

    @Override
    public void purchase(ServerPlayer holder) {
        super.purchase(holder);
        MinecraftServer server = holder.server;
        server.getCommands().performPrefixedCommand(holder.createCommandSourceStack().withPermission(2), command);
    }

    @Override
    public boolean autoPurchasable() {
        return false;
    }

    @Override
    public void renderTooltip(CurrencyShopScreen.ShopEntryButton button, GuiGraphics pGuiGraphics, Font gameFont, int pMouseX, int pMouseY) {
        pGuiGraphics.renderTooltip(gameFont, Component.translatable("shopentry.eternalcurrencies.command", this.getName()), pMouseX, pMouseY);
    }
}
