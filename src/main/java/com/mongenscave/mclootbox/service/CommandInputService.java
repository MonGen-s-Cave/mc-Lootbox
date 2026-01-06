package com.mongenscave.mclootbox.service;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.identifiers.RewardGroupType;
import com.mongenscave.mclootbox.model.PendingInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandInputService {

    private static final Map<UUID, PendingInput> pending = new ConcurrentHashMap<>();

    public static void requestCommand(@NotNull Player player, String lootboxId, RewardGroupType type, String rewardId) {
        UUID uuid = player.getUniqueId();

        PendingInput input = new PendingInput(
                lootboxId,
                type,
                rewardId,
                System.currentTimeMillis() + 30_000L
        );

        pending.put(uuid, input);

        // TODO: Ide meg kell MessageKeys vagy configba rakni :)
        player.showTitle(Title.title(
                Component.text("§bADD COMMAND"),
                Component.text("§7Type your command in the chat"))
        );

        player.sendMessage("§7Type the command without §f/§7.");
        player.sendMessage("§7Use §ccancel §7to abort.");
        player.sendMessage("§7You can use placeholders like §f{player}§7.");

        Bukkit.getScheduler().runTaskLater(
                McLootbox.getInstance(),
                () -> {
                    PendingInput current = pending.get(uuid);
                    if (current != null && current.isExpired()) {
                        pending.remove(uuid);
                        player.sendMessage("§cCommand input timed out.");
                    }
                },
                20L * 30
        );
    }

    public static PendingInput get(UUID uuid) {
        return pending.get(uuid);
    }

    public static void clear(UUID uuid) {
        pending.remove(uuid);
    }
}
