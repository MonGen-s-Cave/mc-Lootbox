package com.mongenscave.mclootbox.animation.impl;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.animation.*;
import com.mongenscave.mclootbox.animation.utils.GlowUtil;
import com.mongenscave.mclootbox.animation.utils.RewardItemUtil;
import com.mongenscave.mclootbox.model.LootboxReward;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import com.mongenscave.mclootbox.service.LootboxRewardService;
import com.mongenscave.mclootbox.utils.LoggerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public final class DefaultLootboxAnimation implements LootboxAnimation {
    private final List<RewardItemUtil> labels = new ArrayList<>();

    private static final int INTRO_TICKS = 100;
    private static final int PREVIEW_SPAWN_DELAY = 8;
    private static final int ORBIT_SPIN_TICKS = 80;
    private static final int ORBIT_STOP_TICKS = 20;
    private static final int NORMAL_REVEAL_DELAY = 30;

    private static final int SUCK_IN_DURATION = 30;
    private static final int FINAL_SLIDE_DURATION = 50;
    private static final int CENTER_SHRINK_DURATION = 20;

    private static final double ORBIT_RADIUS = 1.61;
    private static final double ORBIT_Y = 0.15;

    private static final float PREVIEW_SCALE = 0.5f;
    private static final float WIN_SCALE = 0.7f;

    private static final int NORMAL_RISE_DURATION = 14;
    private static final double NORMAL_RISE_Y = 0.18;
    private static final int NORMAL_POST_REVEAL_DELAY = 40;
    private static final float NORMAL_LABEL_BASE_SCALE = 0.45f;
    private static final float NORMAL_LABEL_RISE_BOOST = 0.08f;

    private static final float FINAL_WIN_SCALE = 0.65f;
    private static final int FINAL_RISE_DURATION = 20;
    private static final double FINAL_RISE_Y = 0.30;

    private static final int FINAL_SUSPENSE_CYCLES = 14;
    private static final int FINAL_SUSPENSE_STEP_TICKS = 6;
    private static final float FINAL_SUSPENSE_SCALE = 0.65f;
    private static final int FINAL_SUSPENSE_RESET_DELAY = 8;

    @Override
    public void start(@NotNull AnimationContext context) {
        McLootbox.getScheduler().runTask(() -> run(context));
    }

    private void run(@NotNull AnimationContext context) {
        float openYaw = context.player().getLocation().getYaw();

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
        boolean[] cutPlayed = {false};

        context.hologram().spawn();

        task[0] = McLootbox.getScheduler().runTaskTimer(() -> {
            labels.forEach(RewardItemUtil::tick);

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

                    attachLabel(preview, reward);

                    preview.getWorld().spawnParticle(
                            Particle.BUBBLE_POP,
                            orbitLoc,
                            16,
                            0.12,
                            0.12,
                            0.12,
                            0.02
                    );

                    preview.getWorld().playSound(
                            orbitLoc,
                            Sound.BLOCK_DECORATED_POT_INSERT,
                            1.0f,
                            1.0f
                    );
                }

                if (orbitTick < ORBIT_SPIN_TICKS) {
                    rotateOrbit(base, previews.values(), orbitTick * 0.08);

                    if (orbitTick % 4 == 0) {
                        float progress = orbitTick / (float) ORBIT_SPIN_TICKS;

                        float pitch = 0.8f + progress * 0.6f;
                        float volume = 0.4f + progress * 0.2f;

                        context.player().playSound(
                                context.player().getLocation(),
                                Sound.BLOCK_NOTE_BLOCK_HAT,
                                volume,
                                pitch
                        );

                        if (orbitTick % 12 == 0) {
                            context.player().playSound(
                                    context.player().getLocation(),
                                    Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                                    0.25f,
                                    1.2f + progress * 0.4f
                            );
                        }
                    }

                    tick[0]++;
                    return;
                }

                if (orbitTick < ORBIT_SPIN_TICKS + ORBIT_STOP_TICKS) {

                    if (!cutPlayed[0]) {
                        cutPlayed[0] = true;

                        context.player().playSound(
                                context.player().getLocation(),
                                Sound.BLOCK_NOTE_BLOCK_BASS,
                                0.9f,
                                0.6f
                        );
                    }

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

                        GlowUtil.applyGlow(display, NamedTextColor.GREEN);
                        playNormalWinRise(display, openYaw);

                        context.player().playSound(
                                context.player().getLocation(),
                                Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                0.6f,
                                1.2f
                        );
                    }

                    LootboxRewardService.give(context.player(), reward);
                    context.player().playSound(
                            context.player().getLocation(),
                            Sound.UI_BUTTON_CLICK,
                            0.8f,
                            1.4f
                    );
                }

                if (revealIndex[0] >= normalWon.size()) {
                    task[0].cancel();

                    McLootbox.getScheduler().runTaskLater(() -> {
                        suckInNormalRewards(center, previews.values());

                        McLootbox.getScheduler().runTaskLater(() -> startFinalPhase(context, center, finalPool, finalWon, openYaw), SUCK_IN_DURATION + 2);
                    }, NORMAL_POST_REVEAL_DELAY);
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

    private void startFinalPhase(AnimationContext context, @NotNull ItemDisplay center, @NotNull List<LootboxReward> finalPool, List<LootboxReward> finalWon, float openYaw) {
        Location base = center.getLocation().clone().add(0, 1.2, 0);

        Quaternionf fixedRotation = yawRotation(openYaw);

        Vector3f forward = new Vector3f(
                -(float) Math.sin(Math.toRadians(openYaw)),
                0f,
                (float) Math.cos(Math.toRadians(openYaw))
        ).normalize();

        Vector3f right = new Vector3f(-forward.z, 0f, forward.x);

        double spacing = 1.15;
        double startOffset = -(finalPool.size() - 1) * spacing / 2.0;

        List<ItemDisplay> displays = new ArrayList<>();
        Map<ItemDisplay, Location> targets = new HashMap<>();

        for (int i = 0; i < finalPool.size(); i++) {
            LootboxReward reward = finalPool.get(i);

            ItemDisplay d = base.getWorld().spawn(base, ItemDisplay.class);
            d.setItemStack(previewItem(reward));
            d.setBillboard(Display.Billboard.FIXED);

            attachLabel(d, reward);

            d.setTransformation(new Transformation(
                    new Vector3f(),
                    fixedRotation,
                    new Vector3f(PREVIEW_SCALE, PREVIEW_SCALE, PREVIEW_SCALE),
                    new Quaternionf()
            ));

            double offset = startOffset + i * spacing;
            Location target = base.clone().add(
                    right.x * offset,
                    0,
                    right.z * offset
            );

            displays.add(d);
            targets.put(d, target);
        }

        animateFinalReveal(context, center, displays, targets, fixedRotation, finalPool, finalWon);
    }

    private void animateFinalReveal(AnimationContext context, ItemDisplay center, @NotNull List<ItemDisplay> displays, Map<ItemDisplay, Location> targets, Quaternionf fixedRotation, List<LootboxReward> finalPool, List<LootboxReward> finalWon) {
        Map<ItemDisplay, Location> start = new HashMap<>();
        displays.forEach(d -> start.put(d, d.getLocation()));

        MyScheduledTask[] task = new MyScheduledTask[1];
        int[] tick = {0};

        task[0] = McLootbox.getScheduler().runTaskTimer(() -> {
            double t = tick[0] / (double) FINAL_SLIDE_DURATION;
            double ease = 1 - Math.pow(1 - t, 3);

            labels.removeIf(label -> !label.tick());

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
                        fixedRotation,
                        new Vector3f(PREVIEW_SCALE, PREVIEW_SCALE, PREVIEW_SCALE),
                        new Quaternionf()
                ));
            }

            if (++tick[0] >= FINAL_SLIDE_DURATION) {
                task[0].cancel();

                playFinalSuspense(
                        context,
                        center,
                        displays,
                        fixedRotation,
                        finalPool,
                        finalWon
                );
            }
        }, 0L, 1L);
    }

    private void revealFinalWinners(AnimationContext context, ItemDisplay center, List<ItemDisplay> displays, List<LootboxReward> finalPool, @NotNull List<LootboxReward> finalWon, Quaternionf fixedRotation) {
        int delay = 40;

        for (LootboxReward reward : finalWon) {
            ItemDisplay win = displays.get(finalPool.indexOf(reward));

            McLootbox.getScheduler().runTaskLater(() -> {

                Location startLoc = win.getLocation();
                Location endLoc = startLoc.clone().add(0, FINAL_RISE_Y, 0);

                MyScheduledTask[] riseTask = new MyScheduledTask[1];
                int[] tick = {0};

                removeLabelFor(win);

                GlowUtil.applyGlow(win, NamedTextColor.GOLD);

                context.player().playSound(
                        context.player().getLocation(),
                        Sound.UI_TOAST_CHALLENGE_COMPLETE,
                        1.0f,
                        1.0f
                );

                context.player().playSound(
                        context.player().getLocation(),
                        Sound.ENTITY_PLAYER_LEVELUP,
                        0.9f,
                        1.35f
                );

                riseTask[0] = McLootbox.getScheduler().runTaskTimer(() -> {
                    double t = tick[0] / (double) FINAL_RISE_DURATION;
                    double ease = 1 - Math.pow(1 - t, 3);

                    win.teleport(new Location(
                            startLoc.getWorld(),
                            lerp(startLoc.getX(), endLoc.getX(), ease),
                            lerp(startLoc.getY(), endLoc.getY(), ease),
                            lerp(startLoc.getZ(), endLoc.getZ(), ease)
                    ));

                    float scale = (float) (FINAL_WIN_SCALE + 0.15 * ease);

                    win.setTransformation(new Transformation(
                            new Vector3f(),
                            fixedRotation,
                            new Vector3f(scale, scale, scale),
                            new Quaternionf()
                    ));

                    if (++tick[0] >= FINAL_RISE_DURATION) {
                        riseTask[0].cancel();
                    }

                }, 0L, 1L);

                LootboxRewardService.give(context.player(), reward);

            }, delay);

            delay += 45;
        }

        McLootbox.getScheduler().runTaskLater(() -> {
            displays.forEach(ItemDisplay::remove);
            shrinkAndRemoveCenter(center, context);
        }, delay + 30);
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

    private Quaternionf yawRotation(float yaw) {
        float radians = (float) Math.toRadians(-yaw);
        return new Quaternionf().rotateY(radians);
    }

    private void rotateOrbit(Location base, @NotNull Collection<ItemDisplay> displays, double angleBase) {
        int size = displays.size();
        int index = 0;

        for (ItemDisplay d : displays) {
            double angle = angleBase + (2 * Math.PI / size) * index++;
            d.teleport(base.clone().add(
                    Math.cos(angle) * ORBIT_RADIUS,
                    ORBIT_Y,
                    Math.sin(angle) * ORBIT_RADIUS
            ));
        }
    }

    private void playNormalWinRise(@NotNull ItemDisplay display, float openYaw) {
        Location start = display.getLocation();
        Location end = start.clone().add(0, NORMAL_RISE_Y, 0);

        MyScheduledTask[] task = new MyScheduledTask[1];
        int[] tick = {0};

        Quaternionf fixedRotation = yawRotation(openYaw);
        RewardItemUtil label = getLabelFor(display);

        task[0] = McLootbox.getScheduler().runTaskTimer(() -> {
            double t = tick[0] / (double) NORMAL_RISE_DURATION;
            double ease = 1 - Math.pow(1 - t, 3);

            display.teleport(new Location(
                    start.getWorld(),
                    lerp(start.getX(), end.getX(), ease),
                    lerp(start.getY(), end.getY(), ease),
                    lerp(start.getZ(), end.getZ(), ease)
            ));

            float itemScale = (float) (WIN_SCALE + 0.1 * ease);
            display.setTransformation(new Transformation(
                    new Vector3f(),
                    fixedRotation,
                    new Vector3f(itemScale, itemScale, itemScale),
                    new Quaternionf()
            ));

            if (label != null && label.label.isValid()) {
                float labelScale = NORMAL_LABEL_BASE_SCALE + NORMAL_LABEL_RISE_BOOST * (float) ease;

                label.label.setTransformation(new Transformation(
                        new Vector3f(),
                        new Quaternionf(),
                        new Vector3f(labelScale, labelScale, labelScale),
                        new Quaternionf()
                ));
            }

            if (++tick[0] >= NORMAL_RISE_DURATION) {
                task[0].cancel();
            }
        }, 0L, 1L);
    }

    private void suckInNormalRewards(@NotNull ItemDisplay center, @NotNull Collection<ItemDisplay> previews) {
        Location target = center.getLocation();

        Map<ItemDisplay, Location> start = new HashMap<>();
        previews.forEach(d -> start.put(d, d.getLocation()));

        MyScheduledTask[] task = new MyScheduledTask[1];
        int[] tick = {0};

        labels.removeIf(label -> {
            ItemDisplay item = label.item;

            if (previews.contains(item)) {
                label.remove();
                return true;
            }

            return false;
        });

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
                task[0].cancel();
            }
        }, 0L, 1L);
    }

    private void playFinalSuspense(AnimationContext context, ItemDisplay center, @NotNull List<ItemDisplay> displays, Quaternionf fixedRotation, List<LootboxReward> finalPool, List<LootboxReward> finalWon) {
        int totalSteps = displays.size() * FINAL_SUSPENSE_CYCLES;

        MyScheduledTask[] task = new MyScheduledTask[1];
        int[] step = {0};

        task[0] = McLootbox.getScheduler().runTaskTimer(() -> {

            int index = step[0] % displays.size();
            labels.removeIf(label -> !label.tick());

            for (int i = 0; i < displays.size(); i++) {
                ItemDisplay d = displays.get(i);

                float scale = (i == index)
                        ? FINAL_SUSPENSE_SCALE
                        : PREVIEW_SCALE;

                d.setTransformation(new Transformation(
                        new Vector3f(),
                        fixedRotation,
                        new Vector3f(scale, scale, scale),
                        new Quaternionf()
                ));
            }

            context.player().playSound(
                    context.player().getLocation(),
                    Sound.BLOCK_NOTE_BLOCK_BASEDRUM,
                    0.8f,
                    1.0f + (step[0] * 0.04f)
            );

            if (++step[0] >= totalSteps) {
                task[0].cancel();

                for (ItemDisplay d : displays) {
                    d.setTransformation(new Transformation(
                            new Vector3f(),
                            fixedRotation,
                            new Vector3f(PREVIEW_SCALE, PREVIEW_SCALE, PREVIEW_SCALE),
                            new Quaternionf()
                    ));
                }

                McLootbox.getScheduler().runTaskLater(() -> revealFinalWinners(context, center, displays, finalPool, finalWon, fixedRotation), FINAL_SUSPENSE_RESET_DELAY);
            }

        }, 0L, FINAL_SUSPENSE_STEP_TICKS);
    }

    private void shrinkAndRemoveCenter(ItemDisplay center, AnimationContext context) {
        MyScheduledTask[] task = new MyScheduledTask[1];
        int[] tick = {0};

        task[0] = McLootbox.getScheduler().runTaskTimer(() -> {
            double t = tick[0] / (double) CENTER_SHRINK_DURATION;
            float scale = (float) (1 - (1 - Math.pow(1 - t, 3)));

            labels.removeIf(label -> !label.tick());

            center.setTransformation(new Transformation(
                    new Vector3f(),
                    new Quaternionf(),
                    new Vector3f(scale, scale, scale),
                    new Quaternionf()
            ));

            if (++tick[0] >= CENTER_SHRINK_DURATION) {
                center.remove();

                labels.forEach(RewardItemUtil::remove);
                labels.clear();

                context.hologram().remove();

                AnimationController.end(context.player().getUniqueId());
                task[0].cancel();
            }
        }, 0L, 1L);
    }

    private void attachLabel(@NotNull ItemDisplay item, @NotNull LootboxReward reward) {
        String raw = reward.getName();
        String name = raw != null && !raw.isEmpty() ? raw : reward.getMaterial();

        TextDisplay label = item.getWorld().spawn(item.getLocation().clone().add(0, 0.35, 0), TextDisplay.class);

        label.text(Component.text(MessageProcessor.process(name)));
        label.setBillboard(Display.Billboard.VERTICAL);
        label.setShadowed(true);
        label.setViewRange(32.0f);
        label.setDefaultBackground(false);
        label.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));

        label.setTransformation(new Transformation(
                new Vector3f(),
                new Quaternionf(),
                new Vector3f(0.45f, 0.45f, 0.45f),
                new Quaternionf()
        ));

        labels.add(new RewardItemUtil(item, label));
    }

    @Nullable
    @Contract(pure = true)
    private RewardItemUtil getLabelFor(ItemDisplay item) {
        for (RewardItemUtil label : labels) {
            if (label.item.equals(item)) {
                return label;
            }
        }
        return null;
    }

    private void removeLabelFor(ItemDisplay item) {
        labels.removeIf(label -> {
            if (label.item.equals(item)) {
                label.remove();
                return true;
            }
            return false;
        });
    }

    private void cleanup(ItemDisplay center, @NotNull Collection<ItemDisplay> previews, AnimationContext context) {
        previews.forEach(ItemDisplay::remove);
        if (center != null) center.remove();

        context.hologram().remove();
        labels.forEach(RewardItemUtil::remove);
        labels.clear();

        AnimationController.end(context.player().getUniqueId());
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}