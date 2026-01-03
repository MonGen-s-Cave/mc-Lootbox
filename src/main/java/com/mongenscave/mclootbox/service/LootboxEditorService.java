package com.mongenscave.mclootbox.service;

import com.mongenscave.mclootbox.handler.LootboxEditorCreateHandler;
import lombok.Getter;

public final class LootboxEditorService {

    @Getter private static final LootboxEditorService instance = new LootboxEditorService();
    private final LootboxEditorCreateHandler createHandler = new LootboxEditorCreateHandler();

    private LootboxEditorService() {
    }

    public LootboxEditorCreateHandler createHandler() {
        return createHandler;
    }
}