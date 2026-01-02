package com.mongenscave.mclootbox.model;

import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;

@Getter
public final class Lootbox {

    private final String id;
    private final YamlDocument config;

    private final String displayName;
    private final LootboxItem item;
    private final LootboxVisual visual;

    private final RewardGroup normalRewards;
    private final RewardGroup finalRewards;

    public Lootbox(String id, YamlDocument config, String displayName, LootboxItem item, LootboxVisual visual, RewardGroup normalRewards, RewardGroup finalRewards) {
        this.id = id;
        this.config = config;
        this.displayName = displayName;
        this.item = item;
        this.visual = visual;
        this.normalRewards = normalRewards;
        this.finalRewards = finalRewards;
    }

    public RewardGroup getRewards() {
        return normalRewards;
    }
}