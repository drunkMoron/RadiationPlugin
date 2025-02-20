package com.example.radiation;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void load() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }
    
    // Радиационные блоки
    public Map<Material, Double> getRadiationBlocks() {
        Map<Material, Double> blocks = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("radiation-blocks");
        if(section != null) {
            for(String key : section.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if(mat != null) blocks.put(mat, section.getDouble(key));
            }
        }
        return blocks;
    }
    
    // Настройки сканирования
    public int getCheckRadius() {
        return config.getInt("check-radius", 10);
    }
    
    // Механика поглощения
    public double getRadiationAbsorptionRate() {
        return config.getDouble("absorption.rate", 0.1);
    }

    public double getMobHealthMultiplier() {
        return config.getDouble("mobs.health-multiplier", 2.0);
    }
    
    public double getMobInfectionChance() {
        return config.getDouble("mobs.infection-chance", 0.25);
    }
    
    public int getMobAggroRadius() {
        return config.getInt("mobs.aggro-radius", 15);
    }
    
    public double getParticleThreshold() {
        return config.getDouble("particleThreshold", 50.0);
    }

    // Интерфейс
    public String getDisplayFormat() {
        return config.getString("display.format", "&4☢ &eОкружение: &6%.1f &e| &cИгрок: &6%.1f &eзивертов");
    }
    
    // Хазмат-костюм
    public double getHazmatProtection() {
        return config.getDouble("hazmat.protection-per-piece", 0.225);
    }
    
    // Пороги эффектов
    public Map<Integer, List<String>> getThresholds() {
        Map<Integer, List<String>> thresholds = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("thresholds");
        if(section != null) {
            for(String key : section.getKeys(false)) {
                thresholds.put(Integer.parseInt(key), section.getStringList(key + ".effects"));
            }
        }
        return thresholds;
    }
    
    // Новые параметры
    public double getInventoryRedstoneRadiation() {
        return config.getDouble("sources.inventory.redstone", 15.0);
    }
    
    public double getInfectionRadius() {
        return config.getDouble("infection.radius", 5.0);
    }
    
    public double getInfectionRate() {
        return config.getDouble("infection.rate", 0.05);
    }
    
    public double getInfectionThreshold() {
        return config.getDouble("infection.threshold", 100.0);
    }

    public double getDecayRate() {
        return config.getDouble("decay.rate", 0.01); // Уменьшаем базовое значение
    }
    
    public String getDecayType() {
        return config.getString("decay.type", "exponential"); // Тип спада
    }
    
    public double getDecayBase() {
        return config.getDouble("decay.base", 0.99); // Основание для экспоненты
    }
}