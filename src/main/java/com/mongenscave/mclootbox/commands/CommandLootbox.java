package com.mongenscave.mclootbox.commands;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.identifiers.keys.MessageKeys;
import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"mc-lootbox", "mclootbox", "lootbox"})
@SuppressWarnings("unused")
public class CommandLootbox {
    private final McLootbox plugin = McLootbox.getInstance();

    @Subcommand("reload")
    @CommandPermission("mclootbox.reload")
    public void reload(@NotNull CommandSender sender) {
        plugin.getConfiguration().reload();
        plugin.getLanguage().reload();
        plugin.getGuis().reload();

        plugin.getLootboxManager().load();

        sender.sendMessage(MessageKeys.COMMAND_RELOAD.getMessage());
    }

    @Subcommand("give")
    @CommandPermission("mclootbox.give")
    public void give(@NotNull CommandSender sender, @NotNull OfflinePlayer player, @NotNull String lootboxId, int amount) {
        final int finalAmount = amount <= 0 ? 1 : amount;

        Player target = Bukkit.getPlayer(player.getUniqueId());
        if (target == null) {
            sender.sendMessage(MessageKeys.PLAYER_NOT_FOUND.getMessage());
            return;
        }

        Lootbox lootbox = plugin.getLootboxManager()
                .getLootbox(lootboxId)
                .orElse(null);

        if (lootbox == null) {
            sender.sendMessage(MessageKeys.LOOTBOX_NOT_FOUND.getMessage());
            return;
        }

        plugin.getLootboxManager()
                .createLootboxItem(lootbox, finalAmount)
                .ifPresentOrElse(
                        item -> {
                            target.getInventory().addItem(item);

                            sender.sendMessage(
                                    MessageKeys.LOOTBOX_GIVEN.getMessage()
                                            .replace("{player}", target.getName())
                                            .replace("{lootbox_id}", lootbox.getId())
                                            .replace("{lootbox}", MessageProcessor.process(lootbox.getDisplayName()))
                                            .replace("{amount}", String.valueOf(finalAmount)));

                            target.sendMessage(
                                    MessageKeys.LOOTBOX_RECEIVED.getMessage()
                                            .replace("{lootbox_id}", lootbox.getId())
                                            .replace("{lootbox}", MessageProcessor.process(lootbox.getDisplayName()))
                                            .replace("{amount}", String.valueOf(finalAmount)));
                        },
                        () -> sender.sendMessage(MessageKeys.LOOTBOX_ITEM_ERROR.getMessage()));
    }
}