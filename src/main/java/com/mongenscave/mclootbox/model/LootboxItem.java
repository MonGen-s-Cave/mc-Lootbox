package com.mongenscave.mclootbox.model;

import lombok.Getter;

import java.util.List;

@Getter
public final class LootboxItem {

    private final String material;
    private final String name;
    private final List<String> lore;

    public LootboxItem(String material, String name, List<String> lore) {
        this.material = material;
        this.name = name;
        this.lore = lore;
    }
}