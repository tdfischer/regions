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

import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.Tag;
import org.bukkit.block.data.Directional;
import org.bukkit.block.Banner;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitScheduler;
import java.util.logging.Logger;
import java.lang.Runnable;

import us.camin.regions.Region;
import us.camin.regions.Plugin;

import org.bukkit.Location;
import org.bukkit.Color;

public class RegionPostBuilder {
  Logger log = Logger.getLogger("Regions.RegionPostBuilder");
  Plugin m_plugin;
	Region m_region;
    public RegionPostBuilder(Region r, Plugin p) {
	    m_region = r;
      m_plugin = p;
    }

    private final Material[] bannerTypes = {
      Material.WHITE_WALL_BANNER,
      Material.YELLOW_WALL_BANNER,
      Material.BLUE_WALL_BANNER,
      Material.BLACK_WALL_BANNER,
      Material.BROWN_WALL_BANNER,
      Material.CYAN_WALL_BANNER,
      Material.GREEN_WALL_BANNER,
      Material.LIGHT_BLUE_WALL_BANNER,
      Material.GRAY_WALL_BANNER,
      Material.LIGHT_GRAY_WALL_BANNER,
      Material.LIME_WALL_BANNER,
      Material.MAGENTA_WALL_BANNER,
      Material.ORANGE_WALL_BANNER,
      Material.PINK_WALL_BANNER,
      Material.PURPLE_WALL_BANNER,
      Material.RED_WALL_BANNER,
    };

    boolean isBannerBlock(Block block) {
        return Tag.BANNERS.isTagged(block.getType());
    }

    /*void buildPad(Location center) {
        World world = m_region.location().getWorld();
        // Fill in the cobblestone pad
        for(int x = -1;x <= 1;x++) {
            for(int z = -1;z <= 1;z++) {
                Block b = world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z);
                if (b.getType() != Material.COBBLESTONE) {
                    b.breakNaturally();
                    b.setType(Material.COBBLESTONE);
                }
            }
        }
        world.playSound(center, Sound.BLOCK_STONE_PLACE, (float)1.0, (float)1.0);
        world.spawnParticle(Particle.REDSTONE, center, 1000, 1, 1, 1, m_region.dustOptions());
    }

    void buildPost(Location center) {
        World world = m_region.location().getWorld();
        BukkitScheduler scheduler = m_plugin.getServer().getScheduler();

        // Draw the glowstone base of the tower
        Block b = world.getBlockAt(center.getBlockX(), center.getBlockY() + 1, center.getBlockZ());
        if (b.getType() != Material.GLOWSTONE) {
            b.breakNaturally();
            b.setType(Material.GLOWSTONE);
            world.playSound(b.getLocation(), Sound.BLOCK_GLASS_PLACE, (float)1.0, (float)1.0);
            world.spawnParticle(Particle.REDSTONE, b.getLocation().add(0.5, 0.5, 0.5), 1000, 1, 1, 1, m_region.dustOptions());
        }
        Block markerBlock = world.getBlockAt(center.getBlockX(), center.getBlockY() + 2, center.getBlockZ());

        scheduler.runTaskLater(m_plugin,  () -> {
          if (markerBlock.getType() != m_region.blockMaterial()) {
            markerBlock.breakNaturally();
            markerBlock.setType(m_region.blockMaterial());
            world.playSound(b.getLocation(), Sound.BLOCK_WOOL_PLACE, (float)1.0, (float)1.0);
            world.spawnParticle(Particle.REDSTONE, markerBlock.getLocation().add(0.5, 0.5, 0.5), 1000, 1, 1, 1, m_region.dustOptions());
          }
        }, 15);

        scheduler.runTaskLater(m_plugin,  () -> {
          Block lanternBlock = markerBlock.getRelative(BlockFace.UP);
          Material lanternType = (m_region.charges() > 0) ? Material.LANTERN : Material.SOUL_LANTERN;
          if (lanternBlock.getType() != Material.LANTERN && lanternBlock.getType() != Material.SOUL_LANTERN) {
            lanternBlock.breakNaturally();
            world.playSound(center, Sound.BLOCK_LANTERN_PLACE, (float)1.0, (float)1.0);
            world.spawnParticle(Particle.REDSTONE, lanternBlock.getLocation().add(0.5, 0.5, 0.5), 1000, 1, 1, 1, m_region.dustOptions());
          }
          lanternBlock.setType(lanternType);
          Lantern lanternData = (Lantern)lanternBlock.getBlockData();
          lanternData.setHanging(false);
          lanternBlock.setBlockData(lanternData);
        }, 30);
    }*/

    public void build() {
        BukkitScheduler scheduler = m_plugin.getServer().getScheduler();
        scheduler.runTask(m_plugin, new PadBuilder());
        scheduler.runTaskLater(m_plugin, new PostBuilder(), 20);
        scheduler.runTaskLater(m_plugin, new BannerBuilder(), 20 * 2);
        scheduler.runTaskLater(m_plugin, new LanternBuilder(), 20 * 3);
    }

