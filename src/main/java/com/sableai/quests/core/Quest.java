package com.sableai.quests.core;

import java.util.*;

public class Quest {
    private String questId;
    private String name;
    private String description;
    private QuestType type;
    private int targetAmount;
    private int currentProgress;
    private QuestReward reward;
    private List<String> requiredQuests;
    private boolean completed;
    private String targetData;

    public enum QuestType {
        KILL,
        COLLECT,
        LOCATION,
        DIALOG
    }

    public Quest(String questId, String name, String description,
                 QuestType type, int targetAmount) {
        this.questId = questId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.targetAmount = targetAmount;
        this.currentProgress = 0;
        this.reward = new QuestReward();
        this.requiredQuests = new ArrayList<>();
        this.completed = false;
    }

    // Getters
    public String getQuestId() { return questId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public int getTargetAmount() { return targetAmount; }
    public int getCurrentProgress() { return currentProgress; }
    public QuestReward getReward() { return reward; }
    public boolean isCompleted() { return completed; }
    public String getTargetData() { return targetData; }
    public List<String> getRequiredQuests() { return requiredQuests; }

    // Setters
    public void addProgress(int amount) {
        this.currentProgress += amount;
        if (currentProgress >= targetAmount) {
            this.completed = true;
        }
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setTargetData(String targetData) {
        this.targetData = targetData;
    }

    public void setReward(QuestReward reward) {
        this.reward = reward;
    }

    public void addRequiredQuest(String questId) {
        this.requiredQuests.add(questId);
    }

    // Проверка прогресса
    public int getProgressPercentage() {
        return (int) ((double) currentProgress / targetAmount * 100);
    }

    public boolean isInProgress() {
        return currentProgress > 0 && currentProgress < targetAmount;
    }

    @Override
    public String toString() {
        return "Quest{" +
                "id='" + questId + '\'' +
                ", name='" + name + '\'' +
                ", progress=" + currentProgress + "/" + targetAmount +
                ", type=" + type +
                '}';
    }
}