package com.questboard;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.argument.EntityArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PointManager {
    public static final PointManager INSTANCE = new PointManager();
    private final Map<UUID, Integer> balances = new HashMap<>();

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("pay")
                    .requires(source -> source.hasPermissionLevel(0))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(context -> {
                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                        ServerPlayerEntity source = context.getSource().getPlayer();

                                        if (getBalance(source) >= amount) {
                                            addPoints(source, -amount);
                                            addPoints(target, amount);
                                            return 1;
                                        }
                                        return 0;
                                    }))));
        });
    }

    public void init() {
        // コマンド登録
        registerCommands();
    }

    public int getBalance(ServerPlayerEntity player) {
        return balances.getOrDefault(player.getUuid(), 0);
    }

    public void addPoints(ServerPlayerEntity player, int amount) {
        UUID playerId = player.getUuid();
        int newBalance = getBalance(player) + amount;
        balances.put(playerId, newBalance);
        savePlayerData(player);

        // 通知
        player.sendMessage(Text.literal(String.format("+%dP (残高: %dP)", amount, newBalance)));
    }

    private void savePlayerData(ServerPlayerEntity player) {
        NbtCompound data = new NbtCompound();
        data.putInt("questPoints", getBalance(player));
        player.writeCustomDataToNbt(data);
    }

    private void loadPlayerData(ServerPlayerEntity player) {
        NbtCompound data = new NbtCompound();
        player.readCustomDataFromNbt(data);
        if (data.contains("questPoints")) {
            balances.put(player.getUuid(), data.getInt("questPoints").orElse(0));
        }
    }
}