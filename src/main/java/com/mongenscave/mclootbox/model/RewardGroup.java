package com.mongenscave.mclootbox.model;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

@Getter
public final class RewardGroup {

    private final int prizeSize;
    private final Map<String, LootboxReward> rewards;

    public RewardGroup(int prizeSize, Map<String, LootboxReward> rewards) {
        this.prizeSize = prizeSize;
        this.rewards = rewards;
    }

    @NotNull
    @Contract(pure = true)
    public Collection<LootboxReward> getRewards() {
        return rewards.values();
    }
}