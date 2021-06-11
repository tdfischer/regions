package us.camin.regions;

import java.util.Random;
import org.bukkit.World;
import org.bukkit.Location;
import us.camin.regions.ui.RegionPostBuilder;

public class TestRegionGenerator {
    Plugin m_plugin;
    public TestRegionGenerator(Plugin plugin) {
        m_plugin = plugin;
    }

    public void generate() {
        String[] regionNames = {"Redstone", "Lapis", "Dwarf City", "Birchshire", "Channelside", "Coldwood", "Vincente", "East Redstone City", "Westernly", "Capital City"};
        Random rand = new Random();
        for(World w : m_plugin.getServer().getWorlds()) {
            for(String name : regionNames) {
                Location loc = new Location(w, rand.nextInt(800), 64, rand.nextInt(800));
                Region r = new Region(name, loc);
                m_plugin.regionManager().addRegion(r);
                m_plugin.getServer().getScheduler().runTask(m_plugin, () -> {
                  RegionPostBuilder builder = new RegionPostBuilder(r, m_plugin);
                  //builder.build();
                });
            }
        }
    }
}
