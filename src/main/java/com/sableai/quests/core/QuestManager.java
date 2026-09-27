package com.sableai.quests.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

public class QuestManager {
    private JavaPlugin plugin;
    private Map<String, Quest> quests;
    private Map<String, Set<String>> playerActiveQuests;
    private Map<String, Map<String, Integer>> playerProgress;
    private Map<String, Set<String>> completedQuests;
    private Object economy;
    private Gson gson;
    private File dataFile;
    private DailyManager dailyManager;

    public QuestManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.quests = new HashMap<>();
        this.playerActiveQuests = new HashMap<>();
        this.playerProgress = new HashMap<>();
        this.completedQuests = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFile = new File(plugin.getDataFolder(), "data.json");
        setupEconomy();
        loadData();
        this.dailyManager = new DailyManager(plugin, this);
    }

    private void setupEconomy() {
        try {
            Class<?> econClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(econClass);
            if (rsp != null) {
                economy = rsp.getProvider();
                plugin.getLogger().info("Vault economy found!");
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("Vault not found - economy disabled");
        }
    }

    public void createQuest(String questId, String name, String description,
                            Quest.QuestType type, int targetAmount) {
        if (quests.containsKey(questId)) return;
        quests.put(questId, new Quest(questId, name, description, type, targetAmount));
        // Clear stale completed references if this quest ID was completed in an old session
        for (Set<String> completed : completedQuests.values()) {
            completed.remove(questId);
        }
        saveData();
    }

    public void deleteQuest(String questId) {
        quests.remove(questId);
        for (Set<String> s : playerActiveQuests.values()) s.remove(questId);
        for (Set<String> s : completedQuests.values()) s.remove(questId);
        for (Map<String, Integer> m : playerProgress.values()) m.remove(questId);
        saveData();
    }

    public Quest getQuest(String questId) {
        return quests.get(questId);
    }

    public Map<String, Quest> getAllQuests() {
        return new HashMap<>(quests);
    }

    public void startQuest(Player player, String questId) {
        Quest quest = getQuest(questId);
        if (quest == null) {
            player.sendMessage("§cКвест не найден!");
            return;
        }

        String uuid = player.getUniqueId().toString();

        if (completedQuests.getOrDefault(uuid, new HashSet<>()).contains(questId)) {
            player.sendMessage("§cВы уже выполнили этот квест!");
            return;
        }

        if (playerActiveQuests.getOrDefault(uuid, new HashSet<>()).contains(questId)) {
            player.sendMessage("§cЭтот квест уже активен!");
            return;
        }

        playerActiveQuests.computeIfAbsent(uuid, k -> new HashSet<>()).add(questId);
        playerProgress.computeIfAbsent(uuid, k -> new HashMap<>()).put(questId, 0);
        player.sendMessage("§a✓ Квест начат: §e" + quest.getName());
        saveData();
    }

    public void completeQuest(Player player, String questId) {
        String uuid = player.getUniqueId().toString();
        Quest quest = getQuest(questId);

        if (quest == null) {
            player.sendMessage("§cКвест не найден!");
            return;
        }

        if (!playerActiveQuests.getOrDefault(uuid, new HashSet<>()).contains(questId)) {
            player.sendMessage("§cВы не начинали этот квест!");
            return;
        }

        int progress = playerProgress.getOrDefault(uuid, new HashMap<>()).getOrDefault(questId, 0);
        if (progress < quest.getTargetAmount()) {
            player.sendMessage("§cКвест ещё не выполнен! Прогресс: " + progress + "/" + quest.getTargetAmount());
            return;
        }

        QuestReward reward = quest.getReward();

        player.giveExp(reward.getXp());
        depositMoney(player, reward.getMoney());

        for (String itemData : reward.getItems()) {
            ItemStack item = QuestReward.stringToItem(itemData);
            if (item != null) {
                player.getInventory().addItem(item).values()
                        .forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
            }
        }

        for (String cmd : reward.getCommands()) {
            String parsed = cmd.replace("%player%", player.getName()).replaceFirst("^/", "");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }

        player.sendMessage("§a✓ Квест завершён!");
        player.sendMessage("§e+ " + reward.getMoney() + " денег");
        player.sendMessage("§e+ " + reward.getXp() + " опыта");

        playerActiveQuests.get(uuid).remove(questId);
        playerProgress.getOrDefault(uuid, new HashMap<>()).remove(questId);
        completedQuests.computeIfAbsent(uuid, k -> new HashSet<>()).add(questId);
        saveData();
    }

    public Set<String> getActiveQuests(Player player) {
        return playerActiveQuests.getOrDefault(player.getUniqueId().toString(), new HashSet<>());
    }

    public int getProgress(Player player, String questId) {
        return playerProgress.getOrDefault(player.getUniqueId().toString(), new HashMap<>())
                .getOrDefault(questId, 0);
    }

    public boolean isCompleted(Player player, String questId) {
        return completedQuests.getOrDefault(player.getUniqueId().toString(), new HashSet<>()).contains(questId);
    }

    public void addProgress(Player player, String questId, int amount) {
        String uuid = player.getUniqueId().toString();
        if (!playerActiveQuests.getOrDefault(uuid, new HashSet<>()).contains(questId)) return;
        if (completedQuests.getOrDefault(uuid, new HashSet<>()).contains(questId)) return;

        Quest quest = getQuest(questId);
        if (quest == null) return;

        int old = playerProgress.getOrDefault(uuid, new HashMap<>()).getOrDefault(questId, 0);
        int newVal = Math.min(old + amount, quest.getTargetAmount());
        playerProgress.computeIfAbsent(uuid, k -> new HashMap<>()).put(questId, newVal);

        player.sendMessage("§e" + quest.getName() + ": " + newVal + "/" + quest.getTargetAmount());

        if (newVal >= quest.getTargetAmount()) {
            player.sendMessage("§a✓ Квест выполнен! Откройте /quest и сдайте его в меню активных квестов.");
            saveData();
        }
    }

    private void depositMoney(Player player, int amount) {
        if (economy == null || amount <= 0) return;
        try {
            economy.getClass().getMethod("depositPlayer", Player.class, double.class)
                    .invoke(economy, player, (double) amount);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deposit money: " + e.getMessage());
        }
    }

    private void loadData() {
        if (!dataFile.exists()) return;
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, PlayerData>>() {}.getType();
            Map<String, PlayerData> all = gson.fromJson(reader, type);
            if (all == null) return;
            for (Map.Entry<String, PlayerData> e : all.entrySet()) {
                String uuid = e.getKey();
                PlayerData pd = e.getValue();

                // Load quest definitions
                if (pd.quests != null) {
                    for (QuestData qd : pd.quests) {
                        Quest quest = new Quest(qd.id, qd.name, qd.description, Quest.QuestType.valueOf(qd.type), qd.target);
                        quest.setTargetData(qd.targetData);
                        if (qd.reward != null) {
                            quest.getReward().setXp(qd.reward.xp);
                            quest.getReward().setMoney(qd.reward.money);
                            if (qd.reward.items != null)
                                qd.reward.items.forEach(quest.getReward()::addItem);
                            if (qd.reward.commands != null)
                                qd.reward.commands.forEach(quest.getReward()::addCommand);
                        }
                        quests.put(qd.id, quest);
                    }
                }

                if (pd.completed != null && !pd.completed.isEmpty())
                    completedQuests.put(uuid, new HashSet<>(pd.completed));
                if (pd.active != null && !pd.active.isEmpty()) {
                    playerActiveQuests.put(uuid, new HashSet<>(pd.active));
                    for (String qId : pd.active) {
                        int prog = pd.progress != null ? pd.progress.getOrDefault(qId, 0) : 0;
                        playerProgress.computeIfAbsent(uuid, k -> new HashMap<>()).put(qId, prog);
                    }
                }
            }
            plugin.getLogger().info("Loaded " + quests.size() + " quests, " + all.size() + " player records");

            // Clean up stale references — remove completed/active for quests that don't exist
            completedQuests.values().forEach(s -> s.removeIf(id -> !quests.containsKey(id)));
            playerActiveQuests.values().forEach(s -> s.removeIf(id -> !quests.containsKey(id)));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load data: " + e.getMessage());
        }
    }

    public void saveData() {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            Set<String> allUuids = new HashSet<>();
            allUuids.addAll(completedQuests.keySet());
            allUuids.addAll(playerActiveQuests.keySet());
            Map<String, PlayerData> all = new HashMap<>();

            // Save quest definitions under first UUID (or dedicated key)
            PlayerData global = new PlayerData();
            global.quests = new ArrayList<>();
            for (Quest q : quests.values()) {
                QuestData qd = new QuestData();
                qd.id = q.getQuestId();
                qd.name = q.getName();
                qd.description = q.getDescription();
                qd.type = q.getType().name();
                qd.target = q.getTargetAmount();
                qd.targetData = q.getTargetData();

                RewardData rd = new RewardData();
                rd.xp = q.getReward().getXp();
                rd.money = q.getReward().getMoney();
                rd.items = new ArrayList<>(q.getReward().getItems());
                rd.commands = new ArrayList<>(q.getReward().getCommands());
                qd.reward = rd;

                global.quests.add(qd);
            }
            all.put("__quests__", global);

            for (String uuid : allUuids) {
                PlayerData pd = new PlayerData();
                pd.completed = new ArrayList<>(completedQuests.getOrDefault(uuid, new HashSet<>()));
                pd.active = new ArrayList<>(playerActiveQuests.getOrDefault(uuid, new HashSet<>()));
                pd.progress = new HashMap<>(playerProgress.getOrDefault(uuid, new HashMap<>()));
                all.put(uuid, pd);
            }
            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(all, writer);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save data: " + e.getMessage());
        }
    }

    public DailyManager getDailyManager() {
        return dailyManager;
    }

    public void giveReward(Player player, int xp, int money) {
        player.giveExp(xp);
        depositMoney(player, money);
    }

    private static class PlayerData {
        List<String> completed = new ArrayList<>();
        List<String> active = new ArrayList<>();
        Map<String, Integer> progress = new HashMap<>();
        List<QuestData> quests;
    }

    private static class QuestData {
        String id;
        String name;
        String description;
        String type;
        int target;
        String targetData;
        RewardData reward;
    }

    private static class RewardData {
        int xp;
        int money;
        List<String> items;
        List<String> commands;
    }
}