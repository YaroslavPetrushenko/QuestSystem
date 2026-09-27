package com.sableai.quests.gui;

import com.sableai.quests.core.DailyManager;
import com.sableai.quests.core.DailyQuestData;
import com.sableai.quests.core.QuestManager;
import com.sableai.quests.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DailyQuestMenu implements Listener {

    private static final String TITLE = "§8§lЕжедневные и еженедельные";
    private final QuestManager questManager;

    public DailyQuestMenu(QuestManager questManager) {
        this.questManager = questManager;
    }

    public void open(Player player) {
        DailyManager dm = questManager.getDailyManager();
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        ItemStack info = new ItemStack(Material.CLOCK);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName("§6§lЕжедневные и еженедельные квесты");
        List<String> lore = new ArrayList<>();
        lore.add("§7Автоматически обновляются.");
        lore.add("§7Прогресс отслеживается автоматически.");
        lore.add("");
        lore.add(dm.isDailyEnabled() ? "§a§lЕжедневные: ВКЛ" : "§c§lЕжедневные: ВЫКЛ");
        lore.add(dm.isWeeklyEnabled() ? "§a§lЕженедельные: ВКЛ" : "§c§lЕженедельные: ВЫКЛ");
        im.setLore(lore);
        info.setItemMeta(im);
        inv.setItem(4, info);

        int slot = 18;
        if (dm.isDailyEnabled()) {
            for (DailyQuestData q : dm.getCurrentDaily()) {
                inv.setItem(slot++, createQuestItem(player, q, dm));
            }
            slot = 27;
            for (DailyQuestData q : dm.getCurrentWeekly()) {
                inv.setItem(slot++, createQuestItem(player, q, dm));
            }
        }

        player.openInventory(inv);
    }

    private ItemStack createQuestItem(Player player, DailyQuestData q, DailyManager dm) {
        int progress = dm.getProgress(player, q.getId());
        boolean done = progress >= q.getTargetAmount();
        boolean claimed = dm.isClaimed(player, q.getId());

        Material mat = switch (q.getDifficulty()) {
            case EASY -> Material.LIME_DYE;
            case MEDIUM -> Material.ORANGE_DYE;
            case HARD -> Material.RED_DYE;
        };

        ItemStack item = new ItemStack(claimed ? Material.GREEN_STAINED_GLASS_PANE : done ? Material.LIME_DYE : mat);
        ItemMeta meta = item.getItemMeta();

        String prefix = q.getPeriod() == DailyQuestData.Period.DAILY ? "§e" : "§b";
        meta.setDisplayName(prefix + (claimed ? "§m" : "") + q.getName());

        List<String> l = new ArrayList<>();
        l.add("§7Тип: §f" + q.getType().name() + " §7(" + q.getTargetData() + ")");
        l.add("§7Сложность: " + difficultyColor(q.getDifficulty()) + q.getDifficulty().name());
        l.add("");
        l.add("§7Прогресс: " + getProgressBar(progress, q.getTargetAmount()));
        l.add("§e" + progress + "§7/§e" + q.getTargetAmount());
        l.add("");
        l.add("§7Награда: §e+" + q.getXpReward() + " опыта §7+ предметы");
            List<ItemStack> rewardItems = q.generateItemReward();
            for (ItemStack ri : rewardItems) {
                l.add("§7  §f" + ri.getAmount() + "x " + ri.getType().name());
            }
        l.add("");

        if (claimed) {
            l.add("§7✓ Награда получена");
        } else if (done) {
            l.add("§a§lНажмите, чтобы получить награду!");
        } else {
            l.add("§c§lЕщё не выполнен");
        }

        meta.setLore(l);
        item.setItemMeta(meta);
        return item;
    }

    private String difficultyColor(DailyQuestData.Difficulty d) {
        return switch (d) {
            case EASY -> "§a";
            case MEDIUM -> "§6";
            case HARD -> "§c";
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

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(TITLE)) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        DailyManager dm = questManager.getDailyManager();
        String name = event.getCurrentItem().hasItemMeta() ? event.getCurrentItem().getItemMeta().getDisplayName() : "";

        for (DailyQuestData q : dm.getCurrentDaily()) {
            if (("§e" + q.getName()).equals(name) || ("§e§m" + q.getName()).equals(name)) {
                if (dm.isClaimed(player, q.getId())) return;
                if (dm.getProgress(player, q.getId()) >= q.getTargetAmount()) {
                    dm.claimReward(player, q);
                    open(player);
                }
                return;
            }
        }
        for (DailyQuestData q : dm.getCurrentWeekly()) {
            if (("§b" + q.getName()).equals(name) || ("§b§m" + q.getName()).equals(name)) {
                if (dm.isClaimed(player, q.getId())) return;
                if (dm.getProgress(player, q.getId()) >= q.getTargetAmount()) {
                    dm.claimReward(player, q);
                    open(player);
                }
                return;
            }
        }
    }
}