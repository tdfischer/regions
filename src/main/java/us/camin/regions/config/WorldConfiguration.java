package us.camin.regions.config;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class WorldConfiguration implements ConfigurationSerializable {
    public Map<String, RegionConfiguration> regions = new HashMap<>();

    public WorldConfiguration(Map<String, Object> confSection) {
        Map<String, Object> regionConfigs = (Map<String, Object>)confSection.get("regions");
        for(String regionName : regionConfigs.keySet()) {
            regions.put(regionName, new RegionConfiguration((Map<String, Object>)regionConfigs.get(regionName)));
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        Map<String, Object> regionConfigs = new HashMap<>();
        for(String regionName : regions.keySet()) {
            regionConfigs.put(regionName, regions.get(regionName).serialize());
        }
        return ret;
    }
}