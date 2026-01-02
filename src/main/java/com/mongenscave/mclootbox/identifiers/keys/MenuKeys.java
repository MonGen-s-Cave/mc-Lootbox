package com.mongenscave.mclootbox.identifiers.keys;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.config.Config;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum MenuKeys {
    SUMMARY_MENU_TITLE("summary-menu.title"),
    SUMMARY_MENU_SIZE("summary-menu.size"),

    PREVIEW_MENU_TITLE("preview-menu.title"),
    PREVIEW_MENU_SIZE("preview-menu.size");

    private static final Config config = McLootbox.getInstance().getGuis();
    private final String path;

    MenuKeys(@NotNull String path) { this.path = path; }

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
}