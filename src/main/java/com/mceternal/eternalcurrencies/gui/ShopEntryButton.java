package com.mceternal.eternalcurrencies.gui;

import com.mceternal.eternalcurrencies.api.shop.ShopEntry;
import com.mceternal.eternalcurrencies.network.C2SBuyShopEntryPacket;
import com.mceternal.eternalcurrencies.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ShopEntryButton extends AbstractButton {

    public final ResourceLocation parentCategory;
    public final ShopEntry<?> content;

    protected ShopEntryButton(int x, int index, ResourceLocation parentCategory, ShopEntry<?> content) {
        super(x, 5 + (15 * index), 80, 15, CommonComponents.EMPTY);
        this.parentCategory = parentCategory;
        this.content = content;
        //this.setTooltip(Tooltip.create(content.getName()));
    }

    public void onPress() {
        Minecraft.getInstance().player.displayClientMessage(Component.literal("Clicked a Shop Entry!"), false);
        ShopEntry<?> entry = this.content;
        PacketHandler.sendToServer(new C2SBuyShopEntryPacket(this.parentCategory, entry.id));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        this.defaultButtonNarrationText(pNarrationElementOutput);
    }

    public void renderTooltip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        //this.content.renderTooltip(this, pGuiGraphics, Minecraft.getInstance().font, pMouseX, pMouseY);
    }
}
