package com.example.radiation;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Color;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class GiveHazmatCommand implements CommandExecutor {
    private final RadiationPlugin plugin;

    public GiveHazmatCommand(RadiationPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player p = (Player) sender;
        p.getInventory().addItem(
            createHazmatPiece(Material.LEATHER_HELMET, "Хазмат-шлем"),
            createHazmatPiece(Material.LEATHER_CHESTPLATE, "Хазмат-костюм"),
            createHazmatPiece(Material.LEATHER_LEGGINGS, "Хазмат-штаны"),
            createHazmatPiece(Material.LEATHER_BOOTS, "Хазмат-ботинки")
        );
        
        p.sendMessage("§aВы получили полный комплект хазмата!");
        return true;
    }

    private ItemStack createHazmatPiece(Material material, String name) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setDisplayName("§e" + name);
        meta.setColor(Color.YELLOW); // Изменяет цвет брони на желтый
        meta.getPersistentDataContainer().set(
            RadiationPlugin.getKey("hazmat"),
            PersistentDataType.BYTE,
            (byte)1
        );
        item.setItemMeta(meta);
        return item;
    }
}