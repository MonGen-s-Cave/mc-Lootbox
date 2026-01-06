package com.mongenscave.mclootbox.identifiers.keys;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.item.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum ItemKeys {
    SUMMARY_REWARD_TEMPLATE("summary-menu.reward-template"),
    SUMMARY_CLOSE("summary-menu.items.close"),

    PREVIEW_REWARD_TEMPLATE("preview-menu.reward-template"),
    PREVIEW_CLOSE("preview-menu.items.close"),

    EDITOR_MAIN_LOOTBOX_TEMPLATE("editor-main.lootbox-template"),
    EDITOR_MAIN_CLOSE("editor-main.items.close"),
    EDITOR_MAIN_CREATE("editor-main.items.create"),
    EDITOR_MAIN_RELOAD("editor-main.items.reload"),
    EDITOR_MAIN_NEXT("editor-main.items.next"),
    EDITOR_MAIN_PREV("editor-main.items.prev"),

    EDITOR_LOOTBOX_BACK("editor-lootbox.items.back"),
    EDITOR_LOOTBOX_PREVIEW("editor-lootbox.items.preview"),
    EDITOR_LOOTBOX_ITEM("editor-lootbox.items.item"),
    EDITOR_LOOTBOX_VISUAL("editor-lootbox.items.visual"),
    EDITOR_LOOTBOX_NORMAL_REWARDS("editor-lootbox.items.normal-rewards"),
    EDITOR_LOOTBOX_FINAL_REWARDS("editor-lootbox.items.final-rewards"),

    EDITOR_REWARD_TEMPLATE("editor-rewards.reward-template"),
    EDITOR_REWARD_ADD("editor-rewards.items.add"),
    EDITOR_REWARD_BACK("editor-rewards.items.back"),

    EDITOR_REWARD_TYPE_ITEM("editor-reward-type.items.item"),
    EDITOR_REWARD_TYPE_COMMAND("editor-reward-type.items.command"),
    EDITOR_REWARD_TYPE_BACK("editor-reward-type.items.back"),

    EDITOR_REWARD_SAVE("editor-reward-item-picker.items.save"),
    EDITOR_REWARD_CANCEL("editor-reward-item-picker.items.cancel"),
    EDITOR_REWARD_ITEM_SLOT("editor-reward-item-picker.picked-item");

    private final String path;

    ItemKeys(@NotNull final String path) {
        this.path = path;
    }

    public int getSlot() {
        return McLootbox.getInstance().getGuis().getInt(path + ".slot");
    }

    public List<Integer> getSlots() {
        return McLootbox.getInstance().getGuis().getIntList(path + ".slot");
    }

    public ItemStack getItem() {
        return ItemFactory.createItemFromString(path, McLootbox.getInstance().getGuis()).orElse(null);
    }
}