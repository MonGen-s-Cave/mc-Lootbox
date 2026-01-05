package com.mongenscave.mclootbox.guis.impl.editor;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.data.MenuController;
import com.mongenscave.mclootbox.guis.Menu;
import com.mongenscave.mclootbox.identifiers.RewardGroupType;
import com.mongenscave.mclootbox.identifiers.keys.ItemKeys;
import com.mongenscave.mclootbox.identifiers.keys.MenuKeys;
import com.mongenscave.mclootbox.item.ItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class LootboxEditorRewardItemPicker extends Menu {

    private final String lootboxId;
    private final RewardGroupType type;
    private final boolean giveItem;

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
        place(ItemKeys.EDITOR_REWARD_SAVE);
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        int raw = event.getRawSlot();
        int topSize = inventory.getSize();

        if (raw < topSize) {
            event.setCancelled(true);

            if (ItemKeys.EDITOR_REWARD_SAVE.getSlot().contains(raw)) {
                ItemStack cursor = menuController.owner()
                        .getInventory()
                        .getItemInMainHand();

                if (cursor == null || cursor.getType().isAir()) return;

                McLootbox.getInstance()
                        .getEditorService()
                        .createRewardFromItem(
                                lootboxId,
                                type,
                                cursor.clone(),
                                giveItem
                        );

                menuController.owner().closeInventory();
            }
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

    private void place(@NotNull ItemKeys key) {
        ItemStack item = key.getItem();
        if (item == null) return;

        for (int slot : key.getSlot()) {
            inventory.setItem(slot, item);
        }
    }
}