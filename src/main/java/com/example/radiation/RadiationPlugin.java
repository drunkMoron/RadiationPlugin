package com.example.radiation;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.logging.Level;

public class RadiationPlugin extends JavaPlugin implements Listener {
    private RadiationManager radiationManager;
    private Config config;
    private final NamespacedKey hazmatKey = new NamespacedKey(this, "hazmat");

    @Override
    public void onEnable() {
        config = new Config(this);
        config.load();

        radiationManager = new RadiationManager(this, config);
        GeigerCounterListener geigerListener = new GeigerCounterListener(this, radiationManager, config);

        getServer().getPluginManager().registerEvents(geigerListener, this);
        getServer().getPluginManager().registerEvents(
                new PlayerDeathListener(radiationManager),
                this
        );
        getServer().getPluginManager().registerEvents(this, this); // Register the listener
        registerCommand("givegeiger", new GiveGeigerCommand(this));
        registerCommand("setradiation", new SetRadiationCommand(radiationManager));
        registerCommand("givepill", new GivePillCommand(this));
        registerCommand("givehazmat", new GiveHazmatCommand(this));

        radiationManager.startTasks();
        geigerListener.startActionBarUpdater();
        registerCustomRecipes();
    }

    @Override
    public void onDisable() {
        radiationManager.stopTasks();
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            getLogger().log(Level.SEVERE, "Command registration failed: " + name);
        }
    }

    private void registerCustomRecipes() {
        // Рецепт йодовой таблетки
        ItemStack pill = new ItemStack(Material.SUGAR);
        ItemMeta meta = pill.getItemMeta();
        meta.setDisplayName("§bЙодовая таблетка");
        meta.getPersistentDataContainer().set(
                getKey("anti_radiation_pill"),
                PersistentDataType.BYTE,
                (byte) 1
        );
        pill.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(
                new NamespacedKey(this, "iodine_pill"),
                pill
        );
        recipe.shape("BGS");
        recipe.setIngredient('B', Material.PAPER);
        recipe.setIngredient('G', Material.GOLD_NUGGET);
        recipe.setIngredient('S', Material.SUGAR);

        getServer().addRecipe(recipe);
    }

    public static NamespacedKey getKey(String key) {
        return new NamespacedKey(RadiationPlugin.getPlugin(RadiationPlugin.class), key);
    }

}