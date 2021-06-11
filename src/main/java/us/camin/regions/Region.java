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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.banner.Pattern;
import org.bukkit.DyeColor;
import org.bukkit.Particle.DustOptions;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.material.MaterialData;

import us.camin.regions.config.RegionConfiguration;
import us.camin.regions.ui.Colors;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;

public class Region {
    private Location m_location;
    private String m_name;
    private List<Pattern> m_bannerPatterns = new ArrayList<Pattern>();
    private DyeColor m_color = DyeColor.values()[(int)(System.currentTimeMillis() % DyeColor.values().length)];
    private List<UUID> m_seenPlayers = new ArrayList<UUID>();
    private boolean m_isHub = false;

    public Region(String name, Location location) {
        m_location = location.toBlockLocation();
        m_name = name;
    }

    public Region(String name, Location location, int visits, int charges, DyeColor color) {
        m_location = location.toBlockLocation();
        m_name = name;
        m_visits = visits;
        m_charges = charges;
        m_color = color;
    }

    private static DyeColor[] defaultColors = {
      DyeColor.LIGHT_BLUE,
      DyeColor.BLACK,
      DyeColor.BLUE,
      DyeColor.CYAN,
      DyeColor.BLUE,
      DyeColor.GRAY,
      DyeColor.GREEN,
      DyeColor.PURPLE,
      DyeColor.RED,
      DyeColor.ORANGE,
      DyeColor.GRAY,
      DyeColor.GREEN,
      DyeColor.MAGENTA,
      DyeColor.RED,
      DyeColor.WHITE,
      DyeColor.YELLOW,
    };

    private DyeColor defaultColorForName(String name) {
        int colorCount = defaultColors.length;
        int hashed = Math.abs(name.hashCode());
        return defaultColors[hashed % (colorCount - 1)];
    }

    public Region(String name, World world, RegionConfiguration conf) {
    	if (conf.y == -1) {
        Location defaultLoc = new Location(world, conf.x, 64, conf.z);
        conf.y = world.getHighestBlockAt(defaultLoc).getY();
      }
    	m_name = name;
    	m_visits = conf.visits;
      m_charges = conf.charges;
      m_location = new Location(world, conf.x, conf.y, conf.z);
      m_bannerPatterns = conf.patterns;
      if (conf.color == null) {
        m_color = defaultColorForName(name);
      } else {
        m_color = conf.color;
      }
      m_seenPlayers = conf.seenBy;
      m_isHub = conf.isHub;
    }

    public boolean isHub() {
      return m_isHub;
    }

    public boolean seenByPlayer(OfflinePlayer p) {
      return m_seenPlayers.contains(p.getUniqueId());
    }

    public boolean markSeenByPlayer(OfflinePlayer p) {
      if (!m_seenPlayers.contains(p.getUniqueId())) {
          m_seenPlayers.add(p.getUniqueId());
          return true;
      }
      return false;
    }

    public List<UUID> seenPlayers() {
      return m_seenPlayers;
    }

    public List<Pattern> bannerPatterns() {
      return m_bannerPatterns;
    }

    public void setBannerPatterns(List<Pattern> p) {
      m_bannerPatterns = p;
    }

    public Location location() {
        return m_location;
    }

    public Location teleportLocation() {
        return m_location.clone().add(0.5, 3, 0.5);
    }

    public Location interactLocation() {
        return m_location.clone().add(0, 1, 0);
    }

    public void addCharges(int charges) {
      m_charges += charges;
    }

    int m_visits = 0;
    int m_charges = 0;

    public boolean shouldKeepLoaded() {
        return false;
    }

    public int visits() {
        return m_visits;
    }

    public int charges() {
        return m_charges;
    }

    public void addVisit() {
        m_visits++;
    }

    public String name() {
        return m_name;
    }

    public DyeColor color() {
        return m_color;
    }

    public void setColor(DyeColor color) {
        m_color = color;
    }

    /**
     * An alternative to Location.distance() which doesn't use floating point math.
     */
    public int distanceTo(Location loc) {
        return (int)m_location.distance(loc);
    }

    public String coloredName() {
        return Colors.chatColorForColor(m_color) + name() + ChatColor.RESET;
    }

