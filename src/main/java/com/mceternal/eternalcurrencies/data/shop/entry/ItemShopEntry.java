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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class ItemShopEntry extends ShopEntry<ItemStack> {

    public static final MapCodec<ItemShopEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ItemStack.CODEC.fieldOf("item").forGetter(e -> e.item))
            .and(baseShopEntryFields(inst))
            .apply(inst, ItemShopEntry::new));

    public final ItemStack item;

    public ItemShopEntry(ItemStack item, String name, Map<ResourceLocation, Long> costs, List<ShopRequirement> requirements) {
        super(name, costs, requirements);
        this.item = item;
    }

    public ItemShopEntry(ItemStack item, String name, ResourceLocation currency, long cost, List<ShopRequirement> requirements) {
        super(name, currency, cost, requirements);
        this.item = item;
    }

    @Override
    public Component getDefaultName() {
        return item.getDisplayName();
    }

    @Override
    public Codec<ItemStack> contentCodec() {
        return ItemStack.CODEC;
    }

    @Override
    public MapCodec<ItemShopEntry> codec() {
        return CODEC;
    }

    @Override
    public void purchase(ServerPlayer holder) {
        super.purchase(holder);

        ItemEntity dropped = new ItemEntity(holder.level(), holder.position().x, holder.position().y, holder.position().z, this.item);
        dropped.setNoPickUpDelay();
        dropped.setTarget(holder.getUUID());
        holder.level().addFreshEntity(dropped);
    }

    @Override
    public boolean autoPurchasable() {
        return true;
    }

    @Override
    public void renderTooltip(CurrencyShopScreen.ShopEntryButton button, GuiGraphics pGuiGraphics, Font gameFont, int pMouseX, int pMouseY) {
        pGuiGraphics.renderTooltip(gameFont, this.item, pMouseX, pMouseY);
    }
}
