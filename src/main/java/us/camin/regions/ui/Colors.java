package us.camin.regions.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public class Colors {
    public static ChatColor[] regionColors = {
      ChatColor.AQUA,
      ChatColor.BLACK,
      ChatColor.BLUE,
      ChatColor.BOLD,
      ChatColor.DARK_AQUA,
      ChatColor.DARK_BLUE,
      ChatColor.DARK_GRAY,
      ChatColor.DARK_GREEN,
      ChatColor.DARK_PURPLE,
      ChatColor.DARK_RED,
      ChatColor.GOLD,
      ChatColor.GRAY,
      ChatColor.GREEN,
      ChatColor.LIGHT_PURPLE,
      ChatColor.RED,
      ChatColor.WHITE,
      ChatColor.YELLOW,
    };

    public static ChatColor chatColorForName(String name) {
        int colorCount = regionColors.length;
        int hashed = Math.abs(name.hashCode());
        return regionColors[hashed % (colorCount - 1)];
    }

    public static ChatColor chatColorForColor(DyeColor color) {
        switch (color) {
            case BLACK:
                return ChatColor.BLACK;
            case BLUE:
                return ChatColor.BLUE;
            case BROWN:
                return ChatColor.DARK_RED;
            case CYAN:
                return ChatColor.AQUA;
            case GRAY:
                return ChatColor.GRAY;
            case GREEN:
                return ChatColor.GREEN;
            case LIGHT_BLUE:
                return ChatColor.BLUE;
            case LIGHT_GRAY:
                return ChatColor.GRAY;
            case LIME:
                return ChatColor.GREEN;
            case MAGENTA:
                return ChatColor.LIGHT_PURPLE;
            case ORANGE:
                return ChatColor.GOLD;
            case PINK:
                return ChatColor.RED;
            case PURPLE:
                return ChatColor.DARK_PURPLE;
            case RED:
                return ChatColor.RED;
            case WHITE:
                return ChatColor.WHITE;
            case YELLOW:
                return ChatColor.YELLOW;
        }
        return ChatColor.WHITE;
    }
}
