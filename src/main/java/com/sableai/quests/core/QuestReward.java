package com.sableai.quests.core;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class QuestReward implements ConfigurationSerializable {

    private int money;
    private int xp;
    private List<String> items;
    private List<String> commands;

    public QuestReward() {
        this.money = 0;
        this.xp = 0;
        this.items = new ArrayList<>();
        this.commands = new ArrayList<>();
    }

    public QuestReward(int money, int xp) {
        this();
        this.money = money;
        this.xp = xp;
    }

    public int getMoney() { return money; }
    public int getXp() { return xp; }
    public List<String> getItems() { return items; }
    public List<String> getCommands() { return commands; }

    public void setMoney(int money) { this.money = money; }
    public void setXp(int xp) { this.xp = xp; }
    public void addItem(String item) { this.items.add(item); }
    public void clearItems() { this.items.clear(); }
    public void addCommand(String command) { this.commands.add(command); }
    public void clearCommands() { this.commands.clear(); }

    public static String itemToString(ItemStack item) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos);
            oos.writeObject(item);
            oos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public static ItemStack stringToItem(String data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream ois = new BukkitObjectInputStream(bais);
            ItemStack item = (ItemStack) ois.readObject();
            ois.close();
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("money", money);
        map.put("xp", xp);
        map.put("items", items);
        map.put("commands", commands);
        return map;
    }

    public static QuestReward deserialize(Map<String, Object> map) {
        QuestReward r = new QuestReward();
        r.money = (int) map.getOrDefault("money", 0);
        r.xp = (int) map.getOrDefault("xp", 0);
        r.items = (List<String>) map.getOrDefault("items", new ArrayList<>());
        r.commands = (List<String>) map.getOrDefault("commands", new ArrayList<>());
        return r;
    }

    @Override
    public String toString() {
        return "Reward{money=" + money + ", xp=" + xp + ", items=" + items.size() + ", commands=" + commands.size() + "}";
    }
}