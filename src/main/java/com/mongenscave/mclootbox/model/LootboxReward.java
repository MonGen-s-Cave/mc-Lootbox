package com.mongenscave.mclootbox.model;

import com.mongenscave.mclootbox.item.ItemFactory;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@Getter
public final class LootboxReward {

    private final String id;
    private final double chance;

    private final boolean giveItem;
    private final Section itemSection;
    private final String itemPath;

    private final String material;
    private final String name;
    private final List<String> lore;
    private final int amount;

    private final List<String> commands;

    public LootboxReward(String id, double chance, boolean giveItem, Section itemSection, String itemPath, String material, String name, List<String> lore, int amount, List<String> commands) {
        this.id = id;
        this.chance = chance;
        this.giveItem = giveItem;
        this.itemSection = itemSection;
        this.itemPath = itemPath;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        this.commands = commands;
    }

    public Optional<ItemStack> createDisplayItem() {
        if (itemSection == null) return Optional.empty();
        return ItemFactory.buildItem(itemSection, itemPath);
    }

    public Optional<ItemStack> createGivenItem() {
        if (!giveItem || itemSection == null) return Optional.empty();
        return ItemFactory.buildItem(itemSection, itemPath);
    }
}