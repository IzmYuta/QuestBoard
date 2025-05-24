package com.questboard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.questboard.quest.QuestRegistry;

public class QuestMod implements ModInitializer {
    public static final String MOD_ID = "questmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("クエストMod初期化中...");

        // 各マネージャーの初期化
        PointManager.INSTANCE.init();
        QuestRegistry.INSTANCE.init();

        // サーバー起動時のイベント登録
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            QuestRegistry.INSTANCE.loadQuests();
        });
    }
}