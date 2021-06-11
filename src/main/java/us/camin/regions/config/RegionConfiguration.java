package us.camin.regions.config;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.DyeColor;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;

import us.camin.regions.Region;
import java.util.UUID;

public class RegionConfiguration implements ConfigurationSerializable {
	public int x;
    public int y;
    public int z;
    public int visits;
    public int charges;
    public boolean isHub;
    public List<Pattern> patterns;
    public List<UUID> seenBy;
    public DyeColor color = DyeColor.YELLOW;

    public RegionConfiguration(Region region) {
        Location loc = region.location();
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        charges = region.charges();
        isHub = region.isHub();
        visits = region.visits();
        patterns = region.bannerPatterns();
        color = region.color();
        seenBy = region.seenPlayers();
    }

    public RegionConfiguration(Map<String, Object> confSection) {
        x = (Integer)confSection.get("x");
        y = (Integer)confSection.getOrDefault("y", -1);
        z = (Integer)confSection.get("z");
        isHub = (Boolean)confSection.getOrDefault("isHub", false);
        visits = (Integer)confSection.getOrDefault("visits", 0);
        charges = (Integer)confSection.getOrDefault("charges", 0);
        patterns = (List<Pattern>)confSection.getOrDefault("banner", new ArrayList<Pattern>());
        color = DyeColor.valueOf((String)confSection.getOrDefault("color", "YELLOW"));
        seenBy = new ArrayList<UUID>();
        List<String> strList = (List<String>)confSection.getOrDefault("seenBy", new ArrayList<String>());
        for(String s : strList) {
          seenBy.add(UUID.fromString(s));
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("x", x);
        ret.put("y", y);
        ret.put("z", z);
        ret.put("visits", visits);
        ret.put("charges", charges);
        ret.put("banner", patterns);
        List<String> strList = new ArrayList<String>();
        for(UUID uuid : seenBy) {
          strList.add(uuid.toString());
        }
        ret.put("seenBy", strList);
        ret.put("color", color.toString());
        ret.put("isHub", isHub);
        return ret;
    }
}
