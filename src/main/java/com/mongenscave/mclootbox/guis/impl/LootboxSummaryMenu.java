package com.mongenscave.mclootbox.guis.impl;

import com.mongenscave.mclootbox.data.MenuController;
import com.mongenscave.mclootbox.guis.Menu;
import com.mongenscave.mclootbox.identifiers.keys.ItemKeys;
import com.mongenscave.mclootbox.identifiers.keys.MenuKeys;
import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.model.LootboxSummary;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

        ItemFactory.setItemsForMenu("summary-menu.items", inventory);

        ItemStack close = ItemKeys.SUMMARY_CLOSE.getItem();
        for (int slot : ItemKeys.SUMMARY_CLOSE.getSlots()) {
            inventory.setItem(slot, close);
        }

        ItemStack template = ItemKeys.SUMMARY_REWARD_TEMPLATE.getItem();
        if (template == null) return;

        int slot = 0;

        for (ItemStack reward : entry.items()) {
            if (slot >= inventory.getSize()) break;

            String rewardName = reward.hasItemMeta() && reward.getItemMeta().hasDisplayName()
                    ? reward.getItemMeta().getDisplayName()
                    : reward.getType().name();

            ItemStack display = reward.clone();
            display.setAmount(1);

            display.editMeta(meta -> {

                meta.setDisplayName(MessageProcessor.process(
                        template.getItemMeta().getDisplayName()
                                .replace("{reward_name}", rewardName)
                                .replace("{reward_amount}", String.valueOf(reward.getAmount()))
                                .replace("{lootbox}", entry.title())
                                .replace("{grant_type}", "Lootbox")
                ));

                List<String> finalLore = new ArrayList<>();

                List<String> templateLore = template.getItemMeta().getLore();
                List<String> rewardLore = reward.hasItemMeta()
                        ? reward.getItemMeta().getLore()
                        : null;

                if (templateLore != null) {
                    for (String line : templateLore) {
                        if (line.contains("{reward_lore}") && rewardLore != null) {
                            for (String rewardLine : rewardLore) {
                                finalLore.add(MessageProcessor.process(rewardLine
                                        .replace("{lootbox}", entry.title()))
                                );
                            }
                            continue;
                        }

                        finalLore.add(MessageProcessor.process(line.replace("{reward_name}", rewardName)
                                .replace("{reward_amount}", String.valueOf(reward.getAmount()))
                                .replace("{lootbox}", entry.title())
                                .replace("{grant_type}", "Lootbox")
                        ));
                    }
                }

                meta.setLore(finalLore);
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

        if (ItemKeys.SUMMARY_CLOSE.getSlots().contains(raw)) {
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