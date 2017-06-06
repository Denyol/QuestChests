package com.denyol.questchests.event;

import com.denyol.questchests.chestGUI.CollectItemsGUI;
import com.denyol.questchests.QuestChests;
import com.denyol.questchests.command.CommandQuestChest;
import com.denyol.questchests.database.DatabaseManager;
import com.denyol.questchests.util.HiddenStringUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created for QuestChests on 03/Jun/2017 by Denyol.
 */
public class ChestClick implements Listener
{

    private final QuestChests plugin;

    public ChestClick(QuestChests plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerOpenChest(PlayerInteractEvent event)
    {
        if(!event.isCancelled())
        {
            Block clicked = event.getClickedBlock();

            Player player = event.getPlayer();

            if((clicked.getType() == Material.CHEST || clicked.getType() == Material.TRAPPED_CHEST) && event.getAction() == Action.RIGHT_CLICK_BLOCK)
            {

                if(CommandQuestChest.registerChest.contains(player.getUniqueId()))
                {
                    if(DatabaseManager.isChestRegistered(clicked.getLocation(), plugin, player))
                        player.sendMessage(ChatColor.AQUA + "That chest is either already registered or something went wrong!");
                    else if(DatabaseManager.registerChest(clicked.getLocation(), plugin, player))
                        player.sendMessage(ChatColor.AQUA + "Your questchest has been registered!");
                    else
                        player.sendMessage(ChatColor.RED + "Something went wrong registering that questchest, please check the console log.");

                    CommandQuestChest.registerChest.remove(player.getUniqueId());
                    return;
                }
                else if(CommandQuestChest.deregisterChest.contains(player.getUniqueId()))
                {
                    player.sendMessage(ChatColor.AQUA + "Attempting to de-register this chest, please check the console for errors.");
                    DatabaseManager.deregisterChest(clicked.getLocation(), plugin);
                    CommandQuestChest.deregisterChest.remove(player.getUniqueId());
                    return;
                }

                int chestID = DatabaseManager.getChestID(clicked.getLocation(), plugin);

                boolean hasOpened = DatabaseManager.hasPlayerOpenedChest(chestID, plugin, player.getUniqueId());

                if(chestID > -1 && !hasOpened)
                {
                    event.setCancelled(true);
                }
                else if(hasOpened || chestID < 0)
                {
                    if(hasOpened)
                    {
                        player.sendMessage(ChatColor.RED + "You have already opened this Quest Chest!");
                        event.setCancelled(true);
                    }

                    return;
                }

                CollectItemsGUI.openChestForPlayer(player, clicked.getLocation());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        Inventory inventory = event.getInventory();

        if(inventory.getName().equals(CollectItemsGUI.getItemsGUI.getName()) && clicked != null && clicked.getType() == Material.DIAMOND)
        {
            String encodedData = clicked.getItemMeta().getLore().get(0);

            encodedData = HiddenStringUtil.extractHiddenString(encodedData);
            String[] locationEncoded = encodedData.split(":");

            if(locationEncoded.length != 4)
            {
                event.setCancelled(true);
                return;
            }

            Location chestLocation = new Location(plugin.getServer().getWorld(locationEncoded[0]), Integer.valueOf(locationEncoded[1]), Integer.valueOf(locationEncoded[2]), Integer.valueOf(locationEncoded[3]));

            int chestID = DatabaseManager.getChestID(chestLocation, plugin);

            if((chestLocation.getBlock().getType() == Material.CHEST || chestLocation.getBlock().getType() == Material.TRAPPED_CHEST) && chestID > -1)
            {
                Chest chest = (Chest) chestLocation.getBlock().getState();

                if(chest.getInventory().getContents() == null || chest.getInventory().getContents().length == 0)
                {
                    event.setCancelled(true);
                    return;
                }

                for(ItemStack item : chest.getInventory().getContents())
                {
                    if(item == null)
                        continue;
                    player.getInventory().addItem(item);
                }
                player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(1, Note.Tone.C));
                DatabaseManager.setPlayerOpened(chestID, plugin, QuestChests.getConnection(), player.getUniqueId());
                player.closeInventory();
            }

            event.setCancelled(true);
        }
    }

}
