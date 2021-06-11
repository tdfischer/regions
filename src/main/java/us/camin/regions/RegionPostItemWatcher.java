package us.camin.regions;

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

import org.bukkit.event.Listener;
import org.bukkit.event.Event;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;

import us.camin.regions.ui.RegionPostBuilder;

import java.util.ArrayList;
import java.util.List;

public class RegionPostItemWatcher implements Listener {
    private RegionManager m_manager;
    private Plugin m_plugin;

    public RegionPostItemWatcher(Plugin plugin, RegionManager manager) {
      m_manager = manager;
      m_plugin = plugin;
    }

    static public ItemStack createCompass(Region r) {
      ItemStack stack = new ItemStack(Material.COMPASS);
      ItemMeta meta = stack.getItemMeta();
      List<String> lore = new ArrayList<String>();
      if (r == null) {
          lore.add("Right click to locate the nearest Region Post");
      } else {
          CompassMeta compassMeta = (CompassMeta)meta;
          compassMeta.setDisplayName(r.name() + " Region Compass");
          compassMeta.setLodestone(r.location());
          compassMeta.setLodestoneTracked(false);
          lore.add("Right click to locate the nearest Region Post");
          lore.add("Tracking: " + r.name());
          lore.add("Coordinates: " + r.location().getX() + ", " + r.location().getY());
      }
      meta.setLore(lore);
      meta.addEnchant(Enchantment.SOUL_SPEED, 1, true);
      meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      stack.setItemMeta(meta);
      return stack;
    }

    static public ItemStack createChargeItem() {
      ItemStack stack = new ItemStack(Material.GLOWSTONE_DUST);
      ItemMeta meta = stack.getItemMeta();
      List<String> lore = new ArrayList<String>();
      lore.add("Charges or repairs a region post");
      meta.setLore(lore);
      meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      meta.addEnchant(Enchantment.SOUL_SPEED, 1, true);
      meta.setDisplayName("Region Post Charge");
      stack.setItemMeta(meta);
      return stack;
    }

    static public ItemStack createCreateItem() {
      ItemStack stack = new ItemStack(Material.LANTERN);
      ItemMeta meta = stack.getItemMeta();
      List<String> lore = new ArrayList<String>();
      lore.add("Place to create a new region post");
      meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      meta.addEnchant(Enchantment.SOUL_SPEED, 1, true);
      meta.setLore(lore);
      stack.setItemMeta(meta);
      return stack;
    }

    private ItemStack m_theCompass = createCompass(null);
    private ItemStack m_theItem = createCreateItem();
    private static ItemStack m_theChargeItem = createChargeItem();

    public static boolean isChargeItem(ItemStack stack) {
        return stack.isSimilar(m_theChargeItem);
    }

    public boolean isRegionCompass(ItemStack stack) {
        if (stack.isSimilar(m_theCompass)) {
          return true;
        }

        if (stack.getType() == m_theItem.getType()) {
          ItemMeta meta = stack.getItemMeta();
          ItemMeta theItemMeta = m_theItem.getItemMeta();
          if (meta.getItemFlags() == theItemMeta.getItemFlags()) {
            return true;
          }
        }

        return false;
    }

    public boolean isRegionCreateItem(ItemStack stack, Player p) {
        if (stack.isSimilar(m_theItem)) {
          return true;
        }

        if (stack.getType() == m_theItem.getType()) {
          ItemMeta meta = stack.getItemMeta();
          ItemMeta theItemMeta = m_theItem.getItemMeta();
          if (meta.getLore().equals(theItemMeta.getLore())) {
            return true;
          }
        }

        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack handStack = event.getItem();
        if (handStack == null) {
          return;
        }
        ItemMeta meta = handStack.getItemMeta();

        if (isRegionCompass(handStack) && event.getAction() == Action.RIGHT_CLICK_AIR) {
          event.setCancelled(true);
          ItemStack compassItem = new ItemStack(Material.COMPASS);
          Region nearest = m_manager.nearestRegion(player.getLocation());
          if (nearest == null) {
            player.sendMessage("There are no regions in this world!");
            return;
          }
          compassItem = createCompass(nearest);
          player.setItemInHand(compassItem);
          player.sendMessage("Now tracking " + nearest.name());
        } else if (!event.isCancelled() && isRegionCreateItem(handStack, player) && event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getClickedBlock().getType().isInteractable()) {
          event.setUseItemInHand(Event.Result.DENY);
          event.setCancelled(true);
          if (meta.getDisplayName().equals("")) {
            player.sendMessage("You must first give this item a name!");
          } else {
            Region nearest = m_manager.nearestRegion(player.getLocation());
            if (nearest != null && player.getLocation().distance(nearest.interactLocation()) <= 500) {
              player.sendMessage("You are too close to the region post for " + nearest.name());
            } else {
              Region r = new Region(meta.getDisplayName(), event.getClickedBlock().getRelative(event.getBlockFace()).getLocation());
              m_plugin.getServer().getScheduler().runTask(m_plugin, () -> {
                RegionPostBuilder builder = new RegionPostBuilder(r, m_plugin);
                builder.build();
              });
              player.setItemInHand(handStack.subtract());
              m_plugin.regionManager().addRegion(r);
              m_plugin.saveRegions();
              player.sendMessage("You established the region "+r.coloredName());
            }
          }
        }
    }
}

