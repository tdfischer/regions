package us.camin.regions.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;

import io.github.jdiemke.triangulation.DelaunayTriangulator;
import io.github.jdiemke.triangulation.NotEnoughPointsException;
import io.github.jdiemke.triangulation.Vector2D;
import io.github.jdiemke.triangulation.Triangle2D;

import us.camin.regions.Region;

public class BorderMesh {
    Logger log = Logger.getLogger("Regions.Geometry.BorderMesh");
    Collection<Region> m_regions;
    Map<Region, Polygon> m_polygons;
    Map<Region, Set<Region>> m_neighbors;

    // TODO: Probably need to cache the neighbors after doing all this
    // intersection work! Should be generated during triangulate()
    public Collection<Region> neighbors(Region region) {
        Collection<Region> ret = new ArrayList<Region>();
        if (m_neighbors.containsKey(region)) {
            Collection<Region> allNeighbors = m_neighbors.get(region);
            for(Region neighbor : allNeighbors) {
                int crossings = 0;
                Vector2D start = new Vector2D(region.location().getBlockX(), region.location().getBlockZ());
                Vector2D neighborEnd = new Vector2D(neighbor.location().getBlockX(), neighbor.location().getBlockZ());
                for(Region distantNeighbor : m_regions) {
                  if (distantNeighbor.equals(neighbor)) {
                    continue;
                  }
                  Polygon poly = m_polygons.get(distantNeighbor);
                  for(Polygon.Segment edge : poly.segments()) {
                    // Check if the line from region->neighbor intersects with
                    // any polygon
                    if (doIntersect(start, neighborEnd, edge.start, edge.end)) {
                      crossings++;
                    }
                  }
                }
                if (crossings == 1 || crossings == 0) {
                  ret.add(neighbor);
                }
            }
        }
        return ret;
    }

    public BorderMesh(Collection<Region> regions) {
        m_regions = regions;
        m_polygons = new HashMap<Region, Polygon>();
        m_neighbors = new HashMap<Region, Set<Region>>();
    }

    public class Polygon {
        public double x[];
        public double y[];
        public double z[];

        public class Segment {
          public Vector2D start;
          public Vector2D end;
          public Segment(Vector2D start, Vector2D end) {
            this.start = start;
            this.end = end;
          }
        }

        public List<Segment> segments() {
            List<Segment> ret = new ArrayList<Segment>();
            for(int i = 0; i < x.length-1; i++) {
              ret.add(new Segment(new Vector2D(x[i], z[i]), new Vector2D(x[i+1], z[i+1])));
            }
            ret.add(new Segment(new Vector2D(x[x.length-1], z[z.length-1]), new Vector2D(x[0], z[0])));
            return ret;
        }

        public Polygon(List<Vector2D> points) {
            x = new double[points.size()];
            y = new double[points.size()];
            z = new double[points.size()];

            for(int i = 0; i < points.size(); i++) {
                Vector2D borderPoint = points.get(i);
                if (Double.isNaN(borderPoint.x) || Double.isNaN(borderPoint.y)) {
                    continue;
                }
                //log.info("Coords " + borderPoint.x + ", " + borderPoint.y);
                x[i] = borderPoint.x;
                y[i] = 64;
                z[i] = borderPoint.y;
            }
        }

        public boolean contains(double testx, double testy) {
            return crossings(testx, testy) % 2 == 0;
        }

        public int crossings(double testx, double testy) {
          // winding rule algorithm
          int nvert = x.length;
          int i, j;
          int crossings = 0;
          for (i = 0, j = nvert-1; i < nvert; j = i++) {
              if ( ((z[i]>testy) != (z[j]>testy)) &&
                (testx < (x[j]-x[i]) * (testy-z[i]) / (z[j]-z[i]) + x[i]) )
                  crossings++;
          }
          return crossings;
        }
    }

    private int orientation(Vector2D p, Vector2D q, Vector2D r) {
      int val = (int)((q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y));
      if (val == 0) return 0; // colinear
      return (val > 0) ? 1 : 2; // Clockwise or counter-clockwise
    }

    private boolean doIntersect(Vector2D p1, Vector2D q1, Vector2D p2, Vector2D q2) {
      int o1 = orientation(p1, q1, p2);
      int o2 = orientation(p1, q1, q2);
      int o3 = orientation(p2, q2, p1);
      int o4 = orientation(p2, q2, q1);

      if (o1 != o2 && o3 != o4) {
        return true;
      }

      // It probably isn't possible to generate a route that is colinear with a
      // region border, so assume it isn't intersecting.
      return false;
    }

    public Polygon polygonForRegion(Region r) {
        return m_polygons.get(r);
    }

