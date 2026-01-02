package com.mongenscave.mclootbox.utils;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.commands.CommandLootbox;
import com.mongenscave.mclootbox.listeners.LootboxListener;
import lombok.experimental.UtilityClass;
import revxrsal.commands.bukkit.BukkitLamp;

@UtilityClass
public class RegisterUtil {
    private final McLootbox plugin = McLootbox.getInstance();

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new LootboxListener(), plugin);
    }

    public void registerCommands() {
        var lamp = BukkitLamp.builder(plugin).build();

        lamp.register(new CommandLootbox());
    }
}