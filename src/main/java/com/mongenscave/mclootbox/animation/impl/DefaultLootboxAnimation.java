package com.mongenscave.mclootbox.animation.impl;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.animation.AnimationContext;
import com.mongenscave.mclootbox.animation.AnimationController;
import com.mongenscave.mclootbox.animation.LootboxAnimation;
import com.mongenscave.mclootbox.model.LootboxReward;
import com.mongenscave.mclootbox.service.LootboxRewardService;
import com.mongenscave.mclootbox.utils.LoggerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public final class DefaultLootboxAnimation implements LootboxAnimation {

    private static final int INTRO_TICKS = 100;
    private static final int PREVIEW_SPAWN_DELAY = 8;
    private static final int ORBIT_SPIN_TICKS = 80;
    private static final int ORBIT_STOP_TICKS = 20;
    private static final int NORMAL_REVEAL_DELAY = 30;

    private static final int SUCK_IN_DURATION = 30;
    private static final int FINAL_SLIDE_DURATION = 40;
    private static final int CENTER_SHRINK_DURATION = 20;

    private static final double ORBIT_RADIUS = 1.61;
    private static final double ORBIT_Y = 0.15;

    private static final float PREVIEW_SCALE = 0.5f;
    private static final float WIN_SCALE = 0.8f;
    private static final float FINAL_WIN_SCALE = 0.9f;

    @Override
    public void start(AnimationContext context) {
        McLootbox.getScheduler().runTask(() -> run(context));
    }

    private void run(@NotNull AnimationContext context) {

        Location base = context.player().getEyeLocation()
                .add(context.player().getLocation().getDirection().normalize().multiply(2.8))
                .add(0, -0.35, 0);

        ItemDisplay center = spawnCenter(base, context.displayItem());

        List<LootboxReward> normalPool = new ArrayList<>(context.lootbox().getRewards().getRewards());
        List<LootboxReward> normalWon = context.normalRewards();

        List<LootboxReward> finalPool = new ArrayList<>(context.lootbox().getFinalRewards().getRewards());
        List<LootboxReward> finalWon = context.finalRewards();

        Map<LootboxReward, ItemDisplay> previews = new LinkedHashMap<>();

        MyScheduledTask[] task = new MyScheduledTask[1];
        int[] tick = {0};
        int[] spawnIndex = {0};
        int[] revealIndex = {0};

        task[0] = McLootbox.getScheduler().runTaskTimer(() -> {
            try {
                if (!context.player().isOnline()) {
                    cleanup(center, previews.values(), context);
                    task[0].cancel();
                    return;
                }

                if (tick[0] < INTRO_TICKS) {
                    double p = tick[0] / (double) INTRO_TICKS;
                    double ease = 1 - Math.pow(1 - p, 3);

                    center.setTransformation(new Transformation(
                            new Vector3f(),
                            new Quaternionf().rotateY((float) (ease * Math.PI * 2)),
                            new Vector3f((float) (0.2 + 0.8 * ease)),
                            new Quaternionf()
                    ));

                    tick[0]++;
                    return;
                }

                int orbitTick = tick[0] - INTRO_TICKS;

                if (spawnIndex[0] < normalPool.size()
                        && orbitTick % PREVIEW_SPAWN_DELAY == 0) {

                    LootboxReward reward = normalPool.get(spawnIndex[0]++);
                    ItemDisplay preview = spawnPreview(base, previewItem(reward));

                    double angle = (2 * Math.PI / normalPool.size()) * spawnIndex[0];
                    Location orbitLoc = base.clone().add(
                            Math.cos(angle) * ORBIT_RADIUS,
                            ORBIT_Y,
                            Math.sin(angle) * ORBIT_RADIUS
                    );

                    preview.teleport(orbitLoc);
                    previews.put(reward, preview);

                    preview.getWorld().spawnParticle(
                            Particle.BUBBLE_POP,
                            orbitLoc,
                            16,
                            0.12,
                            0.12,
                            0.12,
                            0.02
                    );

                    context.player().playSound(
                            context.player().getLocation(),
                            Sound.BLOCK_DECORATED_POT_INSERT,
                            0.8f,
                            1.4f
                    );
                }

                if (orbitTick < ORBIT_SPIN_TICKS) {
                    rotateOrbit(base, previews.values(), orbitTick * 0.08);
                    tick[0]++;
                    return;
                }

                if (orbitTick < ORBIT_SPIN_TICKS + ORBIT_STOP_TICKS) {
                    rotateOrbit(base, previews.values(), ORBIT_SPIN_TICKS * 0.08);
                    tick[0]++;
                    return;
                }

                if (revealIndex[0] < normalWon.size()
                        && orbitTick % NORMAL_REVEAL_DELAY == 0) {

                    LootboxReward reward = normalWon.get(revealIndex[0]++);
                    ItemDisplay display = previews.get(reward);

                    if (display != null) {
                        display.setTransformation(new Transformation(
                                new Vector3f(),
                                new Quaternionf(),
                                new Vector3f(WIN_SCALE, WIN_SCALE, WIN_SCALE),
                                new Quaternionf()
                        ));
                    }

                    LootboxRewardService.give(context.player(), reward);
                }

                if (revealIndex[0] >= normalWon.size()) {
                    task[0].cancel();
                    suckInNormalRewards(center, previews.values(), context);
                    startFinalPhase(context, center, finalPool, finalWon);
                    return;
                }

                tick[0]++;

            } catch (Throwable t) {
                LoggerUtils.error(t.getMessage());
                cleanup(center, previews.values(), context);
                task[0].cancel();
            }
        }, 0L, 1L);
    }

    private void suckInNormalRewards(@NotNull ItemDisplay center, @NotNull Collection<ItemDisplay> previews, AnimationContext context) {
        Location target = center.getLocation();

        Map<ItemDisplay, Location> start = new HashMap<>();
        previews.forEach(d -> start.put(d, d.getLocation()));

        MyScheduledTask[] task = new MyScheduledTask[1];
        int[] tick = {0};

        task[0] = McLootbox.getScheduler().runTaskTimer(() -> {
            double t = tick[0] / (double) SUCK_IN_DURATION;
            double ease = 1 - Math.pow(1 - t, 3);

            for (ItemDisplay d : previews) {
                Location s = start.get(d);

                d.teleport(new Location(
                        s.getWorld(),
                        lerp(s.getX(), target.getX(), ease),
                        lerp(s.getY(), target.getY(), ease),
                        lerp(s.getZ(), target.getZ(), ease)
                ));

                float scale = (float) (PREVIEW_SCALE * (1 - ease));
                d.setTransformation(new Transformation(
                        new Vector3f(),
                        new Quaternionf(),
                        new Vector3f(scale, scale, scale),
                        new Quaternionf()
                ));
            }

            if (++tick[0] >= SUCK_IN_DURATION) {
                previews.forEach(ItemDisplay::remove);

                context.player().playSound(
                        context.player().getLocation(),
                        Sound.ENTITY_ENDERMAN_TELEPORT,
                        0.7f,
                        1.6f
                );

                task[0].cancel();
            }
        }, 0L, 1L);
    }

    private void startFinalPhase(@NotNull AnimationContext context, @NotNull ItemDisplay center, @NotNull List<LootboxReward> finalPool, List<LootboxReward> finalWon) {
        Location base = center.getLocation().clone().add(0, 1.2, 0);

        Vector3f toPlayer = new Vector3f(
                (float) (context.player().getLocation().getX() - base.getX()),
                0f,
                (float) (context.player().getLocation().getZ() - base.getZ())
        ).normalize();

        Vector3f rightVector = new Vector3f(-toPlayer.z, 0f, toPlayer.x);

        double spacing = 1.15;
        double startOffset = -(finalPool.size() - 1) * spacing / 2.0;

        List<ItemDisplay> displays = new ArrayList<>();
        Map<ItemDisplay, Location> targets = new HashMap<>();
        Map<ItemDisplay, Quaternionf> rotations = new HashMap<>();

        Quaternionf rot = facePlayerYaw(base, context.player().getLocation());

        for (int i = 0; i < finalPool.size(); i++) {
            LootboxReward reward = finalPool.get(i);

            ItemDisplay d = base.getWorld().spawn(base, ItemDisplay.class);
            d.setItemStack(new ItemStack(Material.valueOf(reward.getMaterial().toUpperCase())));
            d.setBillboard(Display.Billboard.FIXED);

            d.setTransformation(new Transformation(
                    new Vector3f(),
                    rot,
                    new Vector3f(0.5f, 0.5f, 0.5f),
                    new Quaternionf()
            ));

            double offset = startOffset + i * spacing;
            Location target = base.clone().add(
                    rightVector.x * offset,
                    0,
                    rightVector.z * offset
            );

            displays.add(d);
            targets.put(d, target);
            rotations.put(d, rot);
        }

        animateFinalReveal(context, center, displays, targets, rotations, finalPool, finalWon);
    }

    private void animateFinalReveal(AnimationContext context, ItemDisplay center, @NotNull List<ItemDisplay> displays, Map<ItemDisplay, Location> targets, Map<ItemDisplay, Quaternionf> rotations, List<LootboxReward> finalPool, List<LootboxReward> finalWon) {
        Map<ItemDisplay, Location> start = new HashMap<>();
        displays.forEach(d -> start.put(d, d.getLocation()));

        MyScheduledTask[] task = new MyScheduledTask[1];
        int[] tick = {0};

        task[0] = McLootbox.getScheduler().runTaskTimer(() -> {
            double t = tick[0] / (double) FINAL_SLIDE_DURATION;
            double ease = 1 - Math.pow(1 - t, 3);

            for (ItemDisplay d : displays) {
                Location s = start.get(d);
                Location target = targets.get(d);

                d.teleport(new Location(
                        s.getWorld(),
                        lerp(s.getX(), target.getX(), ease),
                        lerp(s.getY(), target.getY(), ease),
                        lerp(s.getZ(), target.getZ(), ease)
                ));

                d.setTransformation(new Transformation(
                        new Vector3f(),
                        rotations.get(d),
                        new Vector3f(0.5f, 0.5f, 0.5f),
                        new Quaternionf()
                ));
            }

            if (++tick[0] >= FINAL_SLIDE_DURATION) {
                task[0].cancel();
                revealFinalWinners(context, center, displays, finalPool, finalWon);
            }
        }, 0L, 1L);
    }

    private void revealFinalWinners(AnimationContext context, ItemDisplay center, List<ItemDisplay> displays, List<LootboxReward> finalPool, @NotNull List<LootboxReward> finalWon) {
        int delay = 40;

        for (LootboxReward reward : finalWon) {
            ItemDisplay win = displays.get(finalPool.indexOf(reward));

            McLootbox.getScheduler().runTaskLater(() -> {
                win.getWorld().strikeLightningEffect(win.getLocation());

                win.setTransformation(new Transformation(
                        new Vector3f(),
                        new Quaternionf(),
                        new Vector3f(FINAL_WIN_SCALE, FINAL_WIN_SCALE, FINAL_WIN_SCALE),
                        new Quaternionf()
                ));

                context.player().playSound(
                        context.player().getLocation(),
                        Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                        1f,
                        0.9f
                );

                LootboxRewardService.give(context.player(), reward);
            }, delay);

            delay += 45;
        }

        McLootbox.getScheduler().runTaskLater(() -> {
            displays.forEach(ItemDisplay::remove);
            shrinkAndRemoveCenter(center, context);
        }, delay + 30);
    }

    private void shrinkAndRemoveCenter(ItemDisplay center, AnimationContext context) {
        MyScheduledTask[] task = new MyScheduledTask[1];
        int[] tick = {0};

        task[0] = McLootbox.getScheduler().runTaskTimer(() -> {
            double t = tick[0] / (double) CENTER_SHRINK_DURATION;
            double ease = 1 - Math.pow(1 - t, 3);

            float scale = (float) (1 - ease);

            center.setTransformation(new Transformation(
                    new Vector3f(),
                    new Quaternionf(),
                    new Vector3f(scale, scale, scale),
                    new Quaternionf()
            ));

            if (++tick[0] >= CENTER_SHRINK_DURATION) {
                center.remove();
                AnimationController.end(context.player().getUniqueId());
                task[0].cancel();
            }
        }, 0L, 1L);
    }

    private void rotateOrbit(Location base, @NotNull Collection<ItemDisplay> displays, double angleBase) {
        int size = displays.size();
        int index = 0;

        for (ItemDisplay d : displays) {
            double angle = angleBase + (2 * Math.PI / size) * index++;

            double x = Math.cos(angle) * ORBIT_RADIUS;
            double z = Math.sin(angle) * ORBIT_RADIUS;

            d.teleport(base.clone().add(x, ORBIT_Y, z));
        }
    }

    @NotNull
    private ItemDisplay spawnCenter(@NotNull Location base, @NotNull ItemStack item) {
        ItemDisplay d = base.getWorld().spawn(base, ItemDisplay.class);
        d.setItemStack(item.clone());

        return d;
    }

    @NotNull
    private ItemDisplay spawnPreview(@NotNull Location base, ItemStack item) {
        ItemDisplay d = base.getWorld().spawn(base, ItemDisplay.class);
        d.setItemStack(item);
        d.setTransformation(new Transformation(
                new Vector3f(),
                new Quaternionf(),
                new Vector3f(PREVIEW_SCALE, PREVIEW_SCALE, PREVIEW_SCALE),
                new Quaternionf()
        ));

        return d;
    }

    @NotNull
    @Contract("_ -> new")
    private ItemStack previewItem(@NotNull LootboxReward reward) {
        return new ItemStack(Material.valueOf(reward.getMaterial().toUpperCase()));
    }

    private void cleanup(ItemDisplay center, @NotNull Collection<ItemDisplay> previews, AnimationContext context) {
        previews.forEach(ItemDisplay::remove);
        if (center != null) center.remove();
        AnimationController.end(context.player().getUniqueId());
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private Quaternionf facePlayerYaw(@NotNull Location itemLoc, @NotNull Location playerLoc) {
        double dx = playerLoc.getX() - itemLoc.getX();
        double dz = playerLoc.getZ() - itemLoc.getZ();

        float yaw = (float) Math.atan2(-dx, dz) + (float) Math.PI;

        return new Quaternionf().rotateY(yaw);
    }
}