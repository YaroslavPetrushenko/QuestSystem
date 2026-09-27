package com.sableai.quests.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

public class DailyManager {

    private final JavaPlugin plugin;
    private final QuestManager questManager;
    private boolean dailyEnabled = true;
    private boolean weeklyEnabled = true;
    private List<DailyQuestData> currentDaily = new ArrayList<>();
    private List<DailyQuestData> currentWeekly = new ArrayList<>();
    private final Map<UUID, Map<String, Integer>> dailyProgress = new HashMap<>();
    private final Map<UUID, Set<String>> dailyClaimed = new HashMap<>();
    private long dailyGeneratedAt;
    private long weeklyGeneratedAt;
    private final Gson gson;
    private final File dataFile;

    public DailyManager(JavaPlugin plugin, QuestManager questManager) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFile = new File(plugin.getDataFolder(), "daily.json");
        load();
        checkReset();
    }

    public boolean isDailyEnabled() { return dailyEnabled; }
    public boolean isWeeklyEnabled() { return weeklyEnabled; }
    public List<DailyQuestData> getCurrentDaily() { return currentDaily; }
    public List<DailyQuestData> getCurrentWeekly() { return currentWeekly; }
    public void setDailyEnabled(boolean v) { dailyEnabled = v; save(); }
    public void setWeeklyEnabled(boolean v) { weeklyEnabled = v; save(); }

    public void regenerateDaily() {
        currentDaily = DailyQuestData.generateDaily();
        dailyGeneratedAt = System.currentTimeMillis();
        dailyProgress.clear();
        dailyClaimed.clear();
        save();
    }

    public void regenerateWeekly() {
        currentWeekly = DailyQuestData.generateWeekly();
        weeklyGeneratedAt = System.currentTimeMillis();
        dailyProgress.clear();
        dailyClaimed.clear();
        save();
    }

    private void regenerateDailyIfNeeded() {
        if (currentDaily.isEmpty() || isExpired(dailyGeneratedAt, 86400000L)) {
            regenerateDaily();
        }
    }

    private void regenerateWeeklyIfNeeded() {
        if (currentWeekly.isEmpty() || isExpired(weeklyGeneratedAt, 604800000L)) {
            regenerateWeekly();
        }
    }

    public void checkReset() {
        regenerateDailyIfNeeded();
        regenerateWeeklyIfNeeded();
    }

    private boolean isExpired(long timestamp, long period) {
        return System.currentTimeMillis() - timestamp > period;
    }

    public int getProgress(Player player, String questId) {
        return dailyProgress.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(questId, 0);
    }

    public boolean isClaimed(Player player, String questId) {
        return dailyClaimed.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(questId);
    }

    public void claimReward(Player player, DailyQuestData q) {
        UUID uuid = player.getUniqueId();
        dailyClaimed.computeIfAbsent(uuid, k -> new HashSet<>()).add(q.getId());

        player.giveExp(q.getXpReward());
        player.sendMessage("§a✓ Награда получена! +" + q.getXpReward() + " опыта");

        List<ItemStack> items = q.generateItemReward();
        for (ItemStack item : items) {
            player.getInventory().addItem(item).values()
                    .forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
            player.sendMessage("§a  + " + item.getAmount() + "x " + item.getType().name());
        }

        save();
    }

    public void addProgress(Player player, Quest.QuestType type, String targetData) {
        if (!dailyEnabled && !weeklyEnabled) return;
        UUID uuid = player.getUniqueId();

        for (DailyQuestData q : currentDaily) {
            if (isClaimed(player, q.getId())) continue;
            if (q.getType() != type) continue;
            if (q.getTargetData() != null && !q.getTargetData().equals(targetData)) continue;
            int old = dailyProgress.getOrDefault(uuid, new HashMap<>()).getOrDefault(q.getId(), 0);
            if (old >= q.getTargetAmount()) continue;
            dailyProgress.computeIfAbsent(uuid, k -> new HashMap<>()).put(q.getId(), old + 1);
            int now = old + 1;
            player.sendMessage("§e[Ежедневно] " + q.getName() + ": " + now + "/" + q.getTargetAmount());
            if (now >= q.getTargetAmount()) {
                player.sendMessage("§a✓ Ежедневный квест выполнен! Заберите награду в меню.");
            }
            save();
        }

        for (DailyQuestData q : currentWeekly) {
            if (isClaimed(player, q.getId())) continue;
            if (q.getType() != type) continue;
            if (q.getTargetData() != null && !q.getTargetData().equals(targetData)) continue;
            int old = dailyProgress.getOrDefault(uuid, new HashMap<>()).getOrDefault(q.getId(), 0);
            if (old >= q.getTargetAmount()) continue;
            dailyProgress.computeIfAbsent(uuid, k -> new HashMap<>()).put(q.getId(), old + 1);
            int now = old + 1;
            player.sendMessage("§b[Еженедельно] " + q.getName() + ": " + now + "/" + q.getTargetAmount());
            if (now >= q.getTargetAmount()) {
                player.sendMessage("§b✓ Еженедельный квест выполнен! Заберите награду в меню.");
            }
            save();
        }
    }

    private void load() {
        if (!dataFile.exists()) {
            regenerateDaily();
            regenerateWeekly();
            return;
        }
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<DailySaveData>() {}.getType();
            DailySaveData d = gson.fromJson(reader, type);
            if (d == null) return;
            dailyEnabled = d.dailyEnabled;
            weeklyEnabled = d.weeklyEnabled;
            dailyGeneratedAt = d.dailyGeneratedAt;
            weeklyGeneratedAt = d.weeklyGeneratedAt;
            if (d.currentDaily != null) currentDaily = new ArrayList<>(d.currentDaily);
            if (d.currentWeekly != null) currentWeekly = new ArrayList<>(d.currentWeekly);
            if (d.progress != null) {
                for (var e : d.progress.entrySet()) {
                    dailyProgress.put(UUID.fromString(e.getKey()), new HashMap<>(e.getValue()));
                }
            }
            if (d.claimed != null) {
                for (var e : d.claimed.entrySet()) {
                    dailyClaimed.put(UUID.fromString(e.getKey()), new HashSet<>(e.getValue()));
                }
            }
            plugin.getLogger().info("Daily manager loaded");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load daily data: " + e.getMessage());
        }
    }

    public void save() {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            DailySaveData d = new DailySaveData();
            d.dailyEnabled = dailyEnabled;
            d.weeklyEnabled = weeklyEnabled;
            d.dailyGeneratedAt = dailyGeneratedAt;
            d.weeklyGeneratedAt = weeklyGeneratedAt;
            d.currentDaily = new ArrayList<>(currentDaily);
            d.currentWeekly = new ArrayList<>(currentWeekly);
            d.progress = new HashMap<>();
            for (var e : dailyProgress.entrySet()) {
                d.progress.put(e.getKey().toString(), new HashMap<>(e.getValue()));
            }
            d.claimed = new HashMap<>();
            for (var e : dailyClaimed.entrySet()) {
                d.claimed.put(e.getKey().toString(), new ArrayList<>(e.getValue()));
            }
            try (FileWriter w = new FileWriter(dataFile)) {
                gson.toJson(d, w);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save daily data: " + e.getMessage());
        }
    }

    private static class DailySaveData {
        boolean dailyEnabled = true;
        boolean weeklyEnabled = true;
        long dailyGeneratedAt;
        long weeklyGeneratedAt;
        List<DailyQuestData> currentDaily = new ArrayList<>();
        List<DailyQuestData> currentWeekly = new ArrayList<>();
        Map<String, Map<String, Integer>> progress = new HashMap<>();
        Map<String, List<String>> claimed = new HashMap<>();
    }
}