package com.mongenscave.mclootbox.manager;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.identifiers.LootboxKeys;
import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.model.*;
import com.mongenscave.mclootbox.utils.LoggerUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class LootboxManager {

    private final Map<String, Lootbox> lootboxes = new ConcurrentHashMap<>();
    private final File folder;

    public LootboxManager() {
        this.folder = new File(McLootbox.getInstance().getDataFolder(), "lootboxes");
        load();
    }

    public void load() {
        lootboxes.clear();

        boolean firstInit = false;

        if (!folder.exists()) {
            folder.mkdirs();
            firstInit = true;
        }

        if (firstInit) {
            try {
                File target = new File(folder, "default.yml");
                if (target.exists()) return;

                McLootbox.getInstance().saveResource("lootboxes/default.yml", false);
            } catch (Exception exception) {
                LoggerUtils.error("Failed to create initial default lootbox: " + exception.getMessage());
            }
        }

        File[] files = folder.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                YamlDocument config = YamlDocument.create(file);
                String id = file.getName().replace(".yml", "").toLowerCase();

                Lootbox lootbox = parseLootbox(id, config);
                lootboxes.put(id, lootbox);

            } catch (Exception exception) {
                LoggerUtils.error(exception.getMessage());
            }
        }
    }

    @NotNull
    private Lootbox parseLootbox(String id, @NotNull YamlDocument config) {
        String displayName = config.getString("display-name");

        LootboxItem item = new LootboxItem(
                config.getString("item.material"),
                config.getString("item.name"),
                config.getStringList("item.lore"));

        LootboxVisual visual = new LootboxVisual(
                config.getString("visual.animation"),
                config.getStringList("visual.hologram"));

        RewardGroup normal = parseRewardGroup(
                config,
                "reward-settings.normal-rewards",
                config.getInt("reward-settings.prize-size.normal"));

        RewardGroup fin = parseRewardGroup(
                config,
                "reward-settings.final-rewards",
                config.getInt("reward-settings.prize-size.final"));

        return new Lootbox(id, config, displayName, item, visual, normal, fin);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    private RewardGroup parseRewardGroup(@NotNull YamlDocument config, String path, int prizeSize) {
        Section section = config.getSection(path);
        Map<String, LootboxReward> rewards = new HashMap<>();

        if (section != null) {
            for (String key : section.getRoutesAsStrings(false)) {
                Section r = section.getSection(key);
                if (r == null) continue;

                Section itemSection = r.getSection("item");
                String itemPath = "lootboxes." + (config.getFile() != null ? config.getFile().getName().replace(".yml", "") : null) + "." + path + "." + key + ".item";

                LootboxReward reward = new LootboxReward(
                        key,
                        r.getDouble("chance"),
                        r.getBoolean("item.give-item"),
                        itemSection,
                        itemPath,
                        r.getString("item.material"),
                        r.getString("item.name"),
                        r.getStringList("item.lore"),
                        r.getInt("item.amount", 1),
                        r.getStringList("commands"));

                rewards.put(key, reward);
            }
        }

        return new RewardGroup(prizeSize, rewards);
    }

    @NotNull
    public Optional<Lootbox> getLootbox(@NotNull String id) {
        return Optional.ofNullable(lootboxes.get(id.toLowerCase()));
    }

    public Optional<ItemStack> createLootboxItem(@NotNull Lootbox lootbox, int amount) {
        Section itemSection = lootbox.getConfig().getSection("item");
        if (itemSection == null) return Optional.empty();

        Optional<ItemStack> optional = ItemFactory.buildItem(itemSection, "lootboxes." + lootbox.getId() + ".item");
        if (optional.isEmpty()) return Optional.empty();

        ItemStack stack = optional.get();

        stack.setAmount(Math.max(1, amount));
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(LootboxKeys.LOOTBOX_ID, PersistentDataType.STRING, lootbox.getId()));

        return Optional.of(stack);
    }
}