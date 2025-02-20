package com.example.radiation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin; // Добавлено!
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RadiationManager {
    private final JavaPlugin plugin;
    private final Config config;
    private final Map<UUID, Double> environmentRadiation = new HashMap<>();
    private final Map<UUID, Double> playerRadiation = new HashMap<>();
    private BukkitTask updateTask;
    private BukkitTask decayTask;
    private final Set<UUID> infectedPlayers = new HashSet<>();

    public RadiationManager(JavaPlugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void startTasks() {
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateRadiation(p);
                applyEffects(p, getPlayerRadiation(p));
                spawnAshParticles(p);
            }
            for (World world : Bukkit.getWorlds()) {
                world.getEntitiesByClass(LivingEntity.class).stream()
                        .filter(this::isEntityInfected)
                        .forEach(this::spawnAshParticlesForEntity);
            }
        }, 0L, 20L);

        decayTask = Bukkit.getScheduler().runTaskTimer(plugin, this::applyRadiationDecay, 0L, 20L);
    }

    private void spawnAshParticles(Player p) {
        if (getPlayerRadiation(p) > config.getParticleThreshold()) {
            Location loc = p.getLocation();
            World world = loc.getWorld();
            if (world != null) {
                world.spawnParticle(Particle.ASH, loc.clone().add(0, 0.5, 0), 5, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }

    private void spawnAshParticlesForEntity(LivingEntity entity) {
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.ASH, loc.clone().add(0, 0.5, 0), 3, 0.1, 0.1, 0.1, 0.01);
        }
    }

    private boolean isEntityInfected(LivingEntity entity) {
        return entity instanceof Player ? infectedPlayers.contains(entity.getUniqueId()) : true;
    }

    private void updateRadiation(Player p) {
        double envRad = calculateEnvironmentRadiation(p.getLocation());
        double inventoryRad = calculateInventoryRadiation(p);
        double protection = 1 - Math.min(0.9, getHazmatProtection(p));
        double totalRad = (envRad + inventoryRad) * protection;

        environmentRadiation.put(p.getUniqueId(), totalRad);
        playerRadiation.put(p.getUniqueId(),
                playerRadiation.getOrDefault(p.getUniqueId(), 0.0) +
                        (totalRad * config.getRadiationAbsorptionRate())
        );
        processInfection(p);
    }

    private double calculateEnvironmentRadiation(Location loc) {
        double total = 0;
        int radius = config.getCheckRadius();
        Map<Material, Double> radiationBlocks = config.getRadiationBlocks();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location checkLoc = loc.clone().add(x, y, z);
                    Material blockType = checkLoc.getBlock().getType();
                    if (radiationBlocks.containsKey(blockType)) {
                        double distance = checkLoc.distance(loc);
                        total += radiationBlocks.get(blockType) / (distance == 0 ? 1 : distance * distance);
                    }
                }
            }
        }
        return total;
    }

    private double calculateInventoryRadiation(Player p) {
        int redstoneCount = 0;
        for (ItemStack item : p.getInventory()) {
            if (item != null && item.getType() == Material.REDSTONE) {
                redstoneCount += item.getAmount();
            }
        }
        return redstoneCount * config.getInventoryRedstoneRadiation();
    }

    private void processInfection(Player source) {
        double sourceRadiation = getPlayerRadiation(source);
        if (sourceRadiation < config.getInfectionThreshold()) return;

        double radius = config.getInfectionRadius();
        double rate = config.getInfectionRate();
        Location sourceLoc = source.getLocation();

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.getUniqueId().equals(source.getUniqueId()) && sourceLoc.distance(target.getLocation()) <= radius) {
                double transferred = sourceRadiation * rate;
                playerRadiation.put(
                        target.getUniqueId(),
                        playerRadiation.getOrDefault(target.getUniqueId(), 0.0) + transferred
                );
                if (getPlayerRadiation(target) >= config.getInfectionThreshold()) {
                    infectedPlayers.add(target.getUniqueId());
                }
            }
        }
    }

    private void applyRadiationDecay() {
        String decayType = config.getDecayType();
        double decayRate = config.getDecayRate();
        double decayBase = config.getDecayBase();

        for (Player p : Bukkit.getOnlinePlayers()) {
            double current = playerRadiation.getOrDefault(p.getUniqueId(), 0.0);
            double newValue;

            switch (decayType.toLowerCase()) {
                case "exponential":
                    newValue = current * decayBase;
                    break;
                case "linear":
                default:
                    newValue = Math.max(0, current - decayRate);
            }

            playerRadiation.put(p.getUniqueId(), Math.max(0, newValue));
            if (getPlayerRadiation(p) < config.getInfectionThreshold()) {
                infectedPlayers.remove(p.getUniqueId());
            }
        }
    }

    public void handlePlayerDeath(Player p) {
        playerRadiation.put(p.getUniqueId(), 0.0);
        environmentRadiation.put(p.getUniqueId(), 0.0);
        infectedPlayers.remove(p.getUniqueId());
    }

    private void applyEffects(Player p, double radiation) {
        for (Map.Entry<Integer, List<String>> entry : config.getThresholds().entrySet()) {
            if (radiation >= entry.getKey()) {
                applyThresholdEffects(p, entry.getValue());
            }
        }
    }

    private void applyThresholdEffects(Player p, List<String> effects) {
        for (String effect : effects) {
            String[] parts = effect.split(" ");
            if (parts.length < 3) continue;

            PotionEffectType type = PotionEffectType.getByName(parts[0]);
            if (type == null) continue;

            int duration = Integer.parseInt(parts[1]) * 20;
            int amplifier = Integer.parseInt(parts[2]) - 1;

            p.addPotionEffect(new PotionEffect(type, duration, amplifier, true, false, true));
        }
    }

    public double getHazmatProtection(Player p) {
        int hazmatPieces = 0;
        for (ItemStack armor : p.getInventory().getArmorContents()) {
            if (armor != null && armor.hasItemMeta() &&
                    armor.getItemMeta().getPersistentDataContainer()
                            .has(RadiationPlugin.getKey("hazmat"))) {
                hazmatPieces++;
            }
        }
        return config.getHazmatProtection() * hazmatPieces;
    }

    public double getEnvironmentRadiation(Player p) {
        return environmentRadiation.getOrDefault(p.getUniqueId(), 0.0);
    }

    public double getPlayerRadiation(Player p) {
        return playerRadiation.getOrDefault(p.getUniqueId(), 0.0);
    }

    public void setEnvironmentRadiation(Player p, double value) {
        environmentRadiation.put(p.getUniqueId(), value);
    }

    public void setPlayerRadiation(Player p, double value) {
        playerRadiation.put(p.getUniqueId(), value);
    }

    public void reducePlayerRadiation(Player p, double amount) {
        double current = playerRadiation.getOrDefault(p.getUniqueId(), 0.0);
        playerRadiation.put(p.getUniqueId(), Math.max(0, current - amount));
    }

    public void stopTasks() {
        if (updateTask != null) updateTask.cancel();
        if (decayTask != null) decayTask.cancel();
    }
}