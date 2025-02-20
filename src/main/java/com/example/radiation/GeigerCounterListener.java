package com.example.radiation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GeigerCounterListener implements Listener {
    private final JavaPlugin plugin;
    private final RadiationManager manager;
    private final Config config;
    
    public GeigerCounterListener(JavaPlugin plugin, RadiationManager manager, Config config) {
        this.plugin = plugin;
        this.manager = manager;
        this.config = config;
    }
    public void startActionBarUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : plugin.getServer().getOnlinePlayers()) {
                    if(hasGeiger(p)) {
                        double env = manager.getEnvironmentRadiation(p);
                        updateGeigerSound(p, env);
                        double playerRad = manager.getPlayerRadiation(p);
                        String message = String.format(config.getDisplayFormat(), env, playerRad);
                        p.sendActionBar(ChatColor.translateAlternateColorCodes('&', message));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        ItemStack item = e.getItem();
        if (isAntiRadiationPill(item)) {
            Player p = e.getPlayer();
            manager.reducePlayerRadiation(p, 50);
            item.setAmount(item.getAmount() - 1);
            p.sendMessage(ChatColor.GREEN + "Радиация снижена на 50 зивертов!");
        }
    }
    
    private boolean hasGeiger(Player p) {
        return isGeiger(p.getInventory().getItemInMainHand()) || 
               isGeiger(p.getInventory().getItemInOffHand());
    }
    
    private boolean isGeiger(ItemStack item) {
        return item != null && 
               item.hasItemMeta() &&
               item.getItemMeta().getPersistentDataContainer()
                   .has(RadiationPlugin.getKey("geiger"));
    }
    
    private boolean isAntiRadiationPill(ItemStack item) {
        return item != null &&
               item.hasItemMeta() &&
               item.getItemMeta().getPersistentDataContainer()
                   .has(RadiationPlugin.getKey("anti_radiation_pill"));
    }

    private final Map<UUID, Long> lastClickTime = new HashMap<>();

    private void updateGeigerSound(Player p, double radiation) {
        long now = System.currentTimeMillis();
        long last = lastClickTime.getOrDefault(p.getUniqueId(), 0L);
        
        // Частота щелчков: 1 щелчок/сек при 50 зивертах, 10/сек при 500+
        double interval = Math.max(50, 1000 - (radiation * 2));
        
        if(now - last > interval) {
            p.playSound(p.getLocation(), 
                Sound.BLOCK_NOTE_BLOCK_HAT, 
                0.8f, 
                (float) (0.5 + (radiation / 1000))
            );
            lastClickTime.put(p.getUniqueId(), now);
        }
    }
}