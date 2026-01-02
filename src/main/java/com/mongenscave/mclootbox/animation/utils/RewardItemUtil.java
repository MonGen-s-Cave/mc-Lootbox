package com.mongenscave.mclootbox.animation.utils;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

public final class RewardItemUtil {

    public final ItemDisplay item;
    public final TextDisplay label;

    public RewardItemUtil(ItemDisplay item, TextDisplay label) {
        this.item = item;
        this.label = label;
    }

    public boolean tick() {
        if (!item.isValid() || item.isDead()) {
            remove();
            return false;
        }

        label.teleport(item.getLocation().clone().add(0, 0.35, 0));
        return true;
    }

    public void remove() {
        if (label.isValid()) {
            label.remove();
            GlowUtil.clearGlow(item);
        }
    }
}