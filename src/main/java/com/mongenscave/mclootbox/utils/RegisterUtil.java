package com.mongenscave.mclootbox.utils;

import com.mongenscave.mclootbox.McLootbox;
import lombok.experimental.UtilityClass;
import revxrsal.commands.bukkit.BukkitLamp;

@UtilityClass
public class RegisterUtil {
    private final McLootbox plugin = McLootbox.getInstance();

    public void registerCommands() {
        var lamp = BukkitLamp.builder(plugin).build();

        //lamp.register();
    }
}