package com.mongenscave.mclootbox.model;

import lombok.Getter;

import java.util.List;

@Getter
public final class LootboxVisual {

    private final String animation;
    private final List<String> hologram;

    public LootboxVisual(String animation, List<String> hologram) {
        this.animation = animation;
        this.hologram = hologram;
    }
}