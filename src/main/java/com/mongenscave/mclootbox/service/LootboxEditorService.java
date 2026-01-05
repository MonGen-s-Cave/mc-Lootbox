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

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class LootboxEditorService {

    @Getter private static final LootboxEditorService instance = new LootboxEditorService();
    private final LootboxEditorCreateHandler createHandler = new LootboxEditorCreateHandler();

    private final LootboxManager lootboxManager = McLootbox.getInstance().getLootboxManager();

    public LootboxEditorService() {
    }

    public void createRewardFromItem(String lootboxId, RewardGroupType type, ItemStack item, boolean giveItem) {
        CompletableFuture.runAsync(() -> {
            Lootbox lootbox = lootboxManager.getLootbox(lootboxId).orElse(null);
            if (lootbox == null) return;

            YamlDocument config = lootbox.getConfig();

            String base = "reward-settings." + (type == RewardGroupType.NORMAL ? "normal-rewards" : "final-rewards");
            String id = UUID.randomUUID().toString().substring(0, 8);
            String path = base + "." + id;

            config.set(path + ".chance", 1.0);
            config.set(path + ".item.give-item", giveItem);

            ItemFactory.serializeItem(item, config, path + ".item");

            config.set(path + ".commands", new ArrayList<>());

            try {
                config.save();
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