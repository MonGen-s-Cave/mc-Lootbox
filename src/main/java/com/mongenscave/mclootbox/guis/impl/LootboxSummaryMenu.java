package com.mongenscave.mclootbox.guis.impl;

import com.mongenscave.mclootbox.data.MenuController;
import com.mongenscave.mclootbox.guis.Menu;
import com.mongenscave.mclootbox.identifiers.keys.ItemKeys;
import com.mongenscave.mclootbox.identifiers.keys.MenuKeys;
import com.mongenscave.mclootbox.model.LootboxSummary;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("deprecation")
public final class LootboxSummaryMenu extends Menu {
    private final LootboxSummary entry;

    public LootboxSummaryMenu(@NotNull MenuController controller, @NotNull LootboxSummary entry) {
        super(controller);
        this.entry = entry;
    }

    @Override
    public void setMenuItems() {
        inventory.clear();

        ItemStack close = ItemKeys.SUMMARY_CLOSE.getItem();
        for (int slot : ItemKeys.SUMMARY_CLOSE.getSlot()) {
            inventory.setItem(slot, close);
        }

        ItemStack template = ItemKeys.SUMMARY_REWARD_TEMPLATE.getItem();
        if (template == null) return;

        int slot = 0;

        for (ItemStack reward : entry.items()) {
            if (slot >= inventory.getSize()) break;

            ItemStack display = reward.clone();
            display.setAmount(1);

            display.editMeta(meta -> {
                meta.setDisplayName(MessageProcessor.process(
                        template.getItemMeta().getDisplayName()
                                .replace("{reward_name}", reward.getItemMeta().getDisplayName())
                                .replace("{reward_amount}", String.valueOf(reward.getAmount()))
                                .replace("{lootbox}", entry.title())
                                .replace("{grant_type}", "Lootbox")
                ));

                meta.setLore(
                        Objects.requireNonNull(template.getItemMeta().getLore()).stream()
                                .map(line -> MessageProcessor.process(
                                        line.replace("{reward_name}", reward.getItemMeta().getDisplayName())
                                                .replace("{reward_amount}", String.valueOf(reward.getAmount()))
                                                .replace("{lootbox}", entry.title())
                                                .replace("{grant_type}", "Lootbox")
                                ))
                                .toList()
                );
            });

            inventory.setItem(slot++, display);
        }
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);

        int raw = event.getRawSlot();
        int size = inventory.getSize();

        if (raw >= size) return;

        if (ItemKeys.SUMMARY_CLOSE.getSlot().contains(raw)) {
            menuController.owner().closeInventory();
            close();
        }
    }

    @NotNull
    @Override
    public String getMenuName() {
        return MessageProcessor.process(MenuKeys.SUMMARY_MENU_TITLE.getString());
    }

    @Override
    public int getSlots() {
        return MenuKeys.SUMMARY_MENU_SIZE.getInt();
    }

    @Override
    public int getMenuTick() {
        return 0;
    }
}