    public boolean triangulate() {
        // Create a list of points that we'll then use Delaunay on to form a mesh
        List<Vector2D> regionPoints = new ArrayList<Vector2D>();
        for (Region r : m_regions) {
          Location loc = r.location();
          Vector2D vec = new Vector2D(loc.getBlockX(), loc.getBlockZ());
          regionPoints.add(vec);
        }
        DelaunayTriangulator mesh = new DelaunayTriangulator(regionPoints);
        try {
          mesh.triangulate();
          log.info("Mesh triangulated!");
        } catch (NullPointerException e) {
          log.warning("Got a null pointer when triangulating, skipping world.");
          return false;
        } catch (NotEnoughPointsException e) {
          log.info("Not enough points to triangulate mesh, all regions will be connected to each other. Add more regions!");
          for(Region region : m_regions) {
            HashSet<Region> neighbors = new HashSet<Region>();
            for(Region neighbor : m_regions) {
              if (neighbor != region) {
                neighbors.add(neighbor);
              }
            }
            m_neighbors.put(region, neighbors);
          }
          return false;
        }

        ArrayList<Triangle> triangleSoup = new ArrayList<Triangle>();

        for(Region region : m_regions) {
          log.info("Creating region mesh for " + region.name());
          Location loc = region.location();
          Vector2D regionCenter = new Vector2D(loc.getBlockX(), loc.getBlockZ());

          for(Triangle2D tri : mesh.getTriangles()) {
            if (vecEquals(tri.a, regionCenter)) {
              triangleSoup.add(new Triangle(tri, region));
            } else if (vecEquals(tri.b, regionCenter)) {
              triangleSoup.add(new Triangle(tri, region));
            } else if (vecEquals(tri.c, regionCenter)) {
              triangleSoup.add(new Triangle(tri, region));
            }
          }
        }

        // Now we go back through our region mesh to generate vornoi points
        for(Region region : m_regions) {
          ArrayList<Vector2D> points = new ArrayList<Vector2D>();
          HashSet<Region> neighbors = new HashSet<Region>();
          log.info("Executing voronoi transform...");
          for(Triangle tri : triangleSoup) {
              if (tri.region == region) {
                  for (Region neighbor : m_regions) {
                      if (neighbor == region) {
                          continue;
                      }
                      Location neighborLoc = neighbor.location();
                      Vector2D neighborCenter = new Vector2D(neighborLoc.getBlockX(), neighborLoc.getBlockZ());
                      if (vecEquals(tri.a, neighborCenter) || vecEquals(tri.b, neighborCenter) || vecEquals(tri.c, neighborCenter)) {
                          neighbors.add(neighbor);
                      }
                  }
                  points.add(tri.circumcenter());
              }
          }

          // Sort points into a renderable polygon based on direction to center
          Location loc = region.location();
          Vector2D regionCenter = new Vector2D(loc.getBlockX(), loc.getBlockZ());
          points.sort((Vector2D a, Vector2D b) -> Double.compare(direction(a, regionCenter), direction(b, regionCenter)));
          log.info("Border for " + region.name() + " is defined by " + points.size() + " points");
          Polygon polygon = new Polygon(points);
          m_polygons.put(region, polygon);
          m_neighbors.put(region, neighbors);
        }
        return true;
    }

    /*public boolean validNeighbors(Region regionA, Region regionB) {
        Polygon polyA = polygonForRegion(regionA);
        Polygon polyB = polygonForRegion(regionA);
        Location center = region.location();
        if (poly.contains(center.getBlockX(), center.getBlockZ())) {
            return false;
        } else {
            return true;
        }
    }*/

    private boolean vecEquals(Vector2D a, Vector2D b) {
	    return a.x == b.x && a.y == b.y;
    }

    class Triangle {
        Vector2D a;
        Vector2D b;
        Vector2D c;
        Region region;

        public Triangle(Vector2D a, Vector2D b, Vector2D c, Region region) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.region = region;
        }

        public Triangle(Triangle2D tri, Region region) {
            this.a = tri.a;
            this.b = tri.b;
            this.c = tri.c;
            this.region = region;
        }

        public boolean equals(Triangle o) {
            return vecEquals(o.a, a) && vecEquals(o.b, b) && vecEquals(o.c, c);
        }

        private Vector2D midpoint(Vector2D a, Vector2D b) {
          return new Vector2D((a.x + b.x) / 2, (a.y + b.y) / 2);
        }

        private double slope(Vector2D from, Vector2D to) {
            return (to.y - from.y) / (to.x - from.x);
        }

        public Vector2D circumcenter() {
            Vector2D midAB = midpoint(a, b);
            Vector2D midBC = midpoint(b, c);

            double slopeAB = -1 / slope(a, b);
            double slopeBC = -1 / slope(b, c);

            double bAB = midAB.y - slopeAB * midAB.x;
            double bBC = midBC.y - slopeBC * midBC.x;

            double x = (bAB - bBC) / (slopeBC - slopeAB);

            return new Vector2D(x, (slopeAB * x) + bAB);
        }
    }

    private double direction(Vector2D a, Vector2D b) {
	    double dir = Math.atan2(a.y - b.y, a.x - b.x);
	    return dir;
    }
}
