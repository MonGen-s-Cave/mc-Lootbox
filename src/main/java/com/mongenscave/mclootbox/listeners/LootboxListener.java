package com.mongenscave.mclootbox.listeners;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.animation.AnimationContext;
import com.mongenscave.mclootbox.animation.AnimationController;
import com.mongenscave.mclootbox.animation.impl.DefaultLootboxAnimation;
import com.mongenscave.mclootbox.hologram.LootboxHologram;
import com.mongenscave.mclootbox.hologram.LootboxTextHologram;
import com.mongenscave.mclootbox.identifiers.LootboxKeys;
import com.mongenscave.mclootbox.manager.LootboxRewardManager;
import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.model.LootboxReward;
import com.mongenscave.mclootbox.model.RewardGroup;
import com.mongenscave.mclootbox.utils.LoggerUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class LootboxListener implements Listener {

    private final McLootbox plugin = McLootbox.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String lootboxId = pdc.get(LootboxKeys.LOOTBOX_ID, PersistentDataType.STRING);
        if (lootboxId == null) return;

        event.setCancelled(true);

        if (!AnimationController.tryStart(player.getUniqueId())) return;

        Lootbox lootbox = plugin.getLootboxManager()
                .getLootbox(lootboxId)
                .orElse(null);

        if (lootbox == null) {
            AnimationController.end(player.getUniqueId());
            return;
        }

        ItemStack display = item.clone();
        display.setAmount(1);

        consumeOne(player, event.getHand());

        CompletableFuture
                .supplyAsync(() -> rollRewards(lootbox))
                .thenAccept(result ->
                        McLootbox.getScheduler().runTask(() ->
                                startAnimation(player, lootbox, display, result)
                        )
                )
                .exceptionally(throwable -> {
                    AnimationController.end(player.getUniqueId());
                    LoggerUtils.error(throwable.getMessage());
                    return null;
                });
    }

    private RollResult rollRewards(@NotNull Lootbox lootbox) {

        List<LootboxReward> normal = new ArrayList<>();
        List<LootboxReward> fin = new ArrayList<>();

        RewardGroup normalGroup = lootbox.getNormalRewards();
        RewardGroup finalGroup = lootbox.getFinalRewards();

        if (normalGroup != null && normalGroup.getPrizeSize() > 0) {
            normal.addAll(
                    LootboxRewardManager.roll(
                            normalGroup.getRewards(),
                            normalGroup.getPrizeSize()
                    )
            );
        }

        if (finalGroup != null && finalGroup.getPrizeSize() > 0) {
            fin.addAll(
                    LootboxRewardManager.roll(
                            finalGroup.getRewards(),
                            finalGroup.getPrizeSize()
                    )
            );
        }

        return new RollResult(normal, fin);
    }

    private void startAnimation(@NotNull Player player, @NotNull Lootbox lootbox, @NotNull ItemStack display, @NotNull RollResult result) {
        if (!player.isOnline()) {
            AnimationController.end(player.getUniqueId());
            return;
        }

        Location origin = player.getEyeLocation()
                .add(player.getLocation().getDirection().normalize().multiply(2.8))
                .add(0, -0.35, 0);

        LootboxHologram hologram = new LootboxTextHologram(origin, player, lootbox);

        AnimationContext context = new AnimationContext(
                player,
                lootbox,
                display,
                result.normal(),
                result.finalRewards(),
                hologram
        );

        new DefaultLootboxAnimation().start(context);
    }

    private void consumeOne(@NotNull Player player, EquipmentSlot hand) {
        ItemStack stack = player.getInventory().getItem(hand);

        int amount = stack.getAmount();
        if (amount <= 1) {
            player.getInventory().setItem(hand, null);
        } else {
            stack.setAmount(amount - 1);
        }
    }

    private record RollResult(
            List<LootboxReward> normal,
            List<LootboxReward> finalRewards
    ) {}
}