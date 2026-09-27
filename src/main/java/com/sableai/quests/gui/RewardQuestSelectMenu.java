package com.sableai.quests.gui;

import com.sableai.quests.core.Quest;
import com.sableai.quests.core.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RewardQuestSelectMenu {

    private final QuestManager questManager;
    private static final Map<UUID, String> pendingQuest = new HashMap<>();
    private static final Map<UUID, Integer> pageMap = new HashMap<>();
    private static final int PAGE_SIZE = 45;

    public RewardQuestSelectMenu(QuestManager questManager) {
        this.questManager = questManager;
    }

    public void open(Player player) {
        openPage(player, 0);
    }

    public void openPage(Player player, int page) {
        List<Quest> questList = new ArrayList<>(questManager.getAllQuests().values());
        int maxPage = Math.max(0, (int) Math.ceil((double) questList.size() / PAGE_SIZE) - 1);
        page = Math.max(0, Math.min(page, maxPage));

        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, questList.size());
        List<Quest> pageQuests = questList.subList(from, to);

        int rows = Math.min(6, (pageQuests.size() / 9) + 2);
        Inventory inv = Bukkit.createInventory(null, rows * 9, "§8Квесты (стр. " + (page + 1) + "/" + (maxPage + 1) + ")");

        for (Quest quest : pageQuests) {
            ItemStack item = new ItemStack(Material.MAP);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + quest.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7ID: " + quest.getQuestId());
            lore.add("§7Тип: " + quest.getType().name());
            lore.add("§7Награда: " + quest.getReward().toString());
            lore.add("");
            lore.add("§aНажмите, чтобы редактировать награду");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName("§e← Назад");
            prev.setItemMeta(prevMeta);
            inv.setItem(rows * 9 - 9, prev);
        }

        if (page < maxPage) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName("§eВперёд →");
            next.setItemMeta(nextMeta);
            inv.setItem(rows * 9 - 1, next);
        }

        pageMap.put(player.getUniqueId(), page);
        pendingQuest.put(player.getUniqueId(), null);
        player.openInventory(inv);
    }

    public static int getPage(Player player) {
        return pageMap.getOrDefault(player.getUniqueId(), 0);
    }

    public static String getPendingQuest(Player player) {
        return pendingQuest.get(player.getUniqueId());
    }

    public static void setPendingQuest(Player player, String questId) {
        pendingQuest.put(player.getUniqueId(), questId);
    }

    public static void clearPending(Player player) {
        pendingQuest.remove(player.getUniqueId());
        pageMap.remove(player.getUniqueId());
    }
}