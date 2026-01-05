package com.mongenscave.mclootbox.guis.impl.editor;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.data.MenuController;
import com.mongenscave.mclootbox.guis.Menu;
import com.mongenscave.mclootbox.identifiers.RewardGroupType;
import com.mongenscave.mclootbox.identifiers.keys.ItemKeys;
import com.mongenscave.mclootbox.identifiers.keys.MenuKeys;
import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.model.LootboxReward;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public final class LootboxEditorRewardListMenu extends Menu {

    private final String lootboxId;
    private final RewardGroupType type;

    public LootboxEditorRewardListMenu(@NotNull MenuController controller, @NotNull String lootboxId, @NotNull RewardGroupType type) {
        super(controller);
        this.lootboxId = lootboxId;
        this.type = type;
    }

    public static void open(Player player, String lootboxId, RewardGroupType type) {
        new LootboxEditorRewardListMenu(new MenuController(player), lootboxId, type).open();
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        ItemFactory.setItemsForMenu("editor-rewards.items", inventory);

        Lootbox lootbox = McLootbox.getInstance()
                .getLootboxManager()
                .getLootbox(lootboxId)
                .orElse(null);

        if (lootbox == null) return;

        Collection<LootboxReward> rewards = type == RewardGroupType.NORMAL
                ? lootbox.getNormalRewards().getRewards()
                : lootbox.getFinalRewards().getRewards();

        AtomicInteger slot = new AtomicInteger();
        for (LootboxReward reward : rewards) {
            reward.createDisplayItem().ifPresent(item -> {
                inventory.setItem(slot.getAndIncrement(), item);
            });
        }
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        int raw = event.getRawSlot();
        int topSize = inventory.getSize();

        event.setCancelled(true);
        if (raw >= topSize) return;

        if (ItemKeys.EDITOR_REWARD_ADD.getSlot().contains(raw)) {
            LootboxEditorRewardTypeMenu.open(
                    menuController.owner(),
                    lootboxId,
                    type
            );
        }
    }

    @NotNull
    @Override
    public String getMenuName() {
        return MenuKeys.EDITOR_REWARD_LIST_TITLE
                .getString()
                .replace("{type}", type.name());
    }

    @Override
    public int getSlots() {
        return MenuKeys.EDITOR_REWARD_LIST_SIZE.getInt();
    }

    @Override
    public int getMenuTick() {
        return 0;
    }
}