package com.mongenscave.mclootbox.commands;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.identifiers.keys.MessageKeys;
import org.bukkit.command.CommandSender;
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

        sender.sendMessage(MessageKeys.COMMAND_ADMIN_RELOAD.getMessage());
    }
}