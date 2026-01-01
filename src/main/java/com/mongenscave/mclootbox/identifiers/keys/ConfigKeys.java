package com.mongenscave.mclootbox.identifiers.keys;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.config.Config;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum ConfigKeys {
    DATABASE_MYSQL("database.mysql"),
    DATABASE_POOL("database.pool");

    private static final Config config = McLootbox.getInstance().getConfiguration();
    private final String path;

    ConfigKeys(@NotNull String path) {
        this.path = path;
    }

    public static @NotNull String getString(@NotNull String path) {
        return config.getString(path);
    }

    public @NotNull String getString() {
        return MessageProcessor.process(config.getString(path));
    }

    public boolean getBoolean() {
        return config.getBoolean(path);
    }

    public int getInt() {
        return config.getInt(path);
    }

    public List<String> getList() {
        return config.getList(path);
    }

    public @NotNull Section getSection() {
        return config.getSection(path);
    }
}