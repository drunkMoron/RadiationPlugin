package com.example.radiation;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GivePillCommand implements CommandExecutor {
    private final RadiationPlugin plugin;

    public GivePillCommand(RadiationPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        ItemStack pill = new ItemStack(Material.SUGAR);
        ItemMeta meta = pill.getItemMeta();
        meta.setDisplayName("§bЙодовая таблетка");
        meta.getPersistentDataContainer().set(
                RadiationPlugin.getKey("anti_radiation_pill"),
                PersistentDataType.BYTE,
                (byte) 1
        );
        pill.setItemMeta(meta);

        ((Player) sender).getInventory().addItem(pill);
        sender.sendMessage("§aВы получили йодовую таблетку!");
        return true;
    }
}