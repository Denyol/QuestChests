package com.denyol.questchests.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created for QuestChests on 03/Jun/2017 by Denyol.
 */
public class CommandQuestChest implements CommandExecutor
{

    public static List<UUID> registerChest = new ArrayList<>();
    public static List<UUID> deregisterChest = new ArrayList<>();


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args)
    {
        if(commandSender instanceof Player)
        {
            Player player = (Player) commandSender;

            if(args.length >= 1 && args[0].equalsIgnoreCase("register") && player.hasPermission("questchests.register"))
            {
                deregisterChest.remove(player.getUniqueId());
                registerChest.add(player.getUniqueId());
                player.sendMessage(ChatColor.AQUA + "Please right click the chest you wish to register.");
                return true;
            }
            else if(args.length >= 1 && args[0].equalsIgnoreCase("remove") && player.hasPermission("questchests.remove"))
            {
                registerChest.remove(player.getUniqueId());
                deregisterChest.add(player.getUniqueId());
                player.sendMessage(ChatColor.AQUA + "Please right click the chest you wish to de-register.");
                return true;
            }

            player.sendMessage(ChatColor.YELLOW + "---- QuestChests ----");
            player.sendMessage(ChatColor.AQUA + "/questchest register " + ChatColor.GOLD + "Registers a chest as a questchest.");
            player.sendMessage(ChatColor.AQUA + "/questchest remove " + ChatColor.GOLD + "De-registers a questchest.");

        }
        else
            commandSender.sendMessage(ChatColor.RED + "This command can only be executed in game!");
        return true;
    }
}
