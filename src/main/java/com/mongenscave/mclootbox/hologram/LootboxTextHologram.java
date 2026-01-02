package com.mongenscave.mclootbox.hologram;

import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class LootboxTextHologram implements LootboxHologram {

    private final List<TextDisplay> displays = new ArrayList<>();

    private final Location base;
    private final Player player;
    private final Lootbox lootbox;

    public LootboxTextHologram(@NotNull Location base, Player player, Lootbox lootbox) {
        this.base = base.clone();
        this.player = player;
        this.lootbox = lootbox;
    }

    @Override
    public void spawn() {
        Section hologram = lootbox.getConfig().getSection("visual.hologram");
        if (hologram == null) return;

        double yOffset = hologram.getDouble("y-offset", 1.2);
        float scale = hologram.getFloat("scale", 1.0f);
        boolean shadow = hologram.getBoolean("text-shadow", true);
        String backgroundRaw = hologram.getString("background", "TRANSPARENT");

        Display.Billboard billboard = Display.Billboard.valueOf(
                hologram.getString("billboard", "VERTICAL").toUpperCase());

        List<String> lines = hologram.getStringList("lines");

        Location loc = base.clone().add(0, yOffset, 0);

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);

            Location lineLoc = loc.clone().add(0, -0.25 * i, 0);

            TextDisplay display = lineLoc.getWorld().spawn(lineLoc, TextDisplay.class);

            display.text(Component.text(MessageProcessor.process(
                    raw.replace("{player}", player.getName())
                            .replace("{lootbox}", lootbox.getDisplayName()))));

            display.setBillboard(billboard);
            display.setShadowed(shadow);
            display.setViewRange(64.0f);
            display.setDefaultBackground(false);

            Color background = parseBackground(backgroundRaw);
            if (background != null) {
                display.setBackgroundColor(background);
            }

            display.setTransformation(new Transformation(
                    new Vector3f(),
                    new Quaternionf(),
                    new Vector3f(scale, scale, scale),
                    new Quaternionf()
            ));

            displays.add(display);
        }
    }

    @Override
    public void remove() {
        displays.forEach(TextDisplay::remove);
        displays.clear();
    }

    @Nullable
    private Color parseBackground(@NotNull String input) {
        if (input.equalsIgnoreCase("TRANSPARENT")) {
            return Color.fromARGB(0, 0, 0, 0);
        }

        if (input.startsWith("#") && input.length() == 9) {
            try {
                int argb = (int) Long.parseLong(input.substring(1), 16);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                return Color.fromARGB(a, r, g, b);
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }
}