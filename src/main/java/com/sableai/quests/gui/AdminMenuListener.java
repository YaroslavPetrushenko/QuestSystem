package com.sableai.quests.gui;

import com.sableai.quests.core.Quest;
import com.sableai.quests.core.QuestManager;
import com.sableai.quests.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class AdminMenuListener implements Listener {

    private final JavaPlugin plugin;
    private final QuestManager questManager;
    private final AdminQuestMenu adminMenu;
    private final Map<UUID, QuestCreateState> creatingQuest = new HashMap<>();
    private final Map<UUID, String> pendingNames = new HashMap<>();
    private final Map<UUID, Quest.QuestType> pendingTypes = new HashMap<>();

    private enum QuestCreateState { NAME, TYPE, TARGET }

    public AdminMenuListener(JavaPlugin plugin, QuestManager questManager, AdminQuestMenu adminMenu) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.adminMenu = adminMenu;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getView().getTitle().equals("§8§lАдмин-панель квестов")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        int slot = event.getSlot();

        if (slot == 0) {
            var dm = questManager.getDailyManager();
            dm.setDailyEnabled(!dm.isDailyEnabled());
            MessageUtil.send(player, dm.isDailyEnabled() ? "§aЕжедневные квесты включены" : "§cЕжедневные квесты выключены");
            adminMenu.open(player);
            return;
        }

        if (slot == 1) {
            var dm = questManager.getDailyManager();
            dm.setWeeklyEnabled(!dm.isWeeklyEnabled());
            MessageUtil.send(player, dm.isWeeklyEnabled() ? "§aЕженедельные квесты включены" : "§cЕженедельные квесты выключены");
            adminMenu.open(player);
            return;
        }

        if (slot == 2) {
            questManager.getDailyManager().regenerateDaily();
            questManager.getDailyManager().regenerateWeekly();
            MessageUtil.sendSuccess(player, "Ежедневные и еженедельные квесты обновлены!");
            adminMenu.open(player);
            return;
        }

        if (clicked.getType() == Material.EMERALD && clicked.hasItemMeta()) {
            String display = clicked.getItemMeta().getDisplayName();
            if ("§a§lСоздать новый квест".equals(display)) {
                player.closeInventory();
                creatingQuest.put(player.getUniqueId(), QuestCreateState.NAME);
                MessageUtil.send(player, "Напишите название нового квеста в чат:");
                return;
            }
        }

        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasLore()) return;

        String questId = null;
        for (String line : clicked.getItemMeta().getLore()) {
            if (line.startsWith("§7ID: ")) {
                questId = line.substring(6);
                break;
            }
        }
        if (questId == null) return;
        Quest quest = questManager.getQuest(questId);
        if (quest != null) {
            adminMenu.getRewardEditMenu().open(player, quest);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        QuestCreateState state = creatingQuest.get(uuid);
        if (state == null) return;

        event.setCancelled(true);

        if (state == QuestCreateState.NAME) {
            String name = event.getMessage().trim();
            if (name.isEmpty()) {
                MessageUtil.sendError(player, "Имя не может быть пустым!");
                Bukkit.getScheduler().runTask(plugin, () -> adminMenu.open(player));
                creatingQuest.remove(uuid);
                return;
            }
            creatingQuest.put(uuid, QuestCreateState.TYPE);
            pendingNames.put(uuid, name);
            MessageUtil.send(player, "Выберите тип квеста:");
            MessageUtil.send(player, "§7- §eKILL §7- убивать мобов");
            MessageUtil.send(player, "§7- §eCOLLECT §7- собирать предметы");
            MessageUtil.send(player, "§7- §eLOCATION §7- дойти до места");
            MessageUtil.send(player, "§7- §eDIALOG §7- поговорить с NPC");
            return;
        }

        if (state == QuestCreateState.TYPE) {
            String typeStr = event.getMessage().trim().toUpperCase();
            Quest.QuestType type;
            try {
                type = Quest.QuestType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                MessageUtil.sendError(player, "Неверный тип! Введите: KILL, COLLECT, LOCATION, DIALOG");
                return;
            }
            pendingTypes.put(uuid, type);
            creatingQuest.put(uuid, QuestCreateState.TARGET);

            switch (type) {
                case KILL -> MessageUtil.send(player, "Напишите тип моба (например: ZOMBIE, SKELETON, CREEPER):");
                case COLLECT -> MessageUtil.send(player, "Напишите материал (например: POPPY, DIAMOND, IRON_INGOT):");
                case LOCATION -> MessageUtil.send(player, "Напишите координаты (world,x,y,z):");
                case DIALOG -> MessageUtil.send(player, "Напишите имя NPC или тип сущности:");
            }
            return;
        }

        if (state == QuestCreateState.TARGET) {
            String target = event.getMessage().trim().toUpperCase();
            String name = pendingNames.remove(uuid);
            Quest.QuestType type = pendingTypes.remove(uuid);
            creatingQuest.remove(uuid);

            String questId = name.toLowerCase().replace(' ', '_');
            Quest quest = new Quest(questId, name, "", type, 10);
            quest.setTargetData(target);
            questManager.createQuest(questId, name, "", type, 10);
            questManager.getQuest(questId).setTargetData(target);

            MessageUtil.sendSuccess(player, "Квест §e" + name + " §aсоздан! Цель: §e" + target);
            Bukkit.getScheduler().runTask(plugin, () -> adminMenu.open(player));
        }
    }
}