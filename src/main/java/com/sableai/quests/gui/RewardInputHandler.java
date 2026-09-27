package com.sableai.quests.gui;

import com.sableai.quests.core.Quest;
import com.sableai.quests.core.QuestManager;
import com.sableai.quests.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RewardInputHandler implements Listener {

    private final JavaPlugin plugin;
    private final QuestManager questManager;
    private final RewardEditMenu editMenu;
    private final Map<UUID, InputSession> sessions = new HashMap<>();

    public record InputSession(String questId, String type) {}

    public RewardInputHandler(JavaPlugin plugin, QuestManager questManager, RewardEditMenu editMenu) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.editMenu = editMenu;
    }

    public void startInput(Player player, String questId, String type) {
        sessions.put(player.getUniqueId(), new InputSession(questId, type));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        InputSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        event.setCancelled(true);
        sessions.remove(player.getUniqueId());

        Quest quest = questManager.getQuest(session.questId());
        if (quest == null) return;

        String input = event.getMessage().trim();

        switch (session.type()) {
            case "xp" -> {
                try {
                    int xp = Integer.parseInt(input);
                    quest.getReward().setXp(xp);
                    MessageUtil.sendSuccess(player, "Опыт изменён на " + xp);
                } catch (NumberFormatException e) {
                    MessageUtil.sendError(player, "Введите число!");
                }
            }
            case "command" -> {
                quest.getReward().addCommand(input);
                MessageUtil.sendSuccess(player, "Команда добавлена: " + input);
            }
        }

        questManager.saveData();
        Bukkit.getScheduler().runTask(plugin, () -> editMenu.open(player, quest));
    }
}