package com.mongenscave.mclootbox.guis.impl.editor;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.data.MenuController;
import com.mongenscave.mclootbox.guis.Menu;
import com.mongenscave.mclootbox.guis.impl.LootboxPreviewMenu;
import com.mongenscave.mclootbox.identifiers.RewardGroupType;
import com.mongenscave.mclootbox.identifiers.keys.ItemKeys;
import com.mongenscave.mclootbox.identifiers.keys.MenuKeys;
import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.model.Lootbox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class LootboxEditorLootboxMenu extends Menu {

    private final String lootboxId;

    public LootboxEditorLootboxMenu(@NotNull MenuController menuController, @NotNull String lootboxId) {
        super(menuController);
        this.lootboxId = lootboxId;
    }

    public static void open(@NotNull Player player, @NotNull String lootboxId) {
        new LootboxEditorLootboxMenu(new MenuController(player), lootboxId).open();
    }

    @Override
    public void setMenuItems() {
        inventory.clear();

        ItemFactory.setItemsForMenu("editor-lootbox.items", inventory);

        place(ItemKeys.EDITOR_LOOTBOX_BACK);
        place(ItemKeys.EDITOR_LOOTBOX_PREVIEW);
        place(ItemKeys.EDITOR_LOOTBOX_ITEM);
        place(ItemKeys.EDITOR_LOOTBOX_VISUAL);
        place(ItemKeys.EDITOR_LOOTBOX_NORMAL_REWARDS);
        place(ItemKeys.EDITOR_LOOTBOX_FINAL_REWARDS);
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        int raw = event.getRawSlot();
        int topSize = inventory.getSize();

        event.setCancelled(true);
        if (raw >= topSize) {
            return;
        }

        if (ItemKeys.EDITOR_LOOTBOX_BACK.getSlot().contains(raw)) {
            LootboxEditorMainMenu.open(menuController.owner());
            return;
        }

        Optional<Lootbox> optional = McLootbox.getInstance().getLootboxManager().getLootbox(lootboxId);
        if (optional.isEmpty()) {
            menuController.owner().closeInventory();
            return;
        }

        Lootbox lootbox = optional.get();

        if (ItemKeys.EDITOR_LOOTBOX_PREVIEW.getSlot().contains(raw)) {
            new LootboxPreviewMenu(menuController, lootbox).open();
            return;
        }

        if (ItemKeys.EDITOR_LOOTBOX_ITEM.getSlot().contains(raw)) {
            menuController.owner().sendMessage("Coming soon.");
            return;
        }

        if (ItemKeys.EDITOR_LOOTBOX_VISUAL.getSlot().contains(raw)) {
            menuController.owner().sendMessage("Coming soon.");
            return;
        }

        if (ItemKeys.EDITOR_LOOTBOX_NORMAL_REWARDS.getSlot().contains(raw)) {
            LootboxEditorRewardListMenu.open(
                    menuController.owner(),
                    lootboxId,
                    RewardGroupType.NORMAL
            );
            return;
        }

        if (ItemKeys.EDITOR_LOOTBOX_FINAL_REWARDS.getSlot().contains(raw)) {
            LootboxEditorRewardListMenu.open(
                    menuController.owner(),
                    lootboxId,
                    RewardGroupType.FINAL
            );
        }
    }

    @NotNull
    @Override
    public String getMenuName() {
        return MenuKeys.EDITOR_LOOTBOX_MENU_TITLE.getString().replace("{id}", lootboxId);
    }

    @Override
    public int getSlots() {
        return MenuKeys.EDITOR_LOOTBOX_MENU_SIZE.getInt();
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