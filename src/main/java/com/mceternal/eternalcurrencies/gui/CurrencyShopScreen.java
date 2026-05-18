package com.mceternal.eternalcurrencies.gui;

import com.mceternal.eternalcurrencies.EternalCurrencies;
import com.mceternal.eternalcurrencies.api.shop.ShopEntry;
import com.mceternal.eternalcurrencies.data.EternalCurrenciesRegistries;
import com.mceternal.eternalcurrencies.data.shop.ShopCategory;
import com.mceternal.eternalcurrencies.network.C2SBuyShopEntryPacket;
import com.mceternal.eternalcurrencies.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class CurrencyShopScreen extends Screen {

    //i know i haven't committed this texture file, it's a mockup asset while i'm still working out the fundamentals.
    //proper assets and look come once it *works*
    public static final ResourceLocation TEXTURE = EternalCurrencies.resource("textures/gui/shop_mockup_plain.png");

    public static ResourceLocation lastCategory;

    private ResourceLocation currentCategory = lastCategory != null ? lastCategory : EternalCurrencies.resource("exchange");
    private int entryScrollPos = 0;
    private int categoryScrollPos = 0;

    private Registry<ShopCategory> categories;

    private static final int imageWidth = 960;
    private static final int imageHeight = 960;

    public static final int categoryScrollOriginPos = 120;
    public static final int categoryScrollHeight = 60;
    public static final int categoryScrollWidth = 20;

    public CurrencyShopScreen() {
        super(Component.translatable("screen.eternalcurrencies.shop.name"));
    }

    @Override
    protected void init() {
        this.categories = getShopCategories();
        loadEntries();
        addCategoryButtons();
    }

    @Override
    public Component getTitle() {
        return super.getTitle();
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        int x = (this.width - imageWidth) / 2;
        int y = (this.height - imageHeight) / 2;
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, 960, 960, 960, 960);
        pGuiGraphics.renderTooltip(this.font, Component.literal("Category Scroll: "+ this.categoryScrollPos), 0, 16);
        pGuiGraphics.renderTooltip(this.font, Component.literal("Entry Scroll: "+ this.entryScrollPos), 0, 28);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    public void changeCategory(ResourceLocation newCategory) {
        if(this.currentCategory != newCategory) {
            EternalCurrencies.LOGGER.info("Changing category for {} from '{}' to '{}'", this, this.currentCategory, newCategory);
            this.currentCategory = newCategory;
            this.entryScrollPos = 0;
            this.categoryScrollPos = 0;
            this.rebuildWidgets();
        }
    }

    public void addEntry(ShopEntry<?> entry) {
        ShopEntryButton entryButton = new ShopEntryButton(100, this.children().size(), currentCategory, entry);
        this.addRenderableWidget(entryButton);
    }

    public void loadEntries() {
        Objects.requireNonNull(categories.get(currentCategory)).getEntries().forEach(entry -> {
            EternalCurrencies.LOGGER.info("Found ShopEntry {}", entry.id);
            this.addEntry(entry);
        });
    }

    public void addCategoryButtons() {
        int index = 1;
        for (ResourceKey<ShopCategory> key : categories.registryKeySet()) {
            ChangeCategoryButton button = new ChangeCategoryButton(index, key.location());
            this.addRenderableWidget(button);
            index++;
        }
    }

    public Registry<ShopCategory> getShopCategories() {
        return minecraft.level.registryAccess().registry(EternalCurrenciesRegistries.KEY_SHOP_CATEGORIES)
                .orElseThrow(() -> new RuntimeException("Could not get ShopCategory registry in "+ this));
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        //super.mouseScrolled(pMouseX, pMouseY, pDelta);
        AtomicBoolean consume = new AtomicBoolean(false);
        int cornerPos1 = categoryScrollOriginPos + categoryScrollHeight;
        int cornerPos2 = categoryScrollOriginPos + categoryScrollWidth;
        if(this.canScrollCategories()
                && (pMouseX > cornerPos1 || pMouseX < cornerPos1)
                && (pMouseY > cornerPos2 || pMouseY < cornerPos2)) {
            int maxScroll = this.categories.size() * 10;
            int newPos = (int) Mth.clamp(this.categoryScrollPos - pDelta, 0, maxScroll);

            if(categoryScrollPos != newPos)
                consume.set(true);
            this.categoryScrollPos = newPos;
        } else if(this.canScrollEntries()){
            int maxScroll = this.categories.get(this.currentCategory).getEntries().size() * 10;
            int newPos = (int) Mth.clamp(this.entryScrollPos - pDelta, 0, maxScroll);

            if(entryScrollPos != newPos)
                consume.set(true);
            this.entryScrollPos = newPos;
        }
        return consume.get();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public boolean canScrollEntries() {
        return this.categories.getOptional(this.currentCategory)
                .filter(shopCategory -> shopCategory.getEntries().size() > 7)
                .isPresent();
    }

    public boolean canScrollCategories() {
        return this.categories.size() > 7;
    }


    public class ShopEntryButton extends AbstractButton {

        public final ResourceLocation parentCategory;
        public final ShopEntry<?> entry;

        protected ShopEntryButton(int x, int index, ResourceLocation parentCategory, ShopEntry<?> entry) {
            super(x, 5 + (15 * index), 80, 15, CommonComponents.EMPTY);
            this.parentCategory = parentCategory;
            this.entry = entry;
            //this.setTooltip(Tooltip.create(content.getName()));
        }

        public void onPress() {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Clicked Shop Entry! ID: "+ this.entry.id), false);
            PacketHandler.sendToServer(new C2SBuyShopEntryPacket(this.parentCategory, this.entry.id));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        }

        public void renderTooltip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
            if(this.isHovered)
                this.entry.renderTooltip(this, pGuiGraphics, Minecraft.getInstance().font, pMouseX, pMouseY);
        }
    }

    public class ChangeCategoryButton extends Button {

        public final ResourceLocation category;

        public ChangeCategoryButton(int index, ResourceLocation category) {
            super(80, 5 + (15 * index), 20, 15, Component.literal(category.toString()), button -> {}, DEFAULT_NARRATION);
            this.category = category;
            this.setTooltip(Tooltip.create(Component.translatable("eternalcurrencies.shop.category."+ category)));
        }

        public void onPress() {
            Screen screen = Minecraft.getInstance().screen;
            EternalCurrencies.LOGGER.info("CurrencyShopScreen: {}", screen instanceof CurrencyShopScreen);
            if(screen instanceof CurrencyShopScreen shopScreen) {
                shopScreen.changeCategory(this.category);
            }
        }
    }
}
