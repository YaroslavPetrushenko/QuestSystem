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

public class RewardEditMenu {

    private final QuestManager questManager;
    private final RewardItemsMenu itemsMenu;

    public RewardEditMenu(QuestManager questManager) {
        this.questManager = questManager;
        this.itemsMenu = new RewardItemsMenu(questManager, this);
    }

    public void open(Player player, Quest quest) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Награда: " + quest.getName());

        ItemStack xpItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xpMeta = xpItem.getItemMeta();
        xpMeta.setDisplayName("§bОпыт");
        List<String> xpLore = new ArrayList<>();
        xpLore.add("§7Текущий: §e" + quest.getReward().getXp() + " XP");
        xpLore.add("");
        xpLore.add("§aНажмите, чтобы изменить");
        xpMeta.setLore(xpLore);
        xpItem.setItemMeta(xpMeta);
        inv.setItem(11, xpItem);

        ItemStack itemItem = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = itemItem.getItemMeta();
        itemMeta.setDisplayName("§dПредметы");
        List<String> itemLore = new ArrayList<>();
        itemLore.add("§7Количество: §e" + quest.getReward().getItems().size());
        itemLore.add("");
        itemLore.add("§aНажмите, чтобы открыть меню предметов");
        itemMeta.setLore(itemLore);
        itemItem.setItemMeta(itemMeta);
        inv.setItem(13, itemItem);

        ItemStack cmdItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta cmdMeta = cmdItem.getItemMeta();
        cmdMeta.setDisplayName("§6Другое (команды)");
        List<String> cmdLore = new ArrayList<>();
        cmdLore.add("§7Команды при завершении квеста:");
        if (quest.getReward().getCommands().isEmpty()) {
            cmdLore.add(" §7- нет");
        } else {
            for (String c : quest.getReward().getCommands()) {
                cmdLore.add(" §7- " + c);
            }
        }
        cmdLore.add("");
        cmdLore.add("§aНажмите, чтобы добавить команду");
        cmdMeta.setLore(cmdLore);
        cmdItem.setItemMeta(cmdMeta);
        inv.setItem(15, cmdItem);

        ItemStack saveItem = new ItemStack(Material.LIME_DYE);
        ItemMeta saveMeta = saveItem.getItemMeta();
        saveMeta.setDisplayName("§a§lСохранить");
        saveItem.setItemMeta(saveMeta);
        inv.setItem(26, saveItem);

        ItemStack deleteItem = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        deleteMeta.setDisplayName("§c§lУдалить квест");
        List<String> deleteLore = new ArrayList<>();
        deleteLore.add("§7Полностью удаляет квест");
        deleteLore.add("§7и все данные о нём.");
        deleteLore.add("");
        deleteLore.add("§c§lНажмите для подтверждения");
        deleteMeta.setLore(deleteLore);
        deleteItem.setItemMeta(deleteMeta);
        inv.setItem(9, deleteItem);

        RewardQuestSelectMenu.setPendingQuest(player, quest.getQuestId());
        player.openInventory(inv);
    }

    public RewardItemsMenu getItemsMenu() {
        return itemsMenu;
    }
}