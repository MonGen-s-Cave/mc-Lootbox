package com.mongenscave.mclootbox.service;

import com.mongenscave.mclootbox.model.LootboxSummary;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class LootboxSummaryService {

    private static final Map<String, LootboxSummary> CACHE = new ConcurrentHashMap<>();

    private static final long TTL_MILLIS = 5 * 60 * 1000;

    private LootboxSummaryService() {}

    @NotNull
    public static String store(@NotNull Player player, String title, List<ItemStack> items) {
        String token = UUID.randomUUID().toString().substring(0, 8);

        CACHE.put(token, new LootboxSummary(
                player.getUniqueId(),
                title,
                items,
                System.currentTimeMillis()
        ));

        return token;
    }

    @NotNull
    public static Optional<LootboxSummary> get(String token) {
        return Optional.ofNullable(CACHE.get(token));
    }

    public static void cleanupExpired() {
        long now = System.currentTimeMillis();
        CACHE.entrySet().removeIf(e -> now - e.getValue().createdAt() > TTL_MILLIS);
    }
}