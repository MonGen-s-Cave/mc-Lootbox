package com.mongenscave.mclootbox.identifiers.keys;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.item.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum ItemKeys {
    SUMMARY_REWARD_TEMPLATE("summary-menu.reward-template"),
    SUMMARY_CLOSE("summary-menu.items.close");

    private final String path;

    ItemKeys(@NotNull final String path) {
        this.path = path;
    }

    public List<Integer> getSlot() {
        return McLootbox.getInstance().getGuis().getIntList(path + ".slot");
    }

    public ItemStack getItem() {
        return ItemFactory.createItemFromString(path, McLootbox.getInstance().getGuis()).orElse(null);
    }
}