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
    PREVIEW_MENU_SIZE("preview-menu.size"),

    EDITOR_MAIN_MENU_TITLE("editor-main.title"),
    EDITOR_MAIN_MENU_SIZE("editor-main.size"),

    EDITOR_LOOTBOX_MENU_TITLE("editor-lootbox.title"),
    EDITOR_LOOTBOX_MENU_SIZE("editor-lootbox.size"),

    EDITOR_REWARD_LIST_TITLE("editor-rewards.title"),
    EDITOR_REWARD_LIST_SIZE("editor-rewards.size"),

    EDITOR_REWARD_TYPE_TITLE("editor-reward-type.title"),
    EDITOR_REWARD_TYPE_SIZE("editor-reward-type.size"),

    EDITOR_REWARD_ITEM_PICKER_TITLE("editor-reward-item-picker.title"),
    EDITOR_REWARD_ITEM_PICKER_SIZE("editor-reward-item-picker.size");

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

    public List<Integer> getIntList() {
        return config.getIntList(path);
    }

    public List<String> getList() {
        return config.getList(path);
    }
}