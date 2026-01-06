package com.mongenscave.mclootbox.guis.impl.editor.reward;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.data.MenuController;
import com.mongenscave.mclootbox.guis.Menu;
import com.mongenscave.mclootbox.guis.impl.editor.LootboxEditorLootboxMenu;
import com.mongenscave.mclootbox.identifiers.RewardGroupType;
import com.mongenscave.mclootbox.identifiers.keys.ItemKeys;
import com.mongenscave.mclootbox.identifiers.keys.MenuKeys;
import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.model.LootboxReward;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("deprecation")
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

        ItemStack template = ItemKeys.EDITOR_REWARD_TEMPLATE.getItem();
        List<String> templateLore = null;
        String templateName = null;

        if (template != null && template.hasItemMeta()) {
            templateLore = template.getItemMeta().getLore();
            templateName = template.getItemMeta().getDisplayName();
        }

        for (LootboxReward reward : rewards) {
            ItemStack item = reward.createDisplayItem()
                    .map(ItemStack::clone)
                    .orElseGet(() -> {
                        ItemStack fallback = new ItemStack(org.bukkit.Material.BARRIER);
                        fallback.editMeta(meta -> meta.setDisplayName("&cInvalid reward"));
                        return fallback;
                    });

            List<String> finalTemplateLore = templateLore;
            String finalTemplateName = templateName;

            item.editMeta(meta -> {
                if (finalTemplateName != null) {
                    meta.setDisplayName(finalTemplateName
                            .replace("{reward_name}", Boolean.parseBoolean(reward.getName()) ? reward.getName() : reward.getMaterial())
                            .replace("{id}", reward.getId()));
                }

                if (finalTemplateLore == null || finalTemplateLore.isEmpty()) return;

                List<String> finalLore = new ArrayList<>();

                List<String> rewardCommands = reward.getCommands();
                int commandCount = rewardCommands != null ? rewardCommands.size() : 0;

                for (String line : finalTemplateLore) {
                    if ("{reward_lore}".equals(line)) {
                        List<String> rewardLore = reward.getLore();
                        if (rewardLore != null && !rewardLore.isEmpty()) {
                            for (String loreLine : rewardLore) {
                                finalLore.add("  " + loreLine);
                            }
                        } else {
                            finalLore.add("  &8(no lore)");
                        }

                        continue;
                    }

                    if (line.contains("{command}")) {
                        if (commandCount == 0) continue;

                        for (String command : rewardCommands) {
                            finalLore.add(line.replace("{command}", command));
                        }

                        continue;
                    }

                    assert reward.getCommands() != null;
                    finalLore.add(line
                            .replace("{chance}", String.valueOf(reward.getChance()))
                            .replace("{give_item}", reward.isGiveItem() ? "☑" : "☒")
                            .replace("{commands}", String.valueOf(reward.getCommands().size()))
                            .replace("{amount}", String.valueOf(reward.getAmount())));
                }

                meta.setLore(finalLore);
            });

            inventory.setItem(slot.getAndIncrement(), item);
        }
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        int raw = event.getRawSlot();
        int topSize = inventory.getSize();

        event.setCancelled(true);
        if (raw >= topSize) return;

        if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            Lootbox lootbox = McLootbox.getInstance()
                    .getLootboxManager()
                    .getLootbox(lootboxId)
                    .orElse(null);

            if (lootbox == null) return;

            RewardGroupType groupType = this.type;

            LootboxReward reward = findRewardBySlot(raw, lootbox, groupType);
            if (reward == null) return;

            McLootbox.getInstance()
                    .getLootboxManager()
                    .removeReward(lootboxId, groupType, reward.getId());

            setMenuItems();
            menuController.owner().updateInventory();
            return;
        }

        if (ItemKeys.EDITOR_REWARD_BACK.getSlots().contains(raw)) {
            new LootboxEditorLootboxMenu(menuController, lootboxId).open();
            return;
        }

        if (ItemKeys.EDITOR_REWARD_ADD.getSlots().contains(raw)) {
            LootboxEditorRewardTypeMenu.open(
                    menuController.owner(),
                    lootboxId,
                    type);
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

    @Nullable
    private LootboxReward findRewardBySlot(int slot, Lootbox lootbox, RewardGroupType type) {
        Collection<LootboxReward> rewards = type == RewardGroupType.NORMAL ? lootbox.getNormalRewards().getRewards() : lootbox.getFinalRewards().getRewards();

        int index = 0;
        for (LootboxReward reward : rewards) {
            if (index == slot) return reward;
            index++;
        }

        return null;
    }
}