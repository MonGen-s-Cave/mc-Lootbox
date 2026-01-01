package com.mongenscave.mclootbox.identifiers.keys;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.config.Config;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public enum MessageKeys {
    NO_PERMISSION("messages.no-permission"),
    PLAYER_REQUIRED("messages.player-required"),
    PLAYER_NOT_FOUND("messages.player-not-found"),

    COMMAND_ADMIN_RELOAD("messages.admin.reload");

    private static final Config language = McLootbox.getInstance().getLanguage();
    private final String path;

    MessageKeys(@NotNull String path) {
        this.path = path;
    }

    public @NotNull String getMessage() {
        return MessageProcessor.process(language.getString(path)).replace("{prefix}", MessageProcessor.process(language.getString("prefix")));
    }

    public @NotNull List<String> getMessageList() {
        List<String> list = language.getStringList(path);
        String prefix = MessageProcessor.process(language.getString("prefix"));
        if (list == null || list.isEmpty()) return List.of();

        return list.stream()
                .map(MessageProcessor::process)
                .map(s -> s.replace("{prefix}", prefix))
                .toList();
    }
}