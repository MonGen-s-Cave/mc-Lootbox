package com.mongenscave.mclootbox.identifiers;

import com.mongenscave.mclootbox.McLootbox;
import org.bukkit.NamespacedKey;

public final class LootboxKeys {
    public static final NamespacedKey LOOTBOX_ID = new NamespacedKey(McLootbox.getInstance(), "lootbox_id");

    private LootboxKeys() {
    }
}