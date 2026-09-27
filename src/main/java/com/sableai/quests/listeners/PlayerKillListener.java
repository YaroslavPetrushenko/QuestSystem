package com.sableai.quests.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import com.sableai.quests.core.QuestManager;
import com.sableai.quests.core.Quest;

public class PlayerKillListener implements Listener {

    private QuestManager questManager;

    public PlayerKillListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        String killedType = event.getEntityType().name();

        for (String questId : questManager.getActiveQuests(killer)) {
            Quest quest = questManager.getQuest(questId);
            if (quest == null || quest.getType() != Quest.QuestType.KILL) continue;
            if (quest.getTargetData() == null || quest.getTargetData().equals(killedType)) {
                questManager.addProgress(killer, questId, 1);
            }
        }

        questManager.getDailyManager().addProgress(killer, Quest.QuestType.KILL, killedType);
    }
}