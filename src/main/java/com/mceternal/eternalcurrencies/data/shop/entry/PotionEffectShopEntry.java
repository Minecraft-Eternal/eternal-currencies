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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PotionEffectShopEntry extends ShopEntry<MobEffectInstance> {

    public static final MapCodec<MobEffectInstance> EFFECT_INSTANCE_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(MobEffectInstance::getEffect),
            Codec.INT.fieldOf("duration").forGetter(MobEffectInstance::getDuration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(MobEffectInstance::getAmplifier),
            Codec.BOOL.optionalFieldOf("visible", true).forGetter(MobEffectInstance::isVisible),
            Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(MobEffectInstance::showIcon)
    ).apply(inst, MobEffectInstance::new));

    public static final MapCodec<PotionEffectShopEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            EFFECT_INSTANCE_CODEC.fieldOf("effect").forGetter(e -> e.effect))
            .and(baseShopEntryFields(inst))
            .apply(inst, PotionEffectShopEntry::new));

    public final MobEffectInstance effect;

    public PotionEffectShopEntry(MobEffectInstance effect, String name, Map<ResourceLocation, Long> costs, List<ShopRequirement> requirements) {
        super(name, costs, requirements);
        this.effect = effect;
    }

    @Override
    public Component getDefaultName() {
        return effect.getEffect().getDisplayName();
    }

    @Override
    public Codec<MobEffectInstance> contentCodec() {
        return EFFECT_INSTANCE_CODEC.codec();
    }

    @Override
    public MapCodec<? extends ShopEntry> codec() {
        return CODEC;
    }

    @Override
    public void purchase(ServerPlayer holder) {
        super.purchase(holder);
        holder.addEffect(effect);
    }

    @Override
    public boolean autoPurchasable() {
        return false;
    }

    @Override
    public void renderTooltip(CurrencyShopScreen.ShopEntryButton button, GuiGraphics pGuiGraphics, Font gameFont, int pMouseX, int pMouseY) {
        List<Component> desc = new ArrayList<>();
        PotionUtils.addPotionTooltip(List.of(effect), desc, 1.0f);
        pGuiGraphics.renderTooltip(gameFont, desc, Optional.empty(), pMouseX, pMouseY);
    }
}
