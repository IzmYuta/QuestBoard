package com.questboard.quest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import com.questboard.QuestMod;

import java.io.File;
import java.io.Reader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class QuestRegistry {
    public static final QuestRegistry INSTANCE = new QuestRegistry();
    private final Map<String, Quest> quests = new HashMap<>();
    private final Gson gson = new GsonBuilder().create();

    public void init() {
        // 初期化処理
    }

    public void loadQuests() {
        File questDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "questboard/quests");
        if (!questDir.exists()) {
            questDir.mkdirs();
            // デフォルトクエストの作成
            createDefaultQuests(questDir);
        }

        // クエストファイルの読み込み
        loadQuestsFromDirectory(questDir);
    }

    private void loadQuestsFromDirectory(File dir) {
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null)
            return;

        for (File file : files) {
            try (Reader reader = Files.newBufferedReader(file.toPath())) {
                Quest quest = gson.fromJson(reader, Quest.class);
                quests.put(quest.getId(), quest);
            } catch (Exception e) {
                QuestMod.LOGGER.error("クエストの読み込みに失敗: " + file.getName(), e);
            }
        }
    }

    private void createDefaultQuests(File questDir) {
        // ゾンビ討伐クエストを作成
        Quest zombieQuest = new Quest();
        zombieQuest.setId("kill_zombie");
        zombieQuest.setTitle("ゾンビ討伐");
        zombieQuest.setDescription("ゾンビを§b10体§r倒す");
        zombieQuest.setQuestType(QuestType.REPEATABLE);

        // JSONとして保存
        File questFile = new File(questDir, "repeatable/kill_zombie.json");
        questFile.getParentFile().mkdirs();
        try {
            Files.write(questFile.toPath(), gson.toJson(zombieQuest).getBytes());
        } catch (IOException e) {
            QuestMod.LOGGER.error("デフォルトクエストの作成に失敗", e);
        }
    }
}