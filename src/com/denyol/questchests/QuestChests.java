package com.denyol.questchests;

import com.denyol.questchests.command.CommandQuestChest;
import com.denyol.questchests.event.ChestClick;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Created for QuestChests on 01/Jun/2017 by Denyol.
 */
public class QuestChests extends JavaPlugin implements Listener {

    final String username = getConfig().getString("database.username");
    final String password = getConfig().getString("database.password");
    final String url = "jdbc:" + getConfig().getString("database.url") + getConfig().getString("database.name");

    static Connection connection;
    private final Logger logger = this.getLogger();

    @Override
    public void onEnable()
    {

        getConfig().addDefault("database.username", "default");
        getConfig().addDefault("database.name", "default");
        getConfig().addDefault("database.url", "mysql://default:3306/");
        getConfig().addDefault("database.password", "password");
        getConfig().options().copyDefaults(true);
        saveConfig();

        setupDatabase();

        getServer().getPluginManager().registerEvents(new ChestClick(this), this);

        this.getCommand("questchest").setExecutor(new CommandQuestChest());

    }

    @Override
    public void onDisable()
    {
        try
        {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (Exception e)
        {
            logger.severe("Could not close database connection!");
            e.printStackTrace();
        }
    }

    private void setupDatabase()
    {

        logger.info("Attempting to connect to database!");

        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e)
        {
            logger.severe("JDBC Driver unavailable!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try
        {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e)
        {
            logger.severe("Unable to connect to a database!");
            getServer().getPluginManager().disablePlugin(this);
        }

        String mainTableQuery = "CREATE TABLE IF NOT EXISTS QuestChests(" +
                "`id` int(11) unsigned NOT NULL AUTO_INCREMENT, " +
                "`world` varchar(20) NOT NULL DEFAULT '', " +
                "`x` int(9) NOT NULL, " +
                "`y` int(6) NOT NULL, " +
                "`z` int(9) NOT NULL, " +
                "PRIMARY KEY (`id`)) DEFAULT CHARSET=utf8;";

        String playerTableQuery = "CREATE TABLE IF NOT EXISTS `QuestChestsPlayers`(" +
                "`id` int(11) unsigned NOT NULL," +
                "`uuid` varchar(36) NOT NULL DEFAULT '') DEFAULT CHARSET=utf8;";

        try (PreparedStatement mainStatement = connection.prepareStatement(mainTableQuery);
             PreparedStatement playerStatement = connection.prepareStatement(playerTableQuery))
        {
            mainStatement.executeUpdate();
            playerStatement.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
            logger.severe("Unable to create tables!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        logger.info("Connected successfully.");

    }

    public static Connection getConnection()
    {
        return connection;
    }
}
