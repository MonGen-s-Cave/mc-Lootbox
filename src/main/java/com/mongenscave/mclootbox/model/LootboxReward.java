package com.mongenscave.mclootbox.model;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;

import java.util.List;

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
}