package com.sableai.quests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sableai.quests.core.QuestManager;
import com.sableai.quests.gui.RewardQuestSelectMenu;
import com.sableai.quests.gui.PlayerQuestMenu;
import com.sableai.quests.gui.AdminQuestMenu;

public class QuestCommand implements CommandExecutor {

    private final QuestManager questManager;
    private final PlayerQuestMenu playerMenu;
    private final AdminQuestMenu adminMenu;

    public QuestCommand(QuestManager questManager) {
        this.questManager = questManager;
        this.playerMenu = new PlayerQuestMenu(questManager);
        this.adminMenu = new AdminQuestMenu(questManager, new com.sableai.quests.gui.RewardEditMenu(questManager));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            if (!player.isOp()) {
                player.sendMessage("§cНет прав!");
                return true;
            }
            adminMenu.open(player);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            showHelp(player);
            return true;
        }

        playerMenu.openMain(player);
        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage("§e=== Quest System ===");
        player.sendMessage("§a/quest §7- Открыть меню квестов");
        player.sendMessage("§a/quest help §7- Эта справка");
        if (player.isOp()) {
            player.sendMessage("§a/quest admin §7- Админ-панель");
        }
    }
}