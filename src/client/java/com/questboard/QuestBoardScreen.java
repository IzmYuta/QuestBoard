package com.questboard;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.client.MinecraftClient;

public class QuestBoardScreen extends Screen {
    private static final int GRID_SIZE = 9;
    private static final int ROWS = 3;

    public QuestBoardScreen() {
        super(Text.translatable("gui.questboard.title"));
    }

    @Override
    protected void init() {
        super.init();

        // GUIが開かれた時にサウンドを再生
        MinecraftClient.getInstance().player.playSound(
                SoundEvents.UI_TOAST_IN,
                1.0F,
                1.0F);

        // 進行中/未受注タブのボタンを追加
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.questboard.active"),
                button -> showActiveQuests())
                .dimensions(10, 10, 100, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.questboard.available"),
                button -> showAvailableQuests())
                .dimensions(120, 10, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // タイトルを描画
        context.drawTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2 - this.textRenderer.getWidth(this.title) / 2,
                10,
                0xFFFFFF);
    }

    private void showActiveQuests() {
        // 進行中のクエスト表示ロジック
    }

    private void showAvailableQuests() {
        // 未受注クエスト表示ロジック
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}