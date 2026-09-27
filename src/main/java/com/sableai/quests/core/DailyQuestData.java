package com.sableai.quests.core;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DailyQuestData {

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public enum Period {
        DAILY, WEEKLY
    }

    private final Period period;
    private final String id;
    private final String name;
    private final Quest.QuestType type;
    private final String targetData;
    private final int targetAmount;
    private final Difficulty difficulty;
    private final int xpReward;

    public DailyQuestData(Period period, String id, String name, Quest.QuestType type,
                          String targetData, int targetAmount, Difficulty difficulty,
                          int xpReward) {
        this.period = period;
        this.id = id;
        this.name = name;
        this.type = type;
        this.targetData = targetData;
        this.targetAmount = targetAmount;
        this.difficulty = difficulty;
        this.xpReward = xpReward;
    }

    public Period getPeriod() { return period; }
    public String getId() { return id; }
    public String getName() { return name; }
    public Quest.QuestType getType() { return type; }
    public String getTargetData() { return targetData; }
    public int getTargetAmount() { return targetAmount; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getXpReward() { return xpReward; }

    public List<ItemStack> generateItemReward() {
        Random rnd = new Random();
        return switch (difficulty) {
            case EASY -> List.of(
                randomItem(rnd, 1, 3, Material.BREAD, Material.APPLE, Material.CARROT, Material.BONE)
            );
            case MEDIUM -> List.of(
                randomItem(rnd, 1, 2, Material.COOKED_BEEF, Material.GOLDEN_APPLE, Material.IRON_INGOT)
            );
            case HARD -> List.of(
                randomItem(rnd, 1, 1, Material.DIAMOND, Material.EMERALD, Material.ENDER_PEARL)
            );
        };
    }

    private ItemStack randomItem(Random rnd, int min, int max, Material... pool) {
        Material mat = pool[rnd.nextInt(pool.length)];
        int amount = min + rnd.nextInt(max - min + 1);
        return new ItemStack(mat, amount);
    }

    public static List<DailyQuestData> generateDaily() {
        return generate(Period.DAILY, 3);
    }

    public static List<DailyQuestData> generateWeekly() {
        return generate(Period.WEEKLY, 3);
    }

    private static final List<DailyQuestData> DAILY_POOL = Arrays.asList(
        new DailyQuestData(Period.DAILY, "daily_kill_zombie", "Охота на зомби", Quest.QuestType.KILL, "ZOMBIE", 20, Difficulty.EASY, 15),
        new DailyQuestData(Period.DAILY, "daily_kill_skeleton", "Охота на скелетов", Quest.QuestType.KILL, "SKELETON", 15, Difficulty.EASY, 15),
        new DailyQuestData(Period.DAILY, "daily_kill_spider", "Охота на пауков", Quest.QuestType.KILL, "SPIDER", 15, Difficulty.EASY, 15),
        new DailyQuestData(Period.DAILY, "daily_kill_creeper", "Охота на криперов", Quest.QuestType.KILL, "CREEPER", 10, Difficulty.MEDIUM, 30),
        new DailyQuestData(Period.DAILY, "daily_collect_iron", "Сбор железа", Quest.QuestType.COLLECT, "IRON_INGOT", 32, Difficulty.EASY, 12),
        new DailyQuestData(Period.DAILY, "daily_collect_gold", "Сбор золота", Quest.QuestType.COLLECT, "GOLD_INGOT", 16, Difficulty.MEDIUM, 25),
        new DailyQuestData(Period.DAILY, "daily_collect_bread", "Выпечка хлеба", Quest.QuestType.COLLECT, "BREAD", 20, Difficulty.EASY, 10)
    );

    private static final List<DailyQuestData> WEEKLY_POOL = Arrays.asList(
        new DailyQuestData(Period.WEEKLY, "weekly_kill_zombie", "Война с зомби", Quest.QuestType.KILL, "ZOMBIE", 100, Difficulty.MEDIUM, 100),
        new DailyQuestData(Period.WEEKLY, "weekly_kill_skeleton", "Война со скелетами", Quest.QuestType.KILL, "SKELETON", 80, Difficulty.MEDIUM, 100),
        new DailyQuestData(Period.WEEKLY, "weekly_kill_creeper", "Война с криперами", Quest.QuestType.KILL, "CREEPER", 50, Difficulty.HARD, 200),
        new DailyQuestData(Period.WEEKLY, "weekly_kill_enderman", "Охота на эндерменов", Quest.QuestType.KILL, "ENDERMAN", 30, Difficulty.HARD, 250),
        new DailyQuestData(Period.WEEKLY, "weekly_collect_diamond", "Бриллиантовая лихорадка", Quest.QuestType.COLLECT, "DIAMOND", 16, Difficulty.HARD, 250),
        new DailyQuestData(Period.WEEKLY, "weekly_collect_emerald", "Изумрудный урожай", Quest.QuestType.COLLECT, "EMERALD", 24, Difficulty.HARD, 200),
        new DailyQuestData(Period.WEEKLY, "weekly_collect_iron", "Железный запас", Quest.QuestType.COLLECT, "IRON_INGOT", 128, Difficulty.EASY, 75)
    );

    private static List<DailyQuestData> generate(Period period, int count) {
        List<DailyQuestData> pool = period == Period.DAILY ? DAILY_POOL : WEEKLY_POOL;
        List<DailyQuestData> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        return new ArrayList<>(shuffled.subList(0, Math.min(count, shuffled.size())));
    }
}