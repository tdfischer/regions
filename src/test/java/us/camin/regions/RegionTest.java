package us.camin.regions;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.junit.Test;
import org.mockito.Mockito;

import us.camin.regions.config.RegionConfiguration;

public class RegionTest {

	private World world = Mockito.mock(World.class);

	@Test
	public void testHubToHubCost() {
		Map<String, Object> cfg = buildBaseConfig();
		cfg.put("isHub", true);

		Region r1 = new Region("R1", world, new RegionConfiguration(cfg));

		cfg.put("x", 1000);
		Region r2 = new Region("R2", world, new RegionConfiguration(cfg));

		assertEquals("hub to hub travel cost should be zero", 0, r1.getTravelCost(r2));
	}

	@Test
	public void testHubDestinationCost() {
		Map<String, Object> cfg = buildBaseConfig();
		cfg.put("isHub", true);

		Region hub = new Region("hub", world, new RegionConfiguration(cfg));

		cfg.put("isHub", false);
		cfg.put("x", 1000);
		Region region = new Region("region", world, new RegionConfiguration(cfg));

		assertEquals("Traveling to a hub should be half as expensive", 1, region.getTravelCost(hub));
	}

	@Test
	public void testHubSourceCost() {
		Map<String, Object> cfg = buildBaseConfig();
		cfg.put("isHub", true);

		Region hub = new Region("hub", world, new RegionConfiguration(cfg));

		cfg.put("isHub", false);
		cfg.put("x", 1000);
		Region region = new Region("region", world, new RegionConfiguration(cfg));

		assertEquals("leaving a hub should always cost 1", 1, hub.getTravelCost(region));
	}

	@Test
	public void testRegionCost() {
		Map<String, Object> cfg = buildBaseConfig();

		Region r1 = new Region("r1", world, new RegionConfiguration(cfg));
		cfg.put("x", 1000);
		Region r2 = new Region("r2", world, new RegionConfiguration(cfg));

		assertEquals("travel cost should be 1xp per 500 blocks", 2, r1.getTravelCost(r2));
	}

	private static Map<String, Object> buildBaseConfig() {
		Map<String, Object> cfg = new HashMap<>();
		cfg.put("x", 0);
		cfg.put("y", 0);
		cfg.put("z", 0);
		return cfg;
	}
}
