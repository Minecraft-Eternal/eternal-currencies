package com.mceternal.eternalcurrencies.network;

import com.mceternal.eternalcurrencies.EternalCurrencies;
import com.mceternal.eternalcurrencies.api.shop.ShopEntry;
import com.mceternal.eternalcurrencies.data.EternalCurrenciesRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class C2SBuyShopEntryPacket implements IntSupplier {

    public final ResourceLocation category;
    public final String target;

    public C2SBuyShopEntryPacket(ResourceLocation category, String target) {
        this.category = category;
        this.target = target;
    }

    public C2SBuyShopEntryPacket(FriendlyByteBuf buf) {
        this.category = buf.readResourceLocation();
        this.target = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.category);
        buf.writeUtf(this.target);
    }

    public void handle(Supplier<Context> contextSupplier) {
        Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            sender.sendSystemMessage(Component.literal("Received BuyShopEntryPacket for '"+ target +"' in '"+ category.toString() +"'"));
            //TODO: this ifPresent is failing. why?
            sender.level().registryAccess().registry(EternalCurrenciesRegistries.KEY_SHOP_CATEGORIES).ifPresent(categories -> {
                Optional<ShopEntry> entry = categories.get(category).getEntry(target);
                EternalCurrencies.LOGGER.info("ShopEntry isPresent: {}, canPurchase: {}",
                        entry.isPresent(), entry.map(shopEntry -> shopEntry.canPurchase(sender)).orElse(false));

                if (entry.isPresent() && entry.get().canPurchase(sender)) {
                    EternalCurrencies.LOGGER.info("Purchasing entry {}", target);
                    entry.get().purchase(sender);
                }
            });
        });
        context.setPacketHandled(true);
    }

    @Override
    public int getAsInt() {
        return hashCode();
    }
}
