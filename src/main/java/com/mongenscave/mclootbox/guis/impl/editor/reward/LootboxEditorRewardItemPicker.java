package com.mongenscave.mclootbox.guis.impl.editor.reward;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.data.MenuController;
import com.mongenscave.mclootbox.guis.Menu;
import com.mongenscave.mclootbox.identifiers.RewardGroupType;
import com.mongenscave.mclootbox.identifiers.keys.ItemKeys;
import com.mongenscave.mclootbox.identifiers.keys.MenuKeys;
import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.service.CommandInputService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class LootboxEditorRewardItemPicker extends Menu {

    private final String lootboxId;
    private final RewardGroupType type;
    private final boolean giveItem;
    private ItemStack selectedItem;

    public LootboxEditorRewardItemPicker(MenuController controller, String lootboxId, RewardGroupType type, boolean giveItem) {
        super(controller);
        this.lootboxId = lootboxId;
        this.type = type;
        this.giveItem = giveItem;
    }

    public static void open(Player player, String lootboxId, RewardGroupType type, boolean giveItem) {
        new LootboxEditorRewardItemPicker(new MenuController(player), lootboxId, type, giveItem).open();
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        ItemFactory.setItemsForMenu("editor-reward-item-picker.items", inventory);

        ItemStack item = ItemKeys.EDITOR_REWARD_SAVE.getItem();
        if (item == null) return;

        for (int slot : ItemKeys.EDITOR_REWARD_SAVE.getSlots()) {
            inventory.setItem(slot, item);
        }
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        int raw = event.getRawSlot();
        int topSize = inventory.getSize();

        Player player = menuController.owner();

        int itemSlot = ItemKeys.EDITOR_REWARD_ITEM_SLOT.getSlot();

        if (raw < topSize) {
            event.setCancelled(true);

            if (ItemKeys.EDITOR_REWARD_CANCEL.getSlots().contains(raw)) {
                LootboxEditorRewardListMenu.open(player, lootboxId, type);
                return;
            }

            if (ItemKeys.EDITOR_REWARD_SAVE.getSlots().contains(raw)) {
                if (selectedItem == null || selectedItem.getType().isAir()) {
                    player.sendMessage("§cYou must place an item first.");
                    return;
                }

                String rewardId = McLootbox.getInstance()
                        .getEditorService()
                        .createRewardFromItem(
                                lootboxId,
                                type,
                                selectedItem.clone(),
                                giveItem);

                player.closeInventory();

                if (!giveItem) {
                    CommandInputService.requestCommand(player, lootboxId, type, rewardId);
                }

                return;
            }

            if (raw == itemSlot && selectedItem != null) {
                player.getInventory().addItem(selectedItem.clone());
                inventory.clear(itemSlot);
                selectedItem = null;
            }

            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        if (selectedItem == null) {
            selectedItem = clicked.clone();
            inventory.setItem(itemSlot, selectedItem);
        }
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public String getMenuName() {
        return MenuKeys.EDITOR_REWARD_ITEM_PICKER_TITLE.getString();
    }

    @Override
    public int getSlots() {
        return MenuKeys.EDITOR_REWARD_ITEM_PICKER_SIZE.getInt();
    }

    @Override
    public int getMenuTick() {
        return 0;
    }
}