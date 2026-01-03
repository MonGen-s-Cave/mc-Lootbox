package com.mongenscave.mclootbox.utils;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.annotions.Lootboxes;
import com.mongenscave.mclootbox.commands.CommandLootbox;
import com.mongenscave.mclootbox.listeners.LootboxEditorListener;
import com.mongenscave.mclootbox.listeners.LootboxListener;
import com.mongenscave.mclootbox.listeners.MenuListener;
import com.mongenscave.mclootbox.suggestions.LootboxesSuggestionProvider;
import lombok.experimental.UtilityClass;
import revxrsal.commands.bukkit.BukkitLamp;

@UtilityClass
public class RegisterUtil {
    private final McLootbox plugin = McLootbox.getInstance();

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new LootboxListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MenuListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new LootboxEditorListener(), plugin);
    }

    public void registerCommands() {
        var lamp = BukkitLamp.builder(plugin)
                .suggestionProviders(registry -> {
                    registry.addProviderForAnnotation(
                            Lootboxes.class,
                            annotation -> new LootboxesSuggestionProvider<>()
                    );
                }).build();

        lamp.register(new CommandLootbox());
    }
}