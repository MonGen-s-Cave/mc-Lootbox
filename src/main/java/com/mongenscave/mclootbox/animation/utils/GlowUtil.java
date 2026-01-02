package com.mongenscave.mclootbox.animation.utils;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public final class GlowUtil {
    private static final Scoreboard SCOREBOARD = Bukkit.getScoreboardManager().getMainScoreboard();

    private GlowUtil() {}

    public static void applyGlow(Entity entity, @NotNull NamedTextColor color) {
        String teamName = "glow_" + color.examinableName().toLowerCase();

        Team team = SCOREBOARD.getTeam(teamName);
        if (team == null) {
            team = SCOREBOARD.registerNewTeam(teamName);
            team.color(color);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }

        team.addEntry(entity.getUniqueId().toString());
        entity.setGlowing(true);
    }

    public static void clearGlow(@NotNull Entity entity) {
        String entry = entity.getUniqueId().toString();

        for (Team team : SCOREBOARD.getTeams()) {
            team.removeEntry(entry);
        }

        entity.setGlowing(false);
    }
}