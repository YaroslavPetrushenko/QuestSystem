package com.sableai.quests.gui;

import com.sableai.quests.core.Quest;
import com.sableai.quests.core.QuestManager;
import com.sableai.quests.core.QuestReward;
import com.sableai.quests.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RewardItemsMenu implements Listener {

    private final QuestManager questManager;
    private final RewardEditMenu rewardEditMenu;
    private final Set<UUID> savedFlag = new HashSet<>();
    private final Map<UUID, Integer> loadedCount = new HashMap<>();

    public RewardItemsMenu(QuestManager questManager, RewardEditMenu rewardEditMenu) {
        this.questManager = questManager;
        this.rewardEditMenu = rewardEditMenu;
    }

    public void open(Player player, Quest quest) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§lПредметы награды: " + quest.getName());

        List<String> saved = quest.getReward().getItems();
        int count = 0;
        for (int i = 0; i < saved.size() && i < 27; i++) {
            ItemStack item = QuestReward.stringToItem(saved.get(i));
            if (item != null) {
                inv.setItem(27 + i, item);
                count++;
            }
        }
        loadedCount.put(player.getUniqueId(), count);

        ItemStack info = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName("§7Клади предметы сюда ↓");
        info.setItemMeta(im);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, info);
        }

        ItemStack save = new ItemStack(Material.LIME_DYE);
        ItemMeta sm = save.getItemMeta();
        sm.setDisplayName("§a§lСохранить предметы");
        List<String> lore = new ArrayList<>();
        lore.add("§7Все предметы в нижней части");
        lore.add("§7будут сохранены как награда.");
        lore.add("");
        lore.add("§e§lНажмите, чтобы сохранить");
        sm.setLore(lore);
        save.setItemMeta(sm);
        inv.setItem(44, save);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName("§e§l← Назад (без сохранения)");
        back.setItemMeta(bm);
        inv.setItem(45, back);

        savedFlag.remove(player.getUniqueId());
        player.openInventory(inv);
        RewardQuestSelectMenu.setPendingQuest(player, quest.getQuestId());
    }

    public void handleClick(Player player, int slot, Inventory inv, Quest quest) {
        if (slot == 44) {
            saveItems(player, inv, quest);
        } else if (slot == 45) {
            revertItems(player, inv);
            savedFlag.add(player.getUniqueId());
            rewardEditMenu.open(player, quest);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!event.getView().getTitle().startsWith("§8§lПредметы награды:")) return;
        if (savedFlag.contains(player.getUniqueId())) {
            savedFlag.remove(player.getUniqueId());
            return;
        }
        returnNewItems(player, event.getInventory());
    }

    private void saveItems(Player player, Inventory inv, Quest quest) {
        quest.getReward().clearItems();
        for (int i = 27; i < 54; i++) {
            if (i == 44 || i == 45) continue;
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR && item.getType() != Material.BLACK_STAINED_GLASS_PANE) {
                String encoded = QuestReward.itemToString(item);
                if (encoded != null) {
                    quest.getReward().addItem(encoded);
                }
            }
        }
        questManager.saveData();
        savedFlag.add(player.getUniqueId());
        loadedCount.remove(player.getUniqueId());
        MessageUtil.sendSuccess(player, "Сохранено " + quest.getReward().getItems().size() + " предметов!");
        rewardEditMenu.open(player, quest);
    }

    private void revertItems(Player player, Inventory inv) {
        int preloaded = loadedCount.getOrDefault(player.getUniqueId(), 0);
        for (int i = 27 + preloaded; i < 54; i++) {
            if (i == 44 || i == 45) continue;
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR && item.getType() != Material.BLACK_STAINED_GLASS_PANE) {
                player.getInventory().addItem(item).values()
                        .forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
            }
        }
        loadedCount.remove(player.getUniqueId());
    }

    private void returnNewItems(Player player, Inventory inv) {
        int preloaded = loadedCount.getOrDefault(player.getUniqueId(), 0);
        loadedCount.remove(player.getUniqueId());
        for (int i = 27 + preloaded; i < 54; i++) {
            if (i == 44 || i == 45) continue;
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR && item.getType() != Material.BLACK_STAINED_GLASS_PANE) {
                player.getInventory().addItem(item).values()
                        .forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
            }
        }
    }
}