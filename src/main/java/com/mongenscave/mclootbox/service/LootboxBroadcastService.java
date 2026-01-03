package com.mongenscave.mclootbox.service;

import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.model.LootboxReward;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LootboxBroadcastService {

    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private static final LegacyComponentSerializer LEGACY_AMP = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

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
        String processed = MessageProcessor.process(
                translateHex(line.replace("{player}", player.getName())
                        .replace("{lootbox}", lootbox.getDisplayName()))
        );

        Component base = deserializeLegacy(processed);

        if (summary != null && line.contains("{summary}")) {
            return base.replaceText(builder -> builder
                    .matchLiteral("{summary}")
                    .replacement(summary)
            );
        }

        return base;
    }

    private static Component buildRewardLine(@NotNull String line, @NotNull LootboxReward reward, @NotNull Player player, @NotNull Lootbox lootbox, Component summary) {
        String processed = translateHex(
                line.replace("{reward_name}", reward.getName() != null ? reward.getName() : reward.getMaterial())
                        .replace("{reward_amount}", String.valueOf(reward.getAmount()))
                        .replace("{player}", player.getName())
                        .replace("{lootbox}", lootbox.getDisplayName())
        );

        processed = MessageProcessor.process(processed);

        Component base = deserializeLegacy(processed);

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
        String command = lootbox.getConfig().getString("summary.command", "/lootbox summary {token}")
                .replace("{token}", token);

        String processedText = MessageProcessor.process(translateHex(text));
        Component component = deserializeLegacy(processedText).clickEvent(ClickEvent.runCommand(command));

        if (!hover.isEmpty()) {
            String processedHover = MessageProcessor.process(translateHex(hover));
            component = component.hoverEvent(HoverEvent.showText(deserializeLegacy(processedHover)));
        }

        return component;
    }

    private static @NotNull Component deserializeLegacy(@NotNull String input) {
        if (input.indexOf('§') >= 0) {
            return LEGACY_SECTION.deserialize(input);
        }

        return LEGACY_AMP.deserialize(input);
    }

    private static @NotNull String translateHex(@NotNull String input) {
        char code = input.indexOf('§') >= 0 ? '§' : '&';

        Matcher matcher = HEX.matcher(input);
        StringBuffer buffer = new StringBuffer(input.length() + 32);

        while (matcher.find()) {
            String hex = matcher.group(1);

            StringBuilder replacement = new StringBuilder(14)
                    .append(code)
                    .append('x');

            for (int i = 0; i < 6; i++) {
                replacement.append(code).append(hex.charAt(i));
            }

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement.toString()));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}