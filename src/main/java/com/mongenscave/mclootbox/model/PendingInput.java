package com.mongenscave.mclootbox.model;

import com.mongenscave.mclootbox.identifiers.RewardGroupType;

public record PendingInput(String lootboxId, RewardGroupType type, String rewardId, long expiresAt) {
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}