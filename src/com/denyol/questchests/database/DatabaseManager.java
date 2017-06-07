package com.denyol.questchests.database;

import com.denyol.questchests.QuestChests;
import com.sun.istack.internal.NotNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created for QuestChests on 03/Jun/2017 by Denyol.
 */
public final class DatabaseManager
{

    public static boolean isChestRegistered(@NotNull Location chestLocation, QuestChests plugin, Player player)
    {

        int result = DatabaseManager.getChestID(chestLocation, plugin);

        if(result == -2)
        {
            plugin.getLogger().severe("Could not check if this chest is registered! Please contact an admin.");
            player.sendMessage(ChatColor.RED + "Could not execute query to check if chest is registered!");
            return true;
        }
        else if(result == -1)
            return false;
        else
            return true;
    }

    public static int getChestID(@NotNull Location chestLocation, QuestChests plugin)
    {
        String query = "SELECT `id` FROM `QuestChests` WHERE (`x` = '" + chestLocation.getBlockX()
                + "' AND `y` = '" + chestLocation.getBlockY()
                + "' AND `z` = '" + chestLocation.getBlockZ()
                + "' AND `world` = '" + chestLocation.getWorld().getName() + "') LIMIT 0,1;";

        try(PreparedStatement statement = QuestChests.getConnection().prepareStatement(query);
            ResultSet rs = statement.executeQuery(query))
        {
            if(rs.next())
                return rs.getInt("id");
            else
                return -1;
        }
        catch (SQLException e)
        {
            plugin.getLogger().severe("Could not get a quest chest ID from the database!");
            e.printStackTrace();
            return -2;
        }
    }

    public static boolean hasPlayerOpenedChest(int chestID, QuestChests plugin, @NotNull UUID playerUUID)
    {
        String query = "SELECT `uuid` FROM `QuestChestsPlayers` WHERE (`id` = '" + chestID + "') LIMIT 0,1;";

        try(PreparedStatement statement = QuestChests.getConnection().prepareStatement(query);
            ResultSet rs = statement.executeQuery(query))
        {
            if(rs.next())
                return true;
            else
                return false;
        }
        catch (SQLException e)
        {
            plugin.getLogger().severe("Could check if player " + playerUUID.toString() + " has opened chest " + chestID);
            e.printStackTrace();
            return true;
        }
    }

    public static void setPlayerOpened(int chestID, QuestChests plugin, @NotNull Connection databaseConnection, UUID playerUUID)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {

                String query = "INSERT INTO `QuestChestsPlayers` (`id`, `uuid`) VALUES ('" + chestID + "', '" + playerUUID.toString() + "');";

                try(PreparedStatement statement = databaseConnection.prepareStatement(query))
                {
                    statement.executeUpdate();
                }
                catch (SQLException e)
                {
                    plugin.getLogger().severe("Could not register player, " + playerUUID + "to chest, " + chestID);
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public static boolean registerChest(@NotNull Location chestLocation, QuestChests plugin, Player player)
    {

        String query = "INSERT INTO `QuestChests` (`world`, `x`, `y`, `z`) VALUES ('" + chestLocation.getWorld().getName()
                + "', '" + chestLocation.getBlockX()
                + "', '" + chestLocation.getBlockY()
                + "', '" + chestLocation.getBlockZ()
                + "');";

        try(PreparedStatement statement = QuestChests.getConnection().prepareStatement(query))
        {
            statement.executeUpdate();

            return true;
        }
        catch (SQLException e)
        {
            plugin.getLogger().severe("Could not register a questchest!");
            e.printStackTrace();
        }

        return false;
    }

    public static void deregisterChest(@NotNull Location chestLocation, QuestChests plugin)
    {
        String query = "DELETE FROM `QuestChests` WHERE (`world` = '"+ chestLocation.getWorld().getName()
                + "' AND `x` = '" + chestLocation.getBlockX()
                + "' AND `y` = '" + chestLocation.getBlockY()
                + "' AND `z` = '" + chestLocation.getBlockZ() + "');";

        int id = DatabaseManager.getChestID(chestLocation, plugin);

        String removePlayerEntriesQuery = "DELETE FROM `QuestChestsPlayers` WHERE (`id` = '" + id + "');";

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try(PreparedStatement statement = QuestChests.getConnection().prepareStatement(query);
                    PreparedStatement playerStmt = QuestChests.getConnection().prepareStatement(removePlayerEntriesQuery))
                {
                    statement.executeUpdate();
                    playerStmt.executeUpdate();
                }
                catch (SQLException e)
                {
                    plugin.getLogger().severe("Could not register a questchest!");
                    e.printStackTrace();
                }
            }

        }.runTaskAsynchronously(plugin);

        plugin.getLogger().info("Deregistering a chest in world: " + chestLocation.getWorld().getName());
    }

}
