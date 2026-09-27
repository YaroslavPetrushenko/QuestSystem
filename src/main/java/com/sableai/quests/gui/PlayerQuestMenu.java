package com.sableai.quests.gui;

import com.sableai.quests.core.Quest;
import com.sableai.quests.core.QuestManager;
import com.sableai.quests.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PlayerQuestMenu {

    private static final String TITLE_MAIN = "§8§lQuest System";
    private static final String TITLE_AVAILABLE = "§8§lДоступные квесты";
    private static final String TITLE_ACTIVE = "§8§lАктивные квесты";

    private final QuestManager questManager;

    public PlayerQuestMenu(QuestManager questManager) {
        this.questManager = questManager;
    }

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_MAIN);

        ItemStack available = new ItemStack(Material.BOOK);
        ItemMeta am = available.getItemMeta();
        am.setDisplayName("§a§lДоступные квесты");
        am.setLore(Arrays.asList("§7Квесты, которые можно начать", "", "§eНажмите, чтобы открыть"));
        available.setItemMeta(am);
        inv.setItem(11, available);

        ItemStack active = new ItemStack(Material.PAPER);
        ItemMeta acm = active.getItemMeta();
        acm.setDisplayName("§b§lАктивные квесты");
        int activeCount = questManager.getActiveQuests(player).size();
        acm.setLore(Arrays.asList("§7Ваши активные квесты", "§7Активно: §e" + activeCount, "", "§eНажмите, чтобы открыть"));
        active.setItemMeta(acm);
        inv.setItem(15, active);

        ItemStack daily = new ItemStack(Material.NETHER_STAR);
        ItemMeta dm = daily.getItemMeta();
        dm.setDisplayName("§d§lЕжедневные и еженедельные");
        List<String> dailyLore = new ArrayList<>();
        dailyLore.add("§7Автоматические квесты с наградами.");
        dailyLore.add("§7Обновляются каждый день и неделю.");
        dailyLore.add("");
        dailyLore.add("§eНажмите, чтобы открыть");
        dm.setLore(dailyLore);
        daily.setItemMeta(dm);
        inv.setItem(13, daily);

        fillBorder(inv);
        player.openInventory(inv);
    }

    public void openAvailable(Player player) {
        Map<String, Quest> all = questManager.getAllQuests();
        Set<String> active = questManager.getActiveQuests(player);

        if (all.isEmpty()) {
            MessageUtil.sendError(player, "Нет доступных квестов! Создайте их через /quest admin");
            return;
        }

        List<Quest> available = new ArrayList<>();
        for (Quest q : all.values()) {
            boolean isActive = active.contains(q.getQuestId());
            boolean isDone = questManager.isCompleted(player, q.getQuestId());
            if (!isActive && !isDone) {
                available.add(q);
            }
        }

        if (available.isEmpty()) {
            MessageUtil.send(player, "§7Всего квестов: §e" + all.size() + "§7, активных: §e" + active.size() + "§7, выполнено: §e" + countCompleted(player));
            MessageUtil.sendError(player, "Нет доступных квестов — все уже активны или выполнены!");
            return;
        }

        int size = Math.max(27, ((available.size() / 9) + 1) * 9 + 9);
        size = Math.min(size, 54);
        Inventory inv = Bukkit.createInventory(null, size, TITLE_AVAILABLE);

        for (Quest q : available) {
            ItemStack icon = getQuestIcon(q);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName("§e§l" + q.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + q.getDescription());
            lore.add("");
            lore.add("§7Тип: §f" + q.getType().name());
            lore.add("§7Цель: §f" + q.getTargetAmount());
            lore.add("§7Награда: §f" + q.getReward().toString());
            lore.add("");
            lore.add("§a§lНажмите, чтобы начать!");
            meta.setLore(lore);
            icon.setItemMeta(meta);
            inv.addItem(icon);
        }

        fillBack(inv, player);
        player.openInventory(inv);
    }

    public void openActive(Player player) {
        Set<String> activeIds = questManager.getActiveQuests(player);

        int size = Math.max(27, ((activeIds.size() / 9) + 1) * 9 + 9);
        size = Math.min(size, 54);
        Inventory inv = Bukkit.createInventory(null, size, TITLE_ACTIVE);

        for (String qId : activeIds) {
            Quest q = questManager.getQuest(qId);
            if (q == null) continue;

            int progress = questManager.getProgress(player, qId);
            int target = q.getTargetAmount();
            boolean done = progress >= target;

            ItemStack icon = done ? new ItemStack(Material.LIME_DYE) : getQuestIcon(q);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName((done ? "§a§l✔ " : "§e§l") + q.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + q.getDescription());
            lore.add("");
            lore.add("§7Прогресс:");
            lore.add(getProgressBar(progress, target));
            lore.add("§e" + progress + "§7/§e" + target);
            lore.add("");
            lore.add("§7Награда: §f" + q.getReward().toString());
            lore.add("");
            if (done) {
                lore.add("§a§lНажмите, чтобы получить награду!");
            } else {
                lore.add("§c§lЕщё не выполнен");
            }
            meta.setLore(lore);
            icon.setItemMeta(meta);
            inv.addItem(icon);
        }

        if (activeIds.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta em = empty.getItemMeta();
            em.setDisplayName("§c§lНет активных квестов");
            empty.setItemMeta(em);
            inv.setItem(13, empty);
        }

        fillBack(inv, player);
        player.openInventory(inv);
    }

    private ItemStack getQuestIcon(Quest q) {
        return switch (q.getType()) {
            case KILL -> new ItemStack(Material.DIAMOND_SWORD);
            case COLLECT -> new ItemStack(Material.CHEST);
            case LOCATION -> new ItemStack(Material.COMPASS);
            case DIALOG -> new ItemStack(Material.NAME_TAG);
        };
    }

    private String getProgressBar(int current, int max) {
        int bars = 20;
        int filled = (int) ((double) current / max * bars);
        StringBuilder sb = new StringBuilder("§a");
        for (int i = 0; i < bars; i++) {
            sb.append(i < filled ? '▓' : '░');
        }
        return sb.toString();
    }

    private void fillBorder(Inventory inv) {
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.setDisplayName(" ");
        border.setItemMeta(bm);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, border);
        }
    }

    private void fillBack(Inventory inv, Player player) {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName("§e§l← Назад");
        back.setItemMeta(bm);
        inv.setItem(inv.getSize() - 1, back);
    }

    private int countCompleted(Player player) {
        int count = 0;
        for (Quest q : questManager.getAllQuests().values()) {
            if (questManager.isCompleted(player, q.getQuestId())) count++;
        }
        return count;
    }
}