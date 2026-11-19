package com.realharvest.client.gui;

import com.realharvest.blueprint.Blueprint;
import com.realharvest.blueprint.BlueprintRegistry;
import com.realharvest.client.blueprint.ClientBlueprintManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class SurvivalGuideScreen extends Screen {
    private static final int BOOK_W = 280;
    private static final int BOOK_H = 190;
    private final List<Blueprint> list = new ArrayList<>();

    public SurvivalGuideScreen() {
        super(Component.literal("Книжка выживальщика"));
        list.addAll(BlueprintRegistry.getAll());
    }

    @Override
    protected void init() {
        int left = (this.width - BOOK_W) / 2;
        int top = (this.height - BOOK_H) / 2;

        for (int i = 0; i < list.size(); i++) {
            Blueprint bp = list.get(i);
            int col = i % 2;
            int row = i / 2;
            int bx = left + 18 + col * 130;
            int by = top + 40 + row * 68;

            this.addRenderableWidget(Button.builder(Component.literal(bp.getTitle()), btn -> {
                ClientBlueprintManager.setActiveBlueprint(bp);
                this.onClose();
            }).bounds(bx, by + 34, 114, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int left = (this.width - BOOK_W) / 2;
        int top = (this.height - BOOK_H) / 2;

        // Кожаная обложка и страницы разворота книги
        g.fill(left - 6, top - 6, left + BOOK_W + 6, top + BOOK_H + 6, 0xFF4A2B16);
        g.fill(left, top, left + BOOK_W, top + BOOK_H, 0xFFE8D8B5);
        // Центральный переплёт
        g.fill(left + BOOK_W / 2 - 2, top, left + BOOK_W / 2 + 2, top + BOOK_H, 0xFFC4B08A);

        g.drawCenteredString(this.font, "КНИЖКА ВЫЖИВАЛЬЩИКА", left + BOOK_W / 2, top + 12, 0x3B2312);

        // Карточки с картинками (иконками) чертежей
        for (int i = 0; i < list.size(); i++) {
            Blueprint bp = list.get(i);
            int col = i % 2;
            int row = i / 2;
            int bx = left + 18 + col * 130;
            int by = top + 40 + row * 68;

            g.fill(bx, by, bx + 114, by + 32, 0xFFD6C29B);
            g.renderItem(bp.getIcon(), bx + 8, by + 8);
            g.drawString(this.font, bp.getSubtitle(), bx + 30, by + 12, 0x4A3420, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
