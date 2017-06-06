package com.denyol.questchests.chestGUI;

import com.denyol.questchests.util.HiddenStringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created for QuestChests on 03/Jun/2017 by Denyol.
 */
public class CollectItemsGUI
{
    public static final Inventory getItemsGUI = Bukkit.createInventory(null, 9, ChatColor.BLUE + "QuestChest");

    public static void openChestForPlayer(Player player, Location chestLocation)
    {
        ItemStack collectButton = new ItemStack(Material.DIAMOND, 1);
        List<String> lore = new ArrayList<>();
        String serialisedLocation = chestLocation.getWorld().getName() + ":" + chestLocation.getBlockX() + ":" + chestLocation.getBlockY() + ":" + chestLocation.getBlockZ();
        lore.add(HiddenStringUtil.encodeString(serialisedLocation));
        ItemMeta itemMeta = collectButton.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setDisplayName(ChatColor.GOLD + "Collect Items");
        collectButton.setItemMeta(itemMeta);

        getItemsGUI.setItem(4, collectButton);

        player.openInventory(getItemsGUI);
    }

}
