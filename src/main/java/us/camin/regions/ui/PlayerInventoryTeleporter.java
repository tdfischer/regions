package us.camin.regions.ui;

/**
 * This file is part of Regions
 *
 * Regions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Regions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Regions.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.ChatColor;
import org.bukkit.event.Event.Result;
import org.bukkit.DyeColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import us.camin.regions.Plugin;
import us.camin.regions.Region;
import us.camin.regions.RegionManager;
import us.camin.regions.events.PlayerPostInteractEvent;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.lang.Math;

public class PlayerInventoryTeleporter implements Listener {
    Map<Player, Inventory> m_playerInventories;
    Plugin m_plugin;
    RegionManager m_manager;

    public PlayerInventoryTeleporter(Plugin plugin, RegionManager manager) {
        m_plugin = plugin;
        m_manager = manager;
        m_playerInventories = new HashMap<>();
    }

    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        HumanEntity clicker = event.getWhoClicked();

        if (clicker instanceof Player) {
            Player player = (Player)clicker;
            Inventory neighborInv = m_playerInventories.get(player);
            if (neighborInv == null) {
                return;
            }

            if (event.getView().getTopInventory() == neighborInv) {
                event.setResult(Result.DENY);
                event.setCancelled(true);
            }

            if (event.getClickedInventory() == neighborInv) {
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                Material mat = event.getCurrentItem().getType();
                if (mat == Material.BEDROCK || mat == Material.COMPASS || mat == Material.LANTERN) {
                    return;
                }
                m_plugin.getServer().getScheduler().runTask(m_plugin, () -> event.getView().close());
                Region nearest = m_manager.nearestRegion(player.getLocation());

                final String selectedName = meta.getDisplayName();
                final Optional<Region> destination = m_plugin.regionManager().regionsForWorld(player.getLocation().getWorld())
                  .stream()
                  .filter((r) -> r.name().equals(selectedName))
                  .findFirst();
                if (destination.isPresent()) {
                    player.sendMessage("Teleporting you there now...");
                    nearest.addCharges(-1);
                    destination.get().addCharges(1);
                    m_plugin.getServer().getScheduler().runTask(m_plugin, () -> {
                      RegionPostBuilder builder = new RegionPostBuilder(nearest, m_plugin);
                      builder.updateLantern();
                    });
                    m_plugin.saveRegions();
                    new PlayerTeleporter(player, nearest.teleportLocation(), destination.get().teleportLocation(), m_plugin).teleport().thenRun(() -> {
                        RegionPostBuilder builder = new RegionPostBuilder(destination.get(), m_plugin);
                        builder.updateLantern();
                    });
                    if (nearest.charges() == 0) {
                        nearest.location().getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, (float)1.0, (float)1.0);
                        //player.sendMessage("You've consumed this region post's last charge! You won't be able to return without recharging it.");
                    }
                } else {
                    player.sendMessage("There is no region with that name. This is a bug.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerPostInteractEvent event) {
        Collection<Region> nearby = m_manager.neighborsForRegion(event.region);
        Collection<Region> hubs = m_manager.worldHubs(event.region.location().getWorld());
        if (!m_playerInventories.containsKey(event.player)) {
            m_playerInventories.put(event.player, Bukkit.createInventory(null, InventoryType.CHEST, "Nearby Regions"));
        }
        Inventory neighborInventory = m_playerInventories.get(event.player);
        neighborInventory.clear();

        ArrayList<Region> sorted = new ArrayList<Region>();
        ArrayList<String> foundNames = new ArrayList<String>();
        Region nearest = m_manager.nearestRegion(event.player.getLocation());
        for(Region r : nearby) {
            if (!r.name().equals(nearest.name())) {
              foundNames.add(r.name());
              sorted.add(r);
            }
        }
        for(Region r : hubs) {
            if (!foundNames.contains(r.name()) && !r.name().equals(nearest.name())) {
              foundNames.add(r.name());
              sorted.add(r);
            }
        }
        sorted.sort((Region a, Region b) -> Integer.compare(b.visits(), a.visits()));

        for(Region region : sorted) {
            boolean isHub = hubs.contains(region);
            boolean visible = isHub || region.seenByPlayer(event.player) || event.player.hasPermission("regions.bypass.discovery");
            ItemStack item;
            if (visible) {
              item = region.icon();
            } else {
              item = new ItemStack(Material.BEDROCK);
            }
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(region.name());
            ArrayList<String> lore = new ArrayList<String>();
            Location center = region.location();
            int altitude = center.getBlockY();
            if (visible) {
              if (isHub) {
                lore.add("" + ChatColor.GOLD + ChatColor.BOLD + "World hub" + ChatColor.GOLD + " - Accessable from anywhere");
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addEnchant(Enchantment.SOUL_SPEED, 1, true);
              }
              lore.add(ChatColor.WHITE + "Distance: " + ChatColor.YELLOW + Math.round(region.location().distance(event.region.location())));
              lore.add(ChatColor.WHITE + "Population: " + ChatColor.YELLOW + m_plugin.playerWatcher().playersInRegion(region).size());
              lore.add(ChatColor.WHITE + "Altitude: " + ChatColor.YELLOW + altitude);
              Collection<Region> neighborNeighbors = m_manager.neighborsForRegion(region);
              ArrayList<String> neighborNames = new ArrayList<String>();
              for(Region r : neighborNeighbors) {
                if (!foundNames.contains(r.name())) {
                    neighborNames.add(r.coloredName());
                }
              }
              lore.add("Nearby connections: " + String.join(", ", neighborNames));
            } else {
              lore.add("" + ChatColor.BOLD + ChatColor.GOLD + "You haven't discovered this location yet!");
              lore.add(ChatColor.WHITE + "Distance: " + ChatColor.YELLOW + ChatColor.MAGIC + Math.round(region.location().distance(event.region.location())));
              lore.add(ChatColor.WHITE + "Population: " + ChatColor.YELLOW + ChatColor.MAGIC + m_plugin.playerWatcher().playersInRegion(region).size());
              lore.add(ChatColor.WHITE + "Altitude: " + ChatColor.YELLOW + ChatColor.MAGIC + altitude);
              lore.add(ChatColor.GOLD + "Coordinates: " + ChatColor.YELLOW + region.location().getX() + ", "+ region.location().getZ());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            neighborInventory.addItem(item);
        }
        ItemStack chargesItem = new ItemStack(event.region.charges() == 0 ? Material.SOUL_LANTERN : Material.LANTERN);
        ItemMeta meta = chargesItem.getItemMeta();
        meta.setDisplayName(ChatColor.BOLD + "Region Post Configuration");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.WHITE + "Name: "+event.region.coloredName());
        lore.add(ChatColor.WHITE + "Visits: " + ChatColor.YELLOW + event.region.visits());
        lore.add(ChatColor.WHITE + "Charges remaining: " + (event.region.charges() == 0 ? ChatColor.RED : ChatColor.GREEN) + event.region.charges());
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.SOUL_SPEED, 1, true);
        chargesItem.setItemMeta(meta);
        // 22 Is the middle of the bottom row
        neighborInventory.setItem(22, chargesItem);

        event.player.openInventory(neighborInventory);
    }
}
