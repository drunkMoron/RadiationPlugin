package com.example.radiation;

import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;
import org.bukkit.Bukkit;

public class MobInfectionManager {
    private final RadiationPlugin plugin;
    private final Config config;
    private final Set<UUID> infectedMobs = new HashSet<>();

    public MobInfectionManager(RadiationPlugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        startInfectionUpdater();
    }

    private void startInfectionUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : new ArrayList<>(infectedMobs)) {
                    Entity entity = Bukkit.getEntity(uuid); // Get Entity, not LivingEntity
                    if (entity == null || !entity.isValid() || !(entity instanceof LivingEntity)) { // Corrected null check and instanceof
                        infectedMobs.remove(uuid);
                        continue;
                    }
                    LivingEntity livingEntity = (LivingEntity) entity; // Cast to LivingEntity
                    if (livingEntity.isDead() || !(livingEntity instanceof Monster)) {
                        infectedMobs.remove(uuid);
                        continue;
                    }
                    upgradeInfectedMob((Monster) livingEntity);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void infectMob(LivingEntity entity) {
        if (!(entity instanceof Monster)) return;

        infectedMobs.add(entity.getUniqueId());
        entity.getWorld().playSound(entity.getLocation(),
                Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 0.5f);

        // Базовые улучшения
        Monster mob = (Monster) entity;
        mob.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                Integer.MAX_VALUE,
                1,
                true,
                false
        ));

        // Настройки из конфига
        double healthMultiplier = config.getMobHealthMultiplier();
        if (mob.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                    .setBaseValue(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * healthMultiplier);
        }
    }

    private void upgradeInfectedMob(Monster mob) {
        // Агрессивное поведение
        if (mob.getTarget() == null) {
            mob.getWorld().getNearbyPlayers(mob.getLocation(), 15)
                    .stream()
                    .findFirst()
                    .ifPresent(mob::setTarget);
        }

        // Случайные эффекты
        if (Math.random() < 0.1) {
            mob.addPotionEffect(new PotionEffect(
                    PotionEffectType.STRENGTH,
                    100,
                    1,
                    true,
                    false
            ));
        }
    }
}