package com.sableai.quests.gui;

import com.sableai.quests.core.Quest;
import com.sableai.quests.core.QuestManager;
import com.sableai.quests.utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerQuestListener implements Listener {

    private final QuestManager questManager;
    private final PlayerQuestMenu playerMenu;
    private final DailyQuestMenu dailyMenu;

    public PlayerQuestListener(QuestManager questManager, PlayerQuestMenu playerMenu, DailyQuestMenu dailyMenu) {
        this.questManager = questManager;
        this.playerMenu = playerMenu;
        this.dailyMenu = dailyMenu;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        String title = event.getView().getTitle();

        switch (title) {
            case "§8§lQuest System" -> {
                event.setCancelled(true);
                String display = displayName(event.getCurrentItem());
                if ("§a§lДоступные квесты".equals(display)) {
                    playerMenu.openAvailable(player);
                } else if ("§b§lАктивные квесты".equals(display)) {
                    playerMenu.openActive(player);
                } else if ("§d§lЕжедневные и еженедельные".equals(display)) {
                    dailyMenu.open(player);
                }
            }
            case "§8§lДоступные квесты" -> {
                event.setCancelled(true);
                if (isBackButton(event.getCurrentItem())) {
                    playerMenu.openMain(player);
                    return;
                }
                String questId = findQuestId(event.getCurrentItem());
                if (questId != null) {
                    questManager.startQuest(player, questId);
                    player.closeInventory();
                }
            }
            case "§8§lАктивные квесты" -> {
                event.setCancelled(true);
                if (isBackButton(event.getCurrentItem())) {
                    playerMenu.openMain(player);
                    return;
                }
                String questId = findQuestIdByName(event.getCurrentItem());
                if (questId != null) {
                    Quest quest = questManager.getQuest(questId);
                    if (quest == null) return;
                    int progress = questManager.getProgress(player, questId);
                    if (progress >= quest.getTargetAmount()) {
                        questManager.completeQuest(player, questId);
                        player.closeInventory();
                    } else {
                        MessageUtil.sendError(player, "Квест ещё не выполнен!");
                    }
                }
            }
        }
    }

    private String displayName(ItemStack item) {
        if (!item.hasItemMeta()) return "";
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() ? meta.getDisplayName() : "";
    }

    private boolean isBackButton(ItemStack item) {
        return "§e§l← Назад".equals(displayName(item));
    }

    private String findQuestId(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return null;
        boolean hasQuestLore = false;
        for (String line : item.getItemMeta().getLore()) {
            if (line.startsWith("§7Тип:")) { hasQuestLore = true; break; }
        }
        if (!hasQuestLore) return null;

        String name = displayName(item);
        if (name.isEmpty()) return null;
        String stripped = name.replace("§e§l", "");
        for (Quest q : questManager.getAllQuests().values()) {
            if (q.getName().equals(stripped)) {
                return q.getQuestId();
            }
        }
        return null;
    }

    private String findQuestIdByName(ItemStack item) {
        String name = displayName(item);
        if (name.isEmpty()) return null;
        // Active quests may have "§a§l✔ " prefix
        String stripped = name.replace("§a§l✔ ", "").replace("§e§l", "");
        for (Quest q : questManager.getAllQuests().values()) {
            if (q.getName().equals(stripped)) {
                return q.getQuestId();
            }
        }
        return null;
    }
}