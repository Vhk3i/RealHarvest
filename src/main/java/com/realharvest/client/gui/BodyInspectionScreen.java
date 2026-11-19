package com.realharvest.client.gui;

import com.realharvest.client.health.ClientBodyHealth;
import com.realharvest.health.BodyPart;
import com.realharvest.health.InjuryType;
import com.realharvest.network.ModNetwork;
import com.realharvest.network.packet.C2SHealLimbPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BodyInspectionScreen extends Screen {
    public BodyInspectionScreen() {
        super(Component.literal("Осмотр тела"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Кнопки-конечности, расположенные в форме человеческого тела
        addLimbButton(BodyPart.HEAD, cx - 22, cy - 82, 44, 24);
        addLimbButton(BodyPart.TORSO, cx - 24, cy - 54, 48, 52);
        addLimbButton(BodyPart.LEFT_ARM, cx - 64, cy - 54, 36, 48);
        addLimbButton(BodyPart.RIGHT_ARM, cx + 28, cy - 54, 36, 48);
        addLimbButton(BodyPart.LEFT_LEG, cx - 24, cy + 2, 22, 58);
        addLimbButton(BodyPart.RIGHT_LEG, cx + 2, cy + 2, 22, 58);
    }

    private void addLimbButton(BodyPart part, int x, int y, int w, int h) {
        this.addRenderableWidget(Button.builder(Component.literal(""), btn ->
                ModNetwork.CHANNEL.sendToServer(new C2SHealLimbPacket(part))
        ).bounds(x, y, w, h).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        int cx = this.width / 2;
        int cy = this.height / 2;

        g.drawCenteredString(this.font, "ОСМОТР ТЕЛА (нажмите на раненую конечность)", cx, cy - 105, 0xFFFFFF);

        // Рисуем цветной силуэт тела поверх кнопок
        drawLimbBox(g, BodyPart.HEAD, cx - 22, cy - 82, 44, 24);
        drawLimbBox(g, BodyPart.TORSO, cx - 24, cy - 54, 48, 52);
        drawLimbBox(g, BodyPart.LEFT_ARM, cx - 64, cy - 54, 36, 48);
        drawLimbBox(g, BodyPart.RIGHT_ARM, cx + 28, cy - 54, 36, 48);
        drawLimbBox(g, BodyPart.LEFT_LEG, cx - 24, cy + 2, 22, 58);
        drawLimbBox(g, BodyPart.RIGHT_LEG, cx + 2, cy + 2, 22, 58);

        // Статус всех конечностей справа
        int listY = cy - 70;
        for (BodyPart part : BodyPart.values()) {
            InjuryType t = ClientBodyHealth.get(part);
            g.drawString(this.font, part.getDisplayName() + ": " + t.getLabel(), cx + 82, listY, t.getColor(), true);
            listY += 18;
        }
    }

    private void drawLimbBox(GuiGraphics g, BodyPart part, int x, int y, int w, int h) {
        InjuryType t = ClientBodyHealth.get(part);
        g.fill(x, y, x + w, y + h, t.getColor());
        g.renderOutline(x, y, w, h, 0xFF1A1A1A);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
