package com.mongenscave.mclootbox.animation;

import com.mongenscave.mclootbox.hologram.LootboxHologram;
import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.model.LootboxReward;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record AnimationContext(
        Player player,
        Lootbox lootbox,
        ItemStack displayItem,
        List<LootboxReward> normalRewards,
        List<LootboxReward> finalRewards,
        LootboxHologram hologram) {}