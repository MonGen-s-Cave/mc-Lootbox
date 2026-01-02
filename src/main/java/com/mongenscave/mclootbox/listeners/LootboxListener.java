package com.mongenscave.mclootbox.listeners;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.identifiers.LootboxKeys;
import com.mongenscave.mclootbox.manager.LootboxRewardManager;
import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.model.LootboxReward;
import com.mongenscave.mclootbox.service.LootboxRewardService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LootboxListener implements Listener {

    private final McLootbox plugin = McLootbox.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String lootboxId = pdc.get(LootboxKeys.LOOTBOX_ID, PersistentDataType.STRING);
        if (lootboxId == null) return;

        event.setCancelled(true);

        Lootbox lootbox = plugin.getLootboxManager()
                .getLootbox(lootboxId)
                .orElse(null);

        if (lootbox == null) return;

        consumeOne(player, event.getHand());

        List<LootboxReward> rewards = LootboxRewardManager.roll(lootbox.getRewards().getRewards(), lootbox.getRewards().getPrizeSize());

        for (LootboxReward reward : rewards) {
            LootboxRewardService.give(player, reward);
        }
    }

    private void consumeOne(@NotNull Player player, EquipmentSlot hand) {
        ItemStack stack = player.getInventory().getItem(hand);

        int amount = stack.getAmount();
        if (amount <= 1) {
            player.getInventory().setItem(hand, null);
        } else {
            stack.setAmount(amount - 1);
        }
    }
}