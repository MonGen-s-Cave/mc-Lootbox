package com.mongenscave.mclootbox.service;

import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.model.LootboxReward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class LootboxRewardService {

    private LootboxRewardService() {
    }

    public static void give(Player player, @NotNull LootboxReward reward) {
        if (reward.isGiveItem()) giveItem(player, reward);

        for (String command : reward.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
        }
    }

    private static void giveItem(@NotNull Player player, @NotNull LootboxReward reward) {
        if (reward.getItemSection() == null) return;

        Optional<ItemStack> optional = ItemFactory.buildItem(
                reward.getItemSection(),
                reward.getItemPath()
        );

        optional.ifPresent(item -> player.getInventory().addItem(item));
    }
}