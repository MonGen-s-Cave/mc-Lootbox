package com.mongenscave.mclootbox.handler;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.guis.impl.editor.LootboxEditorMainMenu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LootboxEditorCreateHandler {

    private final ConcurrentHashMap<UUID, Boolean> pending = new ConcurrentHashMap<>();

    public void begin(@NotNull Player player) {
        pending.put(player.getUniqueId(), Boolean.TRUE);

        player.closeInventory();
        player.sendMessage("Type the new lootbox id in chat. Type 'cancel' to abort.");
    }

    public boolean isPending(UUID uuid) {
        return pending.containsKey(uuid);
    }

    public void cancel(UUID uuid) {
        pending.remove(uuid);
    }

    public void handle(Player player, @NotNull String message) {
        String input = message.trim();

        if (input.equalsIgnoreCase("cancel")) {
            pending.remove(player.getUniqueId());
            player.sendMessage("Lootbox creation cancelled.");

            LootboxEditorMainMenu.open(player);
            return;
        }

        String id = input.toLowerCase(Locale.ROOT);
        McLootbox plugin = McLootbox.getInstance();

        plugin.getLootboxManager().createLootboxFile(id).thenCompose(created -> {
            if (!created) {
                return plugin.getLootboxManager().reload().thenApply(v -> false);
            }

            return plugin.getLootboxManager().reload().thenApply(v -> true);
        }).thenAccept(success -> {
            pending.remove(player.getUniqueId());

            McLootbox.getScheduler().runTask(() -> {
                if (success) {
                    player.sendMessage("Lootbox created.");
                } else {
                    player.sendMessage("Invalid id or file already exists.");
                }

                LootboxEditorMainMenu.open(player);
            });
        });
    }
}
