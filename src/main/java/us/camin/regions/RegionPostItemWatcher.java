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
import org.bukkit.ChatColor;
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
      lore.add("Right click to locate the nearest Region Post");
      if (r == null) {
      } else {
          CompassMeta compassMeta = (CompassMeta)meta;
          compassMeta.setDisplayName(ChatColor.DARK_PURPLE + "Region Compass (" + r.coloredName() + ChatColor.RESET + ChatColor.DARK_PURPLE + ")");
          compassMeta.setLodestone(r.location());
          compassMeta.setLodestoneTracked(false);
          lore.add("Tracking: " + r.coloredName());
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

    static public ItemStack createAnchor() {
      ItemStack stack = new ItemStack(Material.LANTERN);
      ItemMeta meta = stack.getItemMeta();
      List<String> lore = new ArrayList<String>();
      lore.add("Place to create a new region post");
      meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      meta.addEnchant(Enchantment.SOUL_SPEED, 1, true);
      meta.setLore(lore);
      meta.setDisplayName("Region Post Anchor");
      stack.setItemMeta(meta);
      return stack;
    }

    private ItemStack m_theCompass = createCompass(null);
    private ItemStack m_theAnchor = createAnchor();
    private static ItemStack m_theChargeItem = createChargeItem();

    public static boolean isChargeItem(ItemStack stack) {
        return stack.isSimilar(m_theChargeItem);
    }

    public boolean isRegionCompass(ItemStack stack) {
        if (stack.isSimilar(m_theCompass)) {
          return true;
        }

        if (stack.getType() == m_theCompass.getType()) {
          ItemMeta meta = stack.getItemMeta();
          ItemMeta theItemMeta = m_theCompass.getItemMeta();
          if (meta.getLore() != null && meta.getLore().get(0).equals(theItemMeta.getLore().get(0))) {
            return true;
          }
        }

        return false;
    }

    public boolean isRegionCreateItem(ItemStack stack, Player p) {
        if (stack.isSimilar(m_theAnchor)) {
          return true;
        }

        if (stack.getType() == m_theAnchor.getType()) {
          ItemMeta meta = stack.getItemMeta();
          ItemMeta theItemMeta = m_theAnchor.getItemMeta();
          if (meta.getLore() != null && meta.getLore().equals(theItemMeta.getLore())) {
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
          Region nearest = m_manager.nearestRegion(player.getLocation());
          if (nearest == null) {
            player.sendMessage("There are no regions in this world!");
            return;
          }
          ItemStack compassItem = createCompass(nearest);
          player.setItemInHand(compassItem);
          player.sendMessage("Now tracking " + nearest.name());
        } else if (!event.isCancelled() && isRegionCreateItem(handStack, player) && event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getClickedBlock().getType().isInteractable() && player.hasPermission("regions.create")) {
          event.setUseItemInHand(Event.Result.DENY);
          event.setCancelled(true);
          if (meta.getDisplayName().equals("") || meta.getDisplayName().equals("Region Post Anchor")) {
            player.sendMessage("You must first give this item a name!");
          } else {
            Region nearest = m_manager.nearestRegion(event.getClickedBlock().getLocation());
            if (nearest != null && event.getClickedBlock().getLocation().distance(nearest.interactLocation()) < 500) {
              int distance = 500 - (int)event.getClickedBlock().getLocation().distance(nearest.interactLocation());
              player.sendMessage("You are " + distance + " blocks too close to the region post for " + nearest.name() + ".");
            } else {
              Region r = new Region(meta.getDisplayName(), event.getClickedBlock().getRelative(event.getBlockFace()).getLocation());
              m_plugin.getServer().getScheduler().runTask(m_plugin, () -> {
                RegionPostBuilder builder = new RegionPostBuilder(r, m_plugin);
                builder.build();
              });
              handStack.setAmount(handStack.getAmount()-1);
              player.setItemInHand(handStack);
              m_plugin.regionManager().addRegion(r);
              m_plugin.saveRegions();
              player.sendMessage("You established the region "+r.coloredName());
            }
          }
        }
    }
}

