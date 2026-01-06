package com.mongenscave.mclootbox.listeners;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.handler.LootboxEditorCreateHandler;
import com.mongenscave.mclootbox.model.PendingInput;
import com.mongenscave.mclootbox.service.LootboxEditorService;
import com.mongenscave.mclootbox.service.CommandInputService;
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
        String message = event.getMessage().trim();

        PendingInput rewardInput = CommandInputService.get(player.getUniqueId());
        if (rewardInput != null) {
            event.setCancelled(true);

            // TODO: MessageKeys
            if (message.equalsIgnoreCase("cancel")) {
                CommandInputService.clear(player.getUniqueId());
                player.sendMessage("§cCommand input cancelled.");
                return;
            }

            McLootbox.getInstance()
                    .getEditorService()
                    .addCommandToReward(
                            rewardInput.lootboxId(),
                            rewardInput.type(),
                            rewardInput.rewardId(),
                            message
                    );

            CommandInputService.clear(player.getUniqueId());

            player.sendMessage("§aCommand added:");
            player.sendMessage("§7" + message);
            return;
        }

        LootboxEditorCreateHandler createHandler =
                LootboxEditorService.getInstance().createHandler();

        if (!createHandler.isPending(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        createHandler.handle(player, message);
    }
}