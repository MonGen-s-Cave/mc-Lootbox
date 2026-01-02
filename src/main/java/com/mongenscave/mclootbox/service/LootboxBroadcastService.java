package com.mongenscave.mclootbox.service;

import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.model.LootboxReward;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class LootboxBroadcastService {

    private LootboxBroadcastService() {
    }

    public static void broadcast(@NotNull Player player, @NotNull Lootbox lootbox, @NotNull List<LootboxReward> normalRewards, @NotNull List<LootboxReward> finalRewards, String token) {
        List<String> template = lootbox.getConfig().getStringList("final-broadcast");
        if (template.isEmpty()) return;

        Component summaryComponent = buildSummaryComponent(lootbox, token);
        List<Component> output = new ArrayList<>();

        for (String line : template) {
            if (line.contains("{normal-reward}")) {
                for (LootboxReward reward : normalRewards) {
                    output.add(buildRewardLine(
                            line.replace("{normal-reward}", ""),
                            reward,
                            player,
                            lootbox,
                            summaryComponent
                    ));
                }
                continue;
            }

            if (line.contains("{final-reward}")) {
                for (LootboxReward reward : finalRewards) {
                    output.add(buildRewardLine(
                            line.replace("{final-reward}", ""),
                            reward,
                            player,
                            lootbox,
                            summaryComponent
                    ));
                }
                continue;
            }

            output.add(buildBaseLine(line, player, lootbox, summaryComponent));
        }

        for (Component component : output) {
            Bukkit.broadcast(component);
        }
    }

    private static Component buildBaseLine(@NotNull String line, @NotNull Player player, @NotNull Lootbox lootbox, Component summary) {
        Component base = Component.text(
                MessageProcessor.process(
                        line.replace("{player}", player.getName())
                                .replace("{lootbox}", lootbox.getDisplayName())
                )
        );

        if (summary != null && line.contains("{summary}")) {
            return base.replaceText(builder -> builder
                    .matchLiteral("{summary}")
                    .replacement(summary)
            );
        }

        return base;
    }

    private static Component buildRewardLine(@NotNull String line, @NotNull LootboxReward reward, @NotNull Player player, @NotNull Lootbox lootbox, Component summary) {
        String processed = MessageProcessor.process(
                line.replace("{reward_name}", reward.getName() != null ? reward.getName() : reward.getMaterial())
                        .replace("{reward_amount}", String.valueOf(reward.getAmount()))
                        .replace("{player}", player.getName())
                        .replace("{lootbox}", lootbox.getDisplayName())
        );

        Component base = Component.text(processed);

        if (summary != null && line.contains("{summary}")) {
            return base.replaceText(builder -> builder
                    .matchLiteral("{summary}")
                    .replacement(summary)
            );
        }

        return base;
    }

    @Nullable
    private static Component buildSummaryComponent(@NotNull Lootbox lootbox, String token) {
        if (!lootbox.getConfig().getBoolean("summary.enabled", false)) return null;

        String text = lootbox.getConfig().getString("summary.text", "[Summary]");
        String hover = lootbox.getConfig().getString("summary.hover", "");
        String command = lootbox.getConfig().getString("summary.command", "/lootbox summary {token}").replace("{token}", token);

        Component component = Component.text(MessageProcessor.process(text)).clickEvent(ClickEvent.runCommand(command));
        if (!hover.isEmpty()) {
            component = component.hoverEvent(
                    HoverEvent.showText(Component.text(MessageProcessor.process(hover)))
            );
        }

        return component;
    }
}