package com.questboard;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class QuestModClient implements ClientModInitializer {
    private static KeyBinding questBoardKey;

    @Override
    public void onInitializeClient() {
        // Rキーをクエストボード表示のデフォルトキーとして登録
        questBoardKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.questboard.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.questboard"));

        // キー入力の検知
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (questBoardKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new QuestBoardScreen());
                }
            }
        });
    }
}