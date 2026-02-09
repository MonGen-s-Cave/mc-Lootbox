package com.mongenscave.mclootbox.hologram;

import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LootboxTextHologram implements LootboxHologram {

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
                hologram.getString("billboard", "VERTICAL").toUpperCase()
        );

        List<String> lines = hologram.getStringList("lines");

        Location loc = base.clone().add(0, yOffset, 0);

        for (int index = 0; index < lines.size(); index++) {
            String raw = lines.get(index);

            Location lineLoc = loc.clone().add(0, -0.25 * index, 0);

            TextDisplay display = lineLoc.getWorld().spawn(lineLoc, TextDisplay.class);

            String text = raw.replace("{player}", player.getName())
                    .replace("{lootbox}", lootbox.getDisplayName());

            text = translateHex(text);
            text = MessageProcessor.process(text);

            display.text(deserializeLegacy(text));

            display.setAlignment(TextDisplay.TextAlignment.CENTER);
            display.setBillboard(billboard);
            display.setPersistent(false);
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

    public static @NotNull Component deserializeLegacy(@NotNull String input) {
        if (input.indexOf('§') >= 0) {
            return LEGACY_SECTION.deserialize(input);
        }

        return LEGACY_AMP.deserialize(input);
    }

    public static @NotNull String translateHex(@NotNull String input) {
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
