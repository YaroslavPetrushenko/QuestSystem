package com.sableai.quests.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtil {

    private static final String PREFIX = ChatColor.GOLD + "[Quests] " + ChatColor.RESET;

    public static void send(Player player, String message) {
        player.sendMessage(PREFIX + message);
    }

    public static void sendError(Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.RED + message);
    }

    public static void sendSuccess(Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.GREEN + message);
    }
}