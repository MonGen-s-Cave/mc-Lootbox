package com.mongenscave.mclootbox.animation.impl;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.animation.AnimationContext;
import com.mongenscave.mclootbox.animation.AnimationController;
import com.mongenscave.mclootbox.animation.LootboxAnimation;
import com.mongenscave.mclootbox.service.LootboxRewardService;
import com.mongenscave.mclootbox.utils.LoggerUtils;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class DefaultLootboxAnimation implements LootboxAnimation {

    private static final int TOTAL_TICKS = 100;

    @Override
    public void start(AnimationContext context) {
        McLootbox.getScheduler().runTask(() -> runSync(context));
    }

    private void runSync(@NotNull AnimationContext context) {

        Location base = context.player().getEyeLocation()
                .add(context.player().getLocation().getDirection().normalize().multiply(2.6))
                .add(0, -0.35, 0);

        ItemDisplay display = base.getWorld().spawn(base, ItemDisplay.class);
        display.setItemStack(context.displayItem().clone());

        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new Vector3f(0.2f, 0.2f, 0.2f),
                new Quaternionf()
        ));

        display.setInterpolationDelay(0);
        display.setInterpolationDuration(1);

        MyScheduledTask[] task = new MyScheduledTask[1];
        final int[] tick = {0};

        task[0] = McLootbox.getScheduler().runTaskTimer(() -> {
            try {
                if (!context.player().isOnline()) {
                    finish(context, display);
                    task[0].cancel();
                    return;
                }

                if (tick[0] >= TOTAL_TICKS) {
                    finish(context, display);
                    task[0].cancel();
                    return;
                }

                double progress = tick[0] / (double) TOTAL_TICKS;
                double easeOut = 1 - Math.pow(1 - progress, 3);

                float scale = (float) (0.2 + (1.0 - 0.2) * easeOut);
                float yaw = (float) (easeOut * Math.PI * 2);

                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new Quaternionf().rotateY(yaw),
                        new Vector3f(scale, scale, scale),
                        new Quaternionf()
                ));

                tick[0]++;
            } catch (Throwable throwable) {
                LoggerUtils.error(throwable.getMessage());
                finish(context, display);
                task[0].cancel();
            }
        }, 0L, 1L);
    }

    private void finish(@NotNull AnimationContext context, @NotNull ItemDisplay display) {
        display.remove();

        for (var reward : context.rewards()) {
            LootboxRewardService.give(context.player(), reward);
        }

        AnimationController.end(context.player().getUniqueId());
    }
}
