package com.example.radiation;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    private final RadiationManager manager;
    
    public PlayerDeathListener(RadiationManager manager) {
        this.manager = manager;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        manager.handlePlayerDeath(p);
    }
}