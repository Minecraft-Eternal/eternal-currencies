package com.mceternal.eternalcurrencies.api.shop;

import com.mceternal.eternalcurrencies.api.EternalCurrenciesAPI;
import com.mceternal.eternalcurrencies.data.EternalCurrenciesRegistries;
import com.mceternal.eternalcurrencies.gui.CurrencyShopScreen;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ShopEntry<T> {

    public static final Codec<ShopEntry> DISPATCH_CODEC =
            ExtraCodecs.lazyInitializedCodec(() -> EternalCurrenciesRegistries.SHOP_ENTRY_TYPES.get().getCodec().dispatch(ShopEntry::codec, MapCodec::codec));

    public final String id;
    public final Map<ResourceLocation, Long> costs;
    public final List<ShopRequirement> requirements;

    public ShopEntry(String id, Map<ResourceLocation, Long> costs, List<ShopRequirement> requirements) {
        this.id = id;
        this.costs = costs;
        this.requirements = requirements;
    }

    public ShopEntry(String id, ResourceLocation currency, long cost, List<ShopRequirement> requirements) {
        this(id, Map.of(currency, cost), requirements);
    }

    public ShopEntry(String id, ResourceLocation currency, long cost) {
        this(id, Map.of(currency, cost), List.of());
    }

    public static <T extends ShopEntry> Products.P3<RecordCodecBuilder.Mu<T>, String, Map<ResourceLocation, Long>, List<ShopRequirement>> baseShopEntryFields(RecordCodecBuilder.Instance<T> inst) {
        return inst.group(
                //TODO make a codec that associates Currency IDs (ResourceLocation) to Long.
                Codec.STRING.fieldOf("id").forGetter(e -> e.id),
                Codec.simpleMap(ResourceLocation.CODEC, Codec.LONG,
                        Keyable.forStrings(() ->
                                EternalCurrenciesAPI.getRegisteredCurrencies().keySet().stream().map(ResourceLocation::toString)
                        )).fieldOf("costs").forGetter(e -> e.costs),
                Codec.list(ShopRequirement.DISPATCH_CODEC).optionalFieldOf("requirements", List.of()).forGetter(e -> e.requirements)
        );
    }

    public Component getName() {
        return getDefaultName();
    }

    public abstract Component getDefaultName();

    public abstract Codec<T> contentCodec();

    public abstract MapCodec<? extends ShopEntry> codec();

    //TODO automated purchasing might be cool to allow in the future. possibly a BE that restocks when given redstone pulse?
    /**
     * If this Object is allowed to Purchase this Entry.
     * @param holder Object attempting to Purchase this Entry.
     * @return If this Entry can be Purchased by this Object.
     */
    public boolean canPurchase(ServerPlayer holder) {
        if(!requirements.stream().allMatch(req -> req.meetsRequirement(holder)))
            return false;
        AtomicBoolean pass = new AtomicBoolean(false);
        EternalCurrenciesAPI.getCurrencies(holder).ifPresent(currencies -> {
            if(currencies.hasAtleast(this.costs))
                pass.set(true);
        });
        return pass.get();
    }

    /**
     * Called after {@link ShopEntry#canPurchase} returns true. </p> resolve any effects of Purchasing the Entry here.
     * @param holder Object that has Purchased this Entry.
     */
    public void purchase(ServerPlayer holder) {
        EternalCurrenciesAPI.getCurrencies(holder).ifPresent(currencies ->
                this.costs.forEach(currencies::take));
    }


    public abstract boolean autoPurchasable();


    public void renderTooltip(CurrencyShopScreen.ShopEntryButton button, GuiGraphics pGuiGraphics, Font gameFont, int pMouseX, int pMouseY) {
        pGuiGraphics.renderTooltip(gameFont, button.entry.getName(), pMouseX, pMouseY);
    }
}
