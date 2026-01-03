package com.mongenscave.mclootbox.listeners;

import com.mongenscave.mclootbox.handler.LootboxEditorCreateHandler;
import com.mongenscave.mclootbox.service.LootboxEditorService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public final class LootboxEditorListener implements Listener {

    @EventHandler
    public void onChat(@NotNull AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        LootboxEditorCreateHandler handler = LootboxEditorService.getInstance().createHandler();
        if (!handler.isPending(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        handler.handle(player, event.getMessage());
    }
}