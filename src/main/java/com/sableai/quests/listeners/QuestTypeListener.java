package com.sableai.quests.listeners;

import com.sableai.quests.core.Quest;
import com.sableai.quests.core.QuestManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class QuestTypeListener implements Listener {

    private final QuestManager questManager;

    public QuestTypeListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Location loc = player.getLocation();

        for (String questId : questManager.getActiveQuests(player)) {
            Quest quest = questManager.getQuest(questId);
            if (quest == null || quest.getType() != Quest.QuestType.LOCATION) continue;
            String target = quest.getTargetData();
            if (target == null) continue;

            try {
                String[] parts = target.split(",");
                if (parts.length < 4) continue;
                if (!parts[0].equals(loc.getWorld().getName())) continue;
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                double dist = loc.distanceSquared(new Location(loc.getWorld(), x, y, z));
                if (dist <= 16) { // within 4 blocks
                    int prog = questManager.getProgress(player, questId);
                    if (prog < 1) {
                        questManager.addProgress(player, questId, 1);
                        player.sendMessage("§a✓ Вы прибыли в нужное место!");
                    }
                }
            } catch (Exception ignored) {}
        }

        questManager.getDailyManager().addProgress(player, Quest.QuestType.LOCATION, "move");
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        String entityName = event.getRightClicked().getType().name();
        if (event.getRightClicked().getCustomName() != null) {
            entityName = event.getRightClicked().getCustomName();
        }

        for (String questId : questManager.getActiveQuests(player)) {
            Quest quest = questManager.getQuest(questId);
            if (quest == null || quest.getType() != Quest.QuestType.DIALOG) continue;
            if (quest.getTargetData() == null) continue;

            if (quest.getTargetData().equalsIgnoreCase(entityName)
                || quest.getTargetData().equalsIgnoreCase(event.getRightClicked().getType().name())) {
                int prog = questManager.getProgress(player, questId);
                if (prog < 1) {
                    questManager.addProgress(player, questId, 1);
                    player.sendMessage("§a✓ Диалог завершён!");
                }
            }
        }

        questManager.getDailyManager().addProgress(player, Quest.QuestType.DIALOG, entityName);
    }
}