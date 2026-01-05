package com.mongenscave.mclootbox.guis.impl.editor;

import com.mongenscave.mclootbox.data.MenuController;
import com.mongenscave.mclootbox.guis.Menu;
import com.mongenscave.mclootbox.identifiers.RewardGroupType;
import com.mongenscave.mclootbox.identifiers.keys.ItemKeys;
import com.mongenscave.mclootbox.identifiers.keys.MenuKeys;
import com.mongenscave.mclootbox.item.ItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class LootboxEditorRewardTypeMenu extends Menu {

    private final String lootboxId;
    private final RewardGroupType type;

    public LootboxEditorRewardTypeMenu(MenuController controller, String lootboxId, RewardGroupType type) {
        super(controller);
        this.lootboxId = lootboxId;
        this.type = type;
    }

    public static void open(Player player, String lootboxId, RewardGroupType type) {
        new LootboxEditorRewardTypeMenu(new MenuController(player), lootboxId, type).open();
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        ItemFactory.setItemsForMenu("editor-reward-type.items", inventory);

        place(ItemKeys.EDITOR_REWARD_TYPE_ITEM);
        place(ItemKeys.EDITOR_REWARD_TYPE_COMMAND);
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        int raw = event.getRawSlot();
        event.setCancelled(true);

        if (ItemKeys.EDITOR_REWARD_TYPE_ITEM.getSlot().contains(raw)) {
            LootboxEditorRewardItemPicker.open(
                    menuController.owner(),
                    lootboxId,
                    type,
                    true
            );
            return;
        }

        if (ItemKeys.EDITOR_REWARD_TYPE_COMMAND.getSlot().contains(raw)) {
            LootboxEditorRewardItemPicker.open(
                    menuController.owner(),
                    lootboxId,
                    type,
                    false
            );
        }
    }

    @NotNull
    @Override
    public String getMenuName() {
        return MenuKeys.EDITOR_REWARD_TYPE_TITLE.getString();
    }

    @Override
    public int getSlots() {
        return MenuKeys.EDITOR_REWARD_TYPE_SIZE.getInt();
    }

    @Override
    public int getMenuTick() {
        return 0;
    }

    private void place(@NotNull ItemKeys key) {
        ItemStack item = key.getItem();
        if (item == null) return;

        for (int slot : key.getSlot()) {
            inventory.setItem(slot, item);
        }
    }
}