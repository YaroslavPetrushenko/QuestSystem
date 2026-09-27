package com.sableai.quests.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import com.sableai.quests.core.QuestManager;
import com.sableai.quests.core.Quest;

public class QuestProgressListener implements Listener {

    private QuestManager questManager;

    public QuestProgressListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack picked = event.getItem().getItemStack();
        String pickedType = picked.getType().name();

        for (String questId : questManager.getActiveQuests(player)) {
            Quest quest = questManager.getQuest(questId);
            if (quest == null || quest.getType() != Quest.QuestType.COLLECT) continue;
            if (quest.getTargetData() == null || quest.getTargetData().equals(pickedType)) {
                int old = questManager.getProgress(player, questId);
                int count = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == picked.getType()) {
                        count += item.getAmount();
                    }
                }
                int newProg = Math.min(count, quest.getTargetAmount());
                int diff = newProg - old;
                if (diff > 0) {
                    questManager.addProgress(player, questId, diff);
                }
            }
        }

        questManager.getDailyManager().addProgress(player, Quest.QuestType.COLLECT, pickedType);
    }
}