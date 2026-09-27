package com.sableai.quests.gui;

import com.sableai.quests.core.DailyManager;
import com.sableai.quests.core.Quest;
import com.sableai.quests.core.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AdminQuestMenu {

    private final QuestManager questManager;
    private final RewardEditMenu rewardEditMenu;

    public AdminQuestMenu(QuestManager questManager, RewardEditMenu rewardEditMenu) {
        this.questManager = questManager;
        this.rewardEditMenu = rewardEditMenu;
    }

    public void open(Player player) {
        DailyManager dm = questManager.getDailyManager();
        Map<String, Quest> all = questManager.getAllQuests();
        int size = Math.max(36, ((all.size() + 1) / 9 + 1) * 9);
        size = Math.min(size, 54);
        Inventory inv = Bukkit.createInventory(null, size, "§8§lАдмин-панель квестов");

        // Toggle daily
        ItemStack dailyToggle = new ItemStack(dm.isDailyEnabled() ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta dailym = dailyToggle.getItemMeta();
        dailym.setDisplayName(dm.isDailyEnabled() ? "§a§lЕжедневные: ВКЛ" : "§c§lЕжедневные: ВЫКЛ");
        dailyToggle.setItemMeta(dailym);
        inv.setItem(0, dailyToggle);

        // Toggle weekly
        ItemStack weeklyToggle = new ItemStack(dm.isWeeklyEnabled() ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta weeklym = weeklyToggle.getItemMeta();
        weeklym.setDisplayName(dm.isWeeklyEnabled() ? "§a§lЕженедельные: ВКЛ" : "§c§lЕженедельные: ВЫКЛ");
        weeklyToggle.setItemMeta(weeklym);
        inv.setItem(1, weeklyToggle);

        // Regenerate daily
        ItemStack regen = new ItemStack(Material.ENDER_PEARL);
        ItemMeta rm = regen.getItemMeta();
        rm.setDisplayName("§d§lОбновить ежедневные");
        regen.setItemMeta(rm);
        inv.setItem(2, regen);

        for (Quest q : all.values()) {
            ItemStack item = new ItemStack(Material.MAP);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + q.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7ID: " + q.getQuestId());
            lore.add("§7Тип: " + q.getType().name());
            lore.add("§7Цель: " + q.getTargetAmount());
            lore.add("§7Награда: " + q.getReward().toString());
            lore.add("");
            lore.add("§aНажмите, чтобы настроить награду");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        ItemStack create = new ItemStack(Material.EMERALD);
        ItemMeta cm = create.getItemMeta();
        cm.setDisplayName("§a§lСоздать новый квест");
        cm.setLore(Collections.singletonList("§7Нажмите, затем напишите имя в чат"));
        create.setItemMeta(cm);
        inv.setItem(inv.getSize() - 1, create);

        player.openInventory(inv);
    }

    public void createQuest(Player player, String name) {
        String questId = name.toLowerCase().replace(' ', '_');
        questManager.createQuest(questId, name, "", Quest.QuestType.KILL, 10);
        open(player);
    }

    public RewardEditMenu getRewardEditMenu() {
        return rewardEditMenu;
    }
}