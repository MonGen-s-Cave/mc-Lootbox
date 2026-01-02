package com.mongenscave.mclootbox.manager;

import com.mongenscave.mclootbox.model.LootboxReward;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class LootboxRewardManager {

    private LootboxRewardManager() {
    }

    @NotNull
    public static List<LootboxReward> roll(Collection<LootboxReward> rewards, int prizeSize) {
        List<LootboxReward> pool = new ArrayList<>(rewards);
        List<LootboxReward> result = new ArrayList<>();

        if (pool.isEmpty() || prizeSize <= 0) return result;

        for (int i = 0; i < prizeSize; i++) {
            LootboxReward reward = rollSingle(pool);
            if (reward != null) result.add(reward);
        }

        return result;
    }

    @Nullable
    private static LootboxReward rollSingle(@NotNull List<LootboxReward> rewards) {
        double totalChance = 0.0;

        for (LootboxReward reward : rewards) {
            totalChance += reward.getChance();
        }

        if (totalChance <= 0.0) return null;

        double roll = ThreadLocalRandom.current().nextDouble(totalChance);
        double current = 0.0;

        for (LootboxReward reward : rewards) {
            current += reward.getChance();
            if (roll <= current) return reward;
        }

        return null;
    }
}