package com.sableai.quests;

import org.bukkit.plugin.java.JavaPlugin;
import com.sableai.quests.core.QuestManager;
import com.sableai.quests.commands.QuestCommand;
import com.sableai.quests.listeners.PlayerKillListener;
import com.sableai.quests.listeners.QuestProgressListener;
import com.sableai.quests.listeners.QuestTypeListener;
import com.sableai.quests.gui.RewardMenuListener;
import com.sableai.quests.gui.RewardEditMenu;
import com.sableai.quests.gui.RewardItemsMenu;
import com.sableai.quests.gui.RewardInputHandler;
import com.sableai.quests.gui.PlayerQuestMenu;
import com.sableai.quests.gui.PlayerQuestListener;
import com.sableai.quests.gui.AdminQuestMenu;
import com.sableai.quests.gui.AdminMenuListener;
import com.sableai.quests.gui.DailyQuestMenu;

public class QuestSystem extends JavaPlugin {

    private static QuestSystem instance;
    private QuestManager questManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        questManager = new QuestManager(this);

        RewardEditMenu editMenu = new RewardEditMenu(questManager);
        RewardItemsMenu itemsMenu = new RewardItemsMenu(questManager, editMenu);
        RewardInputHandler inputHandler = new RewardInputHandler(this, questManager, editMenu);
        PlayerQuestMenu playerMenu = new PlayerQuestMenu(questManager);
        AdminQuestMenu adminMenu = new AdminQuestMenu(questManager, editMenu);
        DailyQuestMenu dailyMenu = new DailyQuestMenu(questManager);

        getCommand("quest").setExecutor(new QuestCommand(questManager));

        getServer().getPluginManager().registerEvents(
                new PlayerKillListener(questManager), this);
        getServer().getPluginManager().registerEvents(
                new QuestProgressListener(questManager), this);
        getServer().getPluginManager().registerEvents(
                new RewardMenuListener(questManager, editMenu, itemsMenu, inputHandler), this);
        getServer().getPluginManager().registerEvents(inputHandler, this);
        getServer().getPluginManager().registerEvents(itemsMenu, this);
        getServer().getPluginManager().registerEvents(
                new PlayerQuestListener(questManager, playerMenu, dailyMenu), this);
        getServer().getPluginManager().registerEvents(
                new AdminMenuListener(this, questManager, adminMenu), this);
        getServer().getPluginManager().registerEvents(dailyMenu, this);
        getServer().getPluginManager().registerEvents(
                new QuestTypeListener(questManager), this);

        getLogger().info("╔════════════════════════════╗");
        getLogger().info("║   QuestSystem включён!     ║");
        getLogger().info("║   v1.0.0 for Bukkit        ║");
        getLogger().info("╚════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        getLogger().info("QuestSystem отключён!");
    }

    public static QuestSystem getInstance() {
        return instance;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }
}