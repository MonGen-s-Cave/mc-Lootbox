package com.mongenscave.mclootbox.model;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public record LootboxSummary(
        UUID owner,
        String title,
        List<ItemStack> items,
        long createdAt
) {}