    public void updateLantern() {
        BukkitScheduler scheduler = m_plugin.getServer().getScheduler();
        scheduler.runTask(m_plugin, new LanternBuilder());
    }

    public class PadBuilder implements Runnable {
        public void run() {
            World world = m_region.location().getWorld();
            Location center = m_region.location().clone().add(0, -1, 0);
            // Fill in the cobblestone pad
            for(int x = -1;x <= 1;x++) {
                for(int z = -1;z <= 1;z++) {
                    Block b = world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z);
                    if (b.getType() != Material.COBBLESTONE) {
                        b.breakNaturally();
                        b.setType(Material.COBBLESTONE);
                    }
                }
            }
            world.playSound(center, Sound.BLOCK_STONE_PLACE, (float)1.0, (float)1.0);
            world.spawnParticle(Particle.REDSTONE, center, 1000, 1, 1, 1, m_region.dustOptions());
        }
    }

    public class PostBuilder implements Runnable {
        public void run() {
            World world = m_region.location().getWorld();
            BukkitScheduler scheduler = m_plugin.getServer().getScheduler();
            Location center = m_region.location().clone().add(0, -1, 0);

            // Draw the glowstone base of the tower
            Block b = world.getBlockAt(center.getBlockX(), center.getBlockY() + 1, center.getBlockZ());
            if (b.getType() != Material.GLOWSTONE) {
                b.breakNaturally();
                b.setType(Material.GLOWSTONE);
                world.playSound(b.getLocation(), Sound.BLOCK_GLASS_PLACE, (float)1.0, (float)1.0);
                world.spawnParticle(Particle.REDSTONE, b.getLocation().add(0.5, 0.5, 0.5), 1000, 1, 1, 1, m_region.dustOptions());
            }
            Block markerBlock = world.getBlockAt(center.getBlockX(), center.getBlockY() + 2, center.getBlockZ());

            scheduler.runTaskLater(m_plugin,  () -> {
              if (markerBlock.getType() != m_region.blockMaterial()) {
                markerBlock.breakNaturally();
                markerBlock.setType(m_region.blockMaterial());
                world.playSound(b.getLocation(), Sound.BLOCK_WOOL_PLACE, (float)1.0, (float)1.0);
                world.spawnParticle(Particle.REDSTONE, markerBlock.getLocation().add(0.5, 0.5, 0.5), 1000, 1, 1, 1, m_region.dustOptions());
              }
            }, 15);
        }
    }

    public class BannerBuilder implements Runnable {
        public void run() {
            World world = m_region.location().getWorld();
            Location center = m_region.location().clone().add(0, -1, 0);
            // Place the banners
            Block markerBlock = world.getBlockAt(center.getBlockX(), center.getBlockY() + 2, center.getBlockZ());
            BlockFace[] directions = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
            log.info("Rebuilding banners...");
            for(BlockFace face : directions) {
              Block b = markerBlock.getRelative(face);
              if (!isBannerBlock(b)) {
                b.breakNaturally();
              }
              b.setType(m_region.bannerBlockMaterial());
              Directional bannerDir = (Directional)b.getBlockData();
              bannerDir.setFacing(face);
              b.setBlockData(bannerDir);
              Banner bannerState = (Banner)b.getState();
              bannerState.setPatterns(m_region.bannerPatterns());
              bannerState.update();
              world.spawnParticle(Particle.CLOUD, markerBlock.getLocation().add(0.5, 0.5, 0.5), 10, 1, 1, 1);
            }
            world.playSound(center, Sound.BLOCK_WOOL_PLACE, (float)1.0, (float)1.0);
        }
    }

    public class LanternBuilder implements Runnable {
        public void run() {
              World world = m_region.location().getWorld();
              Location center = m_region.location().clone().add(0, -1, 0);
              Block markerBlock = world.getBlockAt(center.getBlockX(), center.getBlockY() + 2, center.getBlockZ());
              Block lanternBlock = markerBlock.getRelative(BlockFace.UP);
              Material lanternType = (m_region.charges() > 0) ? Material.LANTERN : Material.SOUL_LANTERN;
              if (lanternBlock.getType() != Material.LANTERN && lanternBlock.getType() != Material.SOUL_LANTERN) {
                lanternBlock.breakNaturally();
                world.playSound(center, Sound.BLOCK_LANTERN_PLACE, (float)1.0, (float)1.0);
                world.spawnParticle(Particle.REDSTONE, lanternBlock.getLocation().add(0.5, 0.5, 0.5), 1000, 1, 1, 1, m_region.dustOptions());
              }
              lanternBlock.setType(lanternType);
              Lantern lanternData = (Lantern)lanternBlock.getBlockData();
              lanternData.setHanging(false);
              lanternBlock.setBlockData(lanternData);
        }
    }
}
