package com.example.radiation;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GiveGeigerCommand implements CommandExecutor {
    private final RadiationPlugin plugin;
    
    public GiveGeigerCommand(RadiationPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return true;
        
        ItemStack geiger = new ItemStack(Material.CLOCK);
        ItemMeta meta = geiger.getItemMeta();
        meta.setDisplayName("§aСчетчик Гейгера");
        meta.getPersistentDataContainer().set(
            RadiationPlugin.getKey("geiger"), 
            PersistentDataType.BYTE, 
            (byte)1
        );
        geiger.setItemMeta(meta);
        
        ((Player)sender).getInventory().addItem(geiger);
        return true;
    }
}