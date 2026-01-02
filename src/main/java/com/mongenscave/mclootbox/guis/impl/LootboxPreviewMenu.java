package com.mongenscave.mclootbox.guis.impl;

import com.mongenscave.mclootbox.data.MenuController;
import com.mongenscave.mclootbox.guis.Menu;
import com.mongenscave.mclootbox.identifiers.keys.ItemKeys;
import com.mongenscave.mclootbox.identifiers.keys.MenuKeys;
import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.model.LootboxReward;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public final class LootboxPreviewMenu extends Menu {

    private final Lootbox lootbox;

    public LootboxPreviewMenu(@NotNull MenuController controller, @NotNull Lootbox lootbox) {
        super(controller);
        this.lootbox = lootbox;
    }

    @Override
    public void setMenuItems() {
        inventory.clear();

        ItemFactory.setItemsForMenu("preview-menu.items", inventory);

        ItemStack close = ItemKeys.PREVIEW_CLOSE.getItem();
        for (int slot : ItemKeys.PREVIEW_CLOSE.getSlot()) {
            inventory.setItem(slot, close);
        }

        ItemStack template = ItemKeys.PREVIEW_REWARD_TEMPLATE.getItem();
        if (template == null) return;

        int slot = 0;

        List<LootboxReward> ordered = new ArrayList<>();
        ordered.addAll(lootbox.getNormalRewards().getRewards());
        ordered.addAll(lootbox.getFinalRewards().getRewards());

        for (LootboxReward reward : ordered) {
            if (slot >= inventory.getSize()) break;

            ItemStack base = reward.createDisplayItem().orElse(null);
            if (base == null) continue;

            base.setAmount(1);

            String rewardName = base.hasItemMeta() && base.getItemMeta().hasDisplayName()
                    ? base.getItemMeta().getDisplayName()
                    : base.getType().name();

            base.editMeta(meta -> {
                meta.setDisplayName(MessageProcessor.process(
                        template.getItemMeta().getDisplayName()
                                .replace("{reward_name}", rewardName)
                                .replace("{reward_amount}", String.valueOf(reward.getAmount()))
                                .replace("{lootbox}", lootbox.getDisplayName())
                                .replace("{chance}", formatChance(reward.getChance()))
                ));

                List<String> finalLore = new ArrayList<>();

                List<String> templateLore = template.getItemMeta().getLore();
                List<String> rewardLore = base.getItemMeta().getLore();

                if (templateLore != null) {
                    for (String line : templateLore) {

                        if (line.contains("{reward_lore}") && rewardLore != null) {
                            for (String rl : rewardLore) {
                                finalLore.add(MessageProcessor.process(rl));
                            }
                            continue;
                        }

                        finalLore.add(MessageProcessor.process(line.replace("{reward_name}", rewardName)
                                .replace("{reward_amount}", String.valueOf(reward.getAmount()))
                                .replace("{lootbox}", lootbox.getDisplayName())
                                .replace("{chance}", formatChance(reward.getChance()))
                        ));
                    }
                }

                meta.setLore(finalLore);
            });

            inventory.setItem(slot++, base);
        }
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);

        int raw = event.getRawSlot();
        if (raw >= inventory.getSize()) return;

        if (ItemKeys.PREVIEW_CLOSE.getSlot().contains(raw)) {
            menuController.owner().closeInventory();
            close();
        }
    }

    @NotNull
    @Override
    public String getMenuName() {
        return MessageProcessor.process(MenuKeys.PREVIEW_MENU_TITLE.getString());
    }

    @Override
    public int getSlots() {
        return MenuKeys.PREVIEW_MENU_SIZE.getInt();
    }

    @Override
    public int getMenuTick() {
        return 0;
    }

    @NotNull
    @Contract(pure = true)
    private String formatChance(double chance) {
        return String.format("%.2f", chance);
    }
}