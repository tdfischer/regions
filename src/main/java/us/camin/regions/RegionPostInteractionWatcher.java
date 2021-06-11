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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.material.Banner;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.DyeColor;
import java.util.logging.Logger;

import us.camin.regions.ui.RegionPostBuilder;
import us.camin.regions.events.PlayerPostInteractEvent;
import us.camin.regions.events.PlayerAddRegionChargeEvent;

public class RegionPostInteractionWatcher implements Listener {
    private RegionManager m_manager;
    private Plugin m_plugin;
  Logger log = Logger.getLogger("Regions.RegionPostBuilder");

    public RegionPostInteractionWatcher(Plugin plugin, RegionManager manager) {
      m_manager = manager;
      m_plugin = plugin;
    }

    private final Material[] bannerTypes = {
      Material.WHITE_BANNER,
      Material.YELLOW_BANNER,
      Material.BLUE_BANNER,
      Material.BLACK_BANNER,
      Material.BROWN_BANNER,
      Material.CYAN_BANNER,
      Material.GREEN_BANNER,
      Material.LIGHT_BLUE_BANNER,
      Material.GRAY_BANNER,
      Material.LIGHT_GRAY_BANNER,
      Material.LIME_BANNER,
      Material.MAGENTA_BANNER,
      Material.ORANGE_BANNER,
      Material.PINK_BANNER,
      Material.PURPLE_BANNER,
      Material.RED_BANNER,
    };

    private boolean isBannerItem(ItemStack item) {
      for(Material mat : bannerTypes) {
        if (item.getType() == mat) {
          return true;
        }
      }
      return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack handStack = player.getItemInHand();
        ItemMeta meta = handStack.getItemMeta();
        Region nearest = m_manager.nearestRegion(player.getLocation());
        if (nearest == null) {
          return;
        }
        Block clickedBlock = event.getClickedBlock();
        Location interactRegion = nearest.interactLocation();
        Location lanternRegion = nearest.interactLocation().add(0, 1, 0);
        boolean isInteracted = false;
        if (clickedBlock != null) {
            isInteracted |= clickedBlock.getBlockKey() == interactRegion.toBlockKey();
            isInteracted |= clickedBlock.getBlockKey() == lanternRegion.toBlockKey();
            isInteracted |= nearest.interactLocation().add(1, 0, 0).toBlockKey() == clickedBlock.getBlockKey();
            isInteracted |= nearest.interactLocation().add(-1, 0, 0).toBlockKey() == clickedBlock.getBlockKey();
            isInteracted |= nearest.interactLocation().add(0, 0, 1).toBlockKey() == clickedBlock.getBlockKey();
            isInteracted |= nearest.interactLocation().add(0, 0, -1).toBlockKey() == clickedBlock.getBlockKey();
        }
        if (isInteracted) {
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            if (!player.hasPermission("regions.use")) {
                player.sendMessage("You cannot use region posts at this time.");
                return;
            }
            if (RegionPostItemWatcher.isChargeItem(handStack) && player.hasPermission("regions.charge")) {
              nearest.addCharges(1);
              m_plugin.getServer().getScheduler().runTask(m_plugin, () -> {
                RegionPostBuilder builder = new RegionPostBuilder(nearest, m_plugin);
                builder.updateLantern();
              });
              m_plugin.saveRegions();
              player.setItemInHand(handStack.subtract());
              m_plugin.getServer().getPluginManager().callEvent(new PlayerAddRegionChargeEvent(player, nearest));
            } else if (isBannerItem(handStack) && player.hasPermission("regions.setbanner")) {
              DyeColor bannerColor = DyeColor.getByDyeData(handStack.getData().getData());
              BannerMeta bannerMeta = (BannerMeta)meta;
              log.info("Setting banner color to " + bannerColor);
              nearest.setBannerPatterns(bannerMeta.getPatterns());
              nearest.setColor(bannerColor);
              m_plugin.saveRegions();
              player.sendMessage("You've updated the region post banner");
              m_plugin.getServer().getScheduler().runTask(m_plugin, () -> {
                RegionPostBuilder builder = new RegionPostBuilder(nearest, m_plugin);
                builder.build();
              });
            } else if (nearest.charges() > 0 || player.hasPermission("regions.bypass.charges")) {
              m_plugin.getServer().getPluginManager().callEvent(new PlayerPostInteractEvent(player, nearest));
            } else {
              player.sendMessage("This region post is not charged. Right click on it while holding cobblestone.");
            }
        }
    }
}
