package com.mongenscave.mclootbox.guis.impl.editor;

import com.mongenscave.mclootbox.McLootbox;
import com.mongenscave.mclootbox.data.MenuController;
import com.mongenscave.mclootbox.guis.PaginatedMenu;
import com.mongenscave.mclootbox.guis.impl.LootboxPreviewMenu;
import com.mongenscave.mclootbox.identifiers.LootboxKeys;
import com.mongenscave.mclootbox.identifiers.keys.ItemKeys;
import com.mongenscave.mclootbox.identifiers.keys.MenuKeys;
import com.mongenscave.mclootbox.item.ItemFactory;
import com.mongenscave.mclootbox.model.Lootbox;
import com.mongenscave.mclootbox.processor.MessageProcessor;
import com.mongenscave.mclootbox.service.LootboxEditorService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public final class LootboxEditorMainMenu extends PaginatedMenu {
    private static final String CONTENT_SLOTS_PATH = "editor-main.lootbox-slots";

    private final Map<Integer, String> slotToLootboxId = new ConcurrentHashMap<>();
    private List<Integer> contentSlots = List.of();
    private final List<String> lootboxIds = new ArrayList<>();

    public LootboxEditorMainMenu(@NotNull MenuController menuController) {
        super(menuController);
    }

    public static void open(@NotNull Player player) {
        new LootboxEditorMainMenu(new MenuController(player)).open();
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        int raw = event.getRawSlot();
        int topSize = inventory.getSize();

        if (raw < topSize) {
            event.setCancelled(true);

            if (isKeySlot(raw, ItemKeys.EDITOR_MAIN_PREV)) {
                prevPage();
                return;
            }

            if (isKeySlot(raw, ItemKeys.EDITOR_MAIN_NEXT)) {
                nextPage();
                return;
            }

            if (isKeySlot(raw, ItemKeys.EDITOR_MAIN_CLOSE)) {
                menuController.owner().closeInventory();
                return;
            }

            if (isKeySlot(raw, ItemKeys.EDITOR_MAIN_CREATE)) {
                LootboxEditorService.getInstance().createHandler().begin(menuController.owner());
                return;
            }

            if (isKeySlot(raw, ItemKeys.EDITOR_MAIN_RELOAD)) {
                reloadAndRefresh();
                return;
            }

            String id = slotToLootboxId.get(raw);
            if (id != null) {
                if (event.getClick().isLeftClick()) {
                    LootboxEditorLootboxMenu.open(menuController.owner(), id);
                    return;
                }

                Optional<Lootbox> opt = McLootbox.getInstance().getLootboxManager().getLootbox(id);
                if (opt.isEmpty()) return;

                new LootboxPreviewMenu(menuController, opt.get()).open();
                return;
            }

            return;
        }

        event.setCancelled(true);
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        slotToLootboxId.clear();
        lootboxIds.clear();

        ItemFactory.setItemsForMenu("editor-main.items", inventory);

        this.contentSlots = McLootbox.getInstance().getGuis().getIntList(CONTENT_SLOTS_PATH);
        if (contentSlots == null || contentSlots.isEmpty()) {
            int size = inventory.getSize();
            List<Integer> tmp = new ArrayList<>();
            for (int i = 0; i < size; i++) tmp.add(i);
            this.contentSlots = tmp;
        }

        lootboxIds.addAll(McLootbox.getInstance().getLootboxManager().getLootboxIds());
        lootboxIds.sort(Comparator.naturalOrder());

        renderPage();
    }

    private void renderPage() {
        int pageSize = contentSlots.size();
        if (pageSize == 0) return;

        int total = lootboxIds.size();
        int maxPage = Math.max(0, (total - 1) / pageSize);

        if (page > maxPage) page = maxPage;
        if (page < 0) page = 0;

        ItemStack template = ItemKeys.EDITOR_MAIN_LOOTBOX_TEMPLATE.getItem();

        int start = page * pageSize;
        int endExclusive = Math.min(start + pageSize, total);

        for (int i = start, slotIdx = 0; i < endExclusive && slotIdx < contentSlots.size(); i++, slotIdx++) {
            int slot = contentSlots.get(slotIdx);
            if (slot < 0 || slot >= inventory.getSize()) continue;

            String id = lootboxIds.get(i);
            Optional<Lootbox> optional = McLootbox.getInstance().getLootboxManager().getLootbox(id);
            if (optional.isEmpty()) continue;

            Lootbox lootbox = optional.get();

            ItemStack icon = McLootbox.getInstance().getLootboxManager().createLootboxItem(lootbox, 1)
                    .orElseGet(() -> template != null ? template.clone() : null);

            if (icon == null) continue;

            icon.setAmount(1);

            if (template != null && template.hasItemMeta()) {
                icon.editMeta(meta -> {
                    meta.setDisplayName(MessageProcessor.process(
                            template.getItemMeta().getDisplayName()
                                    .replace("{id}", lootbox.getId())
                                    .replace("{display_name}", lootbox.getDisplayName())
                    ));

                    List<String> loreTemplate = template.getItemMeta().getLore();
                    if (loreTemplate != null) {
                        List<String> built = new ArrayList<>();

                        for (String line : loreTemplate) {
                            built.add(MessageProcessor.process(
                                    line.replace("{id}", lootbox.getId())
                                            .replace("{display_name}", lootbox.getDisplayName())
                                            .replace("{normal_rewards}", String.valueOf(lootbox.getNormalRewards().getRewards().size()))
                                            .replace("{final_rewards}", String.valueOf(lootbox.getFinalRewards().getRewards().size()))
                            ));
                        }

                        meta.setLore(built);
                    }

                    meta.getPersistentDataContainer().set(LootboxKeys.LOOTBOX_ID, PersistentDataType.STRING, lootbox.getId());
                });
            } else {
                icon.editMeta(meta -> meta.getPersistentDataContainer().set(LootboxKeys.LOOTBOX_ID, PersistentDataType.STRING, lootbox.getId()));
            }

            inventory.setItem(slot, icon);
            slotToLootboxId.put(slot, lootbox.getId());
        }
    }

    private void prevPage() {
        if (page <= 0) return;

        page--;
        setMenuItems();
    }

    private void nextPage() {
        int pageSize = contentSlots.size();
        if (pageSize == 0) return;

        int total = lootboxIds.size();
        int maxPage = Math.max(0, (total - 1) / pageSize);

        if (page >= maxPage) return;

        page++;
        setMenuItems();
    }

    private boolean isKeySlot(int raw, @NotNull ItemKeys key) {
        ItemStack item = key.getItem();
        List<Integer> slots = key.getSlots();

        if (item == null || slots == null || slots.isEmpty()) return false;

        return slots.contains(raw);
    }

    private void reloadAndRefresh() {
        Player player = menuController.owner();
        player.sendMessage("Reloading lootboxes...");

        McLootbox plugin = McLootbox.getInstance();
        plugin.getLootboxManager().reload().thenRun(() -> McLootbox.getScheduler().runTask(plugin, () -> {
            player.sendMessage("Lootboxes reloaded.");

            if (player.getOpenInventory().getTopInventory().equals(inventory)) {
                setMenuItems();
                player.updateInventory();
            } else {
                open(player);
            }
        }));
    }

    @NotNull
    @Override
    public String getMenuName() {
        return MenuKeys.EDITOR_MAIN_MENU_TITLE.getString();
    }

    @Override
    public int getSlots() {
        return MenuKeys.EDITOR_MAIN_MENU_SIZE.getInt();
    }

    @Override
    public int getMenuTick() {
        return 0;
    }
}