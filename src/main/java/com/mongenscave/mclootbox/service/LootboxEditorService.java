package com.mongenscave.mclootbox.service;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.handler.LootboxEditorCreateHandler;
import com.mongenscave.mclootbox.identifiers.RewardGroupType;
import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.manager.LootboxManager;
import com.mongenscave.mclootbox.model.Lootbox;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class LootboxEditorService {

    @Getter private static final LootboxEditorService instance = new LootboxEditorService();
    private final LootboxEditorCreateHandler createHandler = new LootboxEditorCreateHandler();

    private final LootboxManager lootboxManager = McLootbox.getInstance().getLootboxManager();

    public LootboxEditorService() {
    }

    @NotNull
    public String createRewardFromItem(String lootboxId, RewardGroupType type, ItemStack item, boolean giveItem) {
        String rewardId = UUID.randomUUID().toString().substring(0, 8);

        CompletableFuture.runAsync(() -> {
            Lootbox lootbox = lootboxManager.getLootbox(lootboxId).orElse(null);
            if (lootbox == null) return;

            String base = "reward-settings."
                    + (type == RewardGroupType.NORMAL ? "normal-rewards" : "final-rewards")
                    + "." + rewardId;

            YamlDocument config = lootbox.getConfig();

            config.set(base + ".chance", 1.0);
            config.set(base + ".item.give-item", giveItem);

            try {
                ItemFactory.serializeItem(item, config, base + ".item");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            config.set(base + ".commands", new ArrayList<>());

            try {
                config.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            lootboxManager.reload();
        }, McLootbox.getInstance().getIoExecutor());

        return rewardId;
    }

    public void addCommandToReward(String lootboxId, RewardGroupType type, String rewardId, String command) {
        CompletableFuture.runAsync(() -> {
            Lootbox lootbox = lootboxManager.getLootbox(lootboxId).orElse(null);
            if (lootbox == null) return;

            String base = "reward-settings."
                    + (type == RewardGroupType.NORMAL ? "normal-rewards" : "final-rewards")
                    + "." + rewardId;

            List<String> commands = lootbox.getConfig().getStringList(base + ".commands");
            commands.add(command);

            lootbox.getConfig().set(base + ".commands", commands);
            try {
                lootbox.getConfig().save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            lootboxManager.reload();
        }, McLootbox.getInstance().getIoExecutor());
    }

    public LootboxEditorCreateHandler createHandler() {
        return createHandler;
    }
}