    public DustOptions dustOptions() {
        return new DustOptions(m_color.getColor(), 1);
    }

    public Material bannerIconMaterial() {
        switch(m_color) {
            case BLACK:
              return Material.BLACK_BANNER;
            case BLUE:
              return Material.BLUE_BANNER;
            case BROWN:
              return Material.BROWN_BANNER;
            case CYAN:
              return Material.CYAN_BANNER;
            case GRAY:
              return Material.GRAY_BANNER;
            case GREEN:
              return Material.GREEN_BANNER;
            case LIGHT_BLUE:
              return Material.LIGHT_BLUE_BANNER;
            case LIGHT_GRAY:
              return Material.LIGHT_GRAY_BANNER;
            case LIME:
              return Material.LIME_BANNER;
            case MAGENTA:
              return Material.MAGENTA_BANNER;
            case ORANGE:
              return Material.ORANGE_BANNER;
            case PINK:
              return Material.PINK_BANNER;
            case PURPLE:
              return Material.PURPLE_BANNER;
            case RED:
              return Material.RED_BANNER;
            case WHITE:
              return Material.WHITE_BANNER;
            case YELLOW:
              return Material.YELLOW_BANNER;
            default:
              break;
        }
        return Material.YELLOW_BANNER;
    }

    public Material bannerBlockMaterial() {
        switch(m_color) {
            case BLACK:
              return Material.BLACK_WALL_BANNER;
            case BLUE:
              return Material.BLUE_WALL_BANNER;
            case BROWN:
              return Material.BROWN_WALL_BANNER;
            case CYAN:
              return Material.CYAN_WALL_BANNER;
            case GRAY:
              return Material.GRAY_WALL_BANNER;
            case GREEN:
              return Material.GREEN_WALL_BANNER;
            case LIGHT_BLUE:
              return Material.LIGHT_BLUE_WALL_BANNER;
            case LIGHT_GRAY:
              return Material.LIGHT_GRAY_WALL_BANNER;
            case LIME:
              return Material.LIME_WALL_BANNER;
            case MAGENTA:
              return Material.MAGENTA_WALL_BANNER;
            case ORANGE:
              return Material.ORANGE_WALL_BANNER;
            case PINK:
              return Material.PINK_WALL_BANNER;
            case PURPLE:
              return Material.PURPLE_WALL_BANNER;
            case RED:
              return Material.RED_WALL_BANNER;
            case WHITE:
              return Material.WHITE_WALL_BANNER;
            case YELLOW:
              return Material.YELLOW_WALL_BANNER;
            default:
              break;
        }
        return Material.YELLOW_WALL_BANNER;
    }

    public Material blockMaterial() {
        switch(m_color) {
            case BLACK:
              return Material.BLACK_WOOL;
            case BLUE:
              return Material.BLUE_WOOL;
            case BROWN:
              return Material.BROWN_WOOL;
            case CYAN:
              return Material.CYAN_WOOL;
            case GRAY:
              return Material.GRAY_WOOL;
            case GREEN:
              return Material.GREEN_WOOL;
            case LIGHT_BLUE:
              return Material.LIGHT_BLUE_WOOL;
            case LIGHT_GRAY:
              return Material.LIGHT_GRAY_WOOL;
            case LIME:
              return Material.LIME_WOOL;
            case MAGENTA:
              return Material.MAGENTA_WOOL;
            case ORANGE:
              return Material.ORANGE_WOOL;
            case PINK:
              return Material.PINK_WOOL;
            case PURPLE:
              return Material.PURPLE_WOOL;
            case RED:
              return Material.RED_WOOL;
            case WHITE:
              return Material.WHITE_WOOL;
            case YELLOW:
              return Material.YELLOW_WOOL;
            default:
              break;
        }
        return Material.YELLOW_WOOL;
    }

    public ItemStack icon() {
      ItemStack item = new ItemStack(bannerIconMaterial());
      BannerMeta bannerMeta = (BannerMeta)item.getItemMeta();
      bannerMeta.setPatterns(bannerPatterns());
      item.setItemMeta(bannerMeta);
      return item;
    }
}
