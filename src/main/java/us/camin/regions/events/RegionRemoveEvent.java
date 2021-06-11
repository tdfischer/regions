package us.camin.regions.events;

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

import org.bukkit.event.HandlerList;

import us.camin.regions.Region;

public class RegionRemoveEvent extends RegionEvent {
    public static final HandlerList s_handlers = new HandlerList();

    public RegionRemoveEvent(Region region) {
        super(region);
    }

    public HandlerList getHandlers() {
        return s_handlers;
    }

    public static HandlerList getHandlerList() {
        return s_handlers;
    }
}

