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
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.PolyLineMarker;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.Marker;
import org.bukkit.event.EventHandler;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.logging.Logger;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import us.camin.regions.events.RegionCreateEvent;
import us.camin.regions.events.RegionRemoveEvent;
import us.camin.regions.geometry.BorderMesh;
import us.camin.regions.geometry.RegionSet;
import us.camin.regions.ui.Colors;

public class DynmapEventRelay implements Listener {
    Logger log = Logger.getLogger("Regions.DynmapEventRelay");
    private MarkerSet m_borderSet;
    private MarkerSet m_centerSet;
    private MarkerSet m_routesSet;
    private MarkerAPI m_api;
    private Map<World, List<GenericMarker>> m_borderMarkers;
    Plugin m_plugin;

    public DynmapEventRelay(Plugin plugin, MarkerAPI markerAPI) {
        m_plugin = plugin;
        m_api = markerAPI;
        m_centerSet = m_api.createMarkerSet("region-centers", "Region Posts", null, false);
        m_borderSet = m_api.createMarkerSet("region-borders", "Region Borders", null, false);
        m_routesSet = m_api.createMarkerSet("region-routes", "Region Routes", null, false);
        m_borderSet.setHideByDefault(true);
        m_routesSet.setHideByDefault(true);
        m_borderMarkers = new HashMap<World, List<GenericMarker>>();

        for(World world : plugin.getServer().getWorlds()) {
            Collection<Region> regions = m_plugin.regionManager().regionsForWorld(world);
            for (Region region : regions) {
                createMarkerForRegion(region);
            }
            updatePolygons(world);
        }
    }

    public void updatePolygons(World world) {
      List<GenericMarker> oldMarkers = m_borderMarkers.get(world);
      if (oldMarkers != null) {
          for(GenericMarker marker : oldMarkers) {
            marker.deleteMarker();
          }
      } else {
          m_borderMarkers.put(world, new ArrayList<GenericMarker>());
      }
	    log.info("Triangulating mesh for world...");

      RegionSet regions = m_plugin.regionManager().regionsForWorld(world);
      BorderMesh geom = regions.borders();

      for(Region region : regions) {
        BorderMesh.Polygon polygon = geom.polygonForRegion(region);
        if (polygon == null) {
            log.info("Could not generate polygon for region " + region.name());
            continue;
        }
        AreaMarker marker = m_borderSet.createAreaMarker(null, region.name(), false, world.getName(), polygon.x, polygon.z, false);
        marker.setFillStyle(0.7, region.color().getColor().asRGB());
        marker.setLineStyle(2, 0.8, region.color().getColor().asRGB());
        m_borderMarkers.get(world).add(marker);

        // Add a line between each region, for teleportations
        double thickness = Math.max(1, Math.log(geom.neighbors(region).size()) * 2.75);
        for(Region neighbor : geom.neighbors(region)) {
            double x[] = { neighbor.location().getBlockX(), region.location().getBlockX() };
            double y[] = { 64, 64 };
            double z[] = { neighbor.location().getBlockZ(), region.location().getBlockZ() };
            String label = neighbor.name() + " / " + region.name();
            String description = "<p>Travel Cost to " + neighbor.name() + ": " + region.getTravelCost(neighbor) + "</p>";
            description += "<p>Travel Cost to " + region.name() + ": " + neighbor.getTravelCost(region) + "</p>";
            PolyLineMarker routeMarker = m_routesSet.createPolyLineMarker(null, label, true, world.getName(), x, y, z, false);
            routeMarker.setDescription(description);
            routeMarker.setLineStyle((int)Math.ceil(thickness), 0.5, region.color().getColor().asRGB());
            m_borderMarkers.get(world).add(routeMarker);
        }
	    }
    }

    private void createMarkerForRegion(Region region) {
        Location loc = region.location();
        MarkerIcon icon = m_api.getMarkerIcon("compass");
        if (region.isHub()) {
          icon = m_api.getMarkerIcon("world");
        }
        CircleMarker circleMarker = m_routesSet.createCircleMarker(null, region.name(), false, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), 60, 60, false);
        Marker marker = m_centerSet.createMarker(null, region.name(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), icon, false);
        circleMarker.setFillStyle(0.75, region.color().getColor().asRGB());
        circleMarker.setLineStyle(0, 0, 0);
        String desc = "<h2>" + region.name() + "</h2>";
        if (region.isHub()) {
          desc += "<p><strong>World Hub</strong></p>";
        }
        marker.setDescription(desc);
        circleMarker.setDescription(desc);
    }

    @EventHandler
    public void onRegionEvent(RegionRemoveEvent event) {
        Marker marker = m_centerSet.findMarkerByLabel(event.region.name());
        marker.deleteMarker();
        updatePolygons(event.region.location().getWorld());
    }

    @EventHandler
    public void onRegionEvent(RegionCreateEvent event) {
        createMarkerForRegion(event.region);
        updatePolygons(event.region.location().getWorld());
    }
}
