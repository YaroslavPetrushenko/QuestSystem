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

public class RewardMenuListener implements Listener {

    private final QuestManager questManager;
    private final RewardEditMenu editMenu;
    private final RewardItemsMenu itemsMenu;
    private final RewardInputHandler inputHandler;

    public RewardMenuListener(QuestManager questManager, RewardEditMenu editMenu,
                              RewardItemsMenu itemsMenu, RewardInputHandler inputHandler) {
        this.questManager = questManager;
        this.editMenu = editMenu;
        this.itemsMenu = itemsMenu;
        this.inputHandler = inputHandler;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();

        if (title.startsWith("§8Квесты")) {
            event.setCancelled(true);
            // handled by PlayerQuestListener
        }

        if (title.startsWith("§8§lПредметы награды:")) {
            // Allow free movement in the items menu (slots 27-53), block top row
            int slot = event.getRawSlot();
            if (slot < 27) {
                event.setCancelled(true);
            }
            // Handle save/back clicks
            if (slot == 44 || slot == 45) {
                event.setCancelled(true);
                String questId = RewardQuestSelectMenu.getPendingQuest(player);
                if (questId == null) return;
                Quest quest = questManager.getQuest(questId);
                if (quest == null) return;
                itemsMenu.handleClick(player, slot, event.getView().getTopInventory(), quest);
            }
            return;
        }

        if (title.startsWith("§8Награда:")) {
            event.setCancelled(true);
            int slot = event.getSlot();
            String questId = RewardQuestSelectMenu.getPendingQuest(player);
            if (questId == null) return;
            Quest quest = questManager.getQuest(questId);
            if (quest == null) return;

            switch (slot) {
                case 11 -> {
                    player.closeInventory();
                    inputHandler.startInput(player, questId, "xp");
                    MessageUtil.send(player, "Напишите количество опыта:");
                }
                case 13 -> {
                    itemsMenu.open(player, quest);
                }
                case 15 -> {
                    player.closeInventory();
                    inputHandler.startInput(player, questId, "command");
                    MessageUtil.send(player, "Напишите команду (используйте %player% для ника):");
                }
                case 26 -> {
                    RewardQuestSelectMenu.clearPending(player);
                    player.closeInventory();
                    questManager.saveData();
                    MessageUtil.sendSuccess(player, "Награда сохранена!");
                }
                case 9 -> {
                    player.closeInventory();
                    questManager.deleteQuest(questId);
                    RewardQuestSelectMenu.clearPending(player);
                    MessageUtil.sendSuccess(player, "Квест §e" + quest.getName() + " §aудалён!");
                }
            }
        }
    }
}