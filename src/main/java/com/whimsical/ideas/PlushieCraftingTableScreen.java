package com.whimsical.ideas;

import com.whimsical.ideas.client.SkinFetcher;
import com.whimsical.ideas.network.CraftPlushiePacket;
import com.whimsical.ideas.network.ModNetwork;
import com.whimsical.ideas.network.SkinUploadPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class PlushieCraftingTableScreen extends AbstractContainerScreen<PlushieCraftingTableMenu> {

    private static final ResourceLocation BG =
            new ResourceLocation(WhimsicalIdeas.MOD_ID, "textures/gui/plushie_crafting_table.png");

    /** 皮肤来源：0=正版，1=皮肤站 */
    private static int skinApiMode = 0;

    private EditBox nameBox;
    private CycleButton<String> localSkinButton;
    private Button apiButton;

    public PlushieCraftingTableScreen(PlushieCraftingTableMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 175;
        this.imageHeight = 167;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 名字输入框：输入槽右边 (30, 14)
        this.nameBox = new EditBox(this.font, x + 30, y + 14, 100, 16,
                Component.translatable("gui.whimsical_ideas.name"));
        this.nameBox.setMaxLength(16);
        this.nameBox.setHint(Component.translatable("gui.whimsical_ideas.name_hint"));
        this.addRenderableWidget(this.nameBox);

        // 本地图片下拉框：图片 (29, 35)
        List<String> localFiles = SkinFetcher.listLocalSkins();
        if (!localFiles.isEmpty()) {
            this.localSkinButton = CycleButton.<String>builder(
                            (value) -> Component.literal(value)
                    )
                    .withValues(localFiles)
                    .withInitialValue(localFiles.get(0))
                    .create(x + 29, y + 35, 100, 16,
                            Component.translatable("gui.whimsical_ideas.local_skin"));
            this.addRenderableWidget(this.localSkinButton);
        }

        // 制作按钮：下拉框右边 (135, 35)
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.whimsical_ideas.craft"),
                b -> {
                    String name = nameBox.getValue().trim();
                    String localFile = localSkinButton != null ? localSkinButton.getValue() : null;

                    if (name.isEmpty() && localFile != null) {
                        ModNetwork.CHANNEL.sendToServer(new CraftPlushiePacket("local:" + localFile));
                        SkinFetcher.fetchLocalBytes(localFile, data -> {
                            if (data != null && data.length > 0) {
                                ModNetwork.CHANNEL.sendToServer(new SkinUploadPacket("local:" + localFile, data));
                            }
                        });
                    } else if (!name.isEmpty()) {
                        ModNetwork.CHANNEL.sendToServer(new CraftPlushiePacket(name));
                        SkinFetcher.fetchSkinBytes(name, data -> {
                            if (data != null && data.length > 0) {
                                ModNetwork.CHANNEL.sendToServer(new SkinUploadPacket(name, data));
                            }
                        });
                    }
                }
        ).bounds(x + 135, y + 35, 30, 16).build());

        // API 切换按钮：下拉框下方 (29, 55)
        this.apiButton = Button.builder(
                getApiButtonText(),
                b -> {
                    skinApiMode = (skinApiMode + 1) % 2;
                    b.setMessage(getApiButtonText());
                }
        ).bounds(x + 29, y + 55, 136, 16).build();
        this.addRenderableWidget(this.apiButton);

        // 问号：右上角 (156, 4)
        Button helpButton = Button.builder(Component.literal("?"), b -> {})
                .bounds(x + imageWidth - 20, y + 4, 16, 16).build();
        helpButton.setTooltip(Tooltip.create(
                Component.translatable("gui.whimsical_ideas.help")
        ));
        this.addRenderableWidget(helpButton);
    }

    private Component getApiButtonText() {
        return skinApiMode == 0
                ? Component.translatable("gui.whimsical_ideas.api.mojang")
                : Component.translatable("gui.whimsical_ideas.api.littleskin");
    }

    public static int getSkinApiMode() {
        return skinApiMode;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        gfx.blit(BG, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
    }
}