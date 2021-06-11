package us.camin.regions.geometry;

import us.camin.regions.Region;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.AbstractSet;
import java.util.Set;
import java.util.Iterator;

public class RegionSet extends AbstractSet<Region> {
    private Set<Region> m_regions = new HashSet<Region>();

    public Iterator<Region> iterator() {
        return m_regions.iterator();
    }

    public int size() {
        return m_regions.size();
    }

    public Region nearestRegion(Location loc) {
        Region nearest = null;
        int minDistance = -1;
        for(Region r : m_regions) {
            int check = r.distanceTo(loc);
            if (minDistance == -1 || check < minDistance) {
                nearest = r;
                minDistance = check;
            }
        }
        return nearest;
    }

    public boolean add(Region r) {
        boolean ret = m_regions.add(r);
        if (ret) {
            m_mesh = new BorderMesh(m_regions);
            m_mesh.triangulate();
        }
        return ret;
    }

    public boolean remove(Region r) {
        boolean ret = m_regions.remove(r);
        if (ret) {
            m_mesh = new BorderMesh(m_regions);
            m_mesh.triangulate();
        }
        return ret;
    }

    public BorderMesh borders() {
        return m_mesh;
    }

    BorderMesh m_mesh = null;
}
