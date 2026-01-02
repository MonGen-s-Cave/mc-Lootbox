package com.mongenscave.mclootbox.service;

import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.model.LootboxReward;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class LootboxRewardService {

    private LootboxRewardService() {
    }

    public static void give(Player player, @NotNull LootboxReward reward) {
        if (reward.isGiveItem()) giveItem(player, reward);

        for (String command : reward.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
        }
    }

    private static void giveItem(Player player, @NotNull LootboxReward reward) {
        Material material;
        try {
            material = Material.valueOf(reward.getMaterial().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return;
        }

        ItemStack item = ItemFactory.create(material, reward.getAmount())
                .setName(reward.getName())
                .setLore(reward.getLore())
                .finish();

        player.getInventory().addItem(item);
    }
}