package com.example.radiation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetRadiationCommand implements CommandExecutor {
    private final RadiationManager manager;
    
    public SetRadiationCommand(RadiationManager manager) {
        this.manager = manager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length < 3) {
            sender.sendMessage("§cИспользуйте: /setradiation <игрок> <env|player> <значение>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) {
            sender.sendMessage("§cИгрок не найден!");
            return true;
        }
        
        try {
            String type = args[1].toLowerCase();
            double value = Double.parseDouble(args[2]);
            
            switch(type) {
                case "env":
                    manager.setEnvironmentRadiation(target, value);
                    break;
                case "player":
                    manager.setPlayerRadiation(target, value);
                    break;
                default:
                    sender.sendMessage("§cНеверный тип! Используйте env или player");
                    return true;
            }
            sender.sendMessage("§aЗначение установлено для " + target.getName());
        } catch(NumberFormatException e) {
            sender.sendMessage("§cНекорректное число!");
        }
        return true;
    }
}