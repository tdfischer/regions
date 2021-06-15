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
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import java.util.logging.Logger;

import us.camin.regions.events.PlayerMoveInEvent;
import us.camin.regions.events.PlayerNearRegionPostEvent;
import us.camin.regions.events.PlayerRegionChangeEvent;
import us.camin.regions.events.PlayerAddRegionChargeEvent;
import us.camin.regions.ui.RegionPostBuilder;

import java.util.Collection;

public class PlayerNotifier implements Listener {
    Logger log = Logger.getLogger("Regions.PlayerNotifier");
    RegionManager m_manager;
    Plugin m_plugin;
    public PlayerNotifier(Plugin plugin, RegionManager manager) {
        m_manager = manager;
        m_plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerRegionChanged(PlayerRegionChangeEvent event) {
        if (event.oldRegion != null) {
            for (Player p : m_plugin.playerWatcher().playersInRegion(event.oldRegion)) {
                p.sendMessage(event.player.getName()+" has left the region.");
            }
        }
        for (Player p : m_plugin.playerWatcher().playersInRegion(event.newRegion)) {
            if (p != event.player) {
                p.sendMessage(event.player.getName()+" has entered the region.");
            }
        }
        event.newRegion.addVisit();
        event.player.sendMessage("Now entering region: "+event.newRegion.coloredName());
        int pop = m_plugin.playerWatcher().playersInRegion(event.newRegion).size();
        Location center = event.newRegion.location();
        int altitude = center.getBlockY();
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        if (protocolManager != null) {
          PacketContainer chatMessage = protocolManager.createPacket(PacketType.Play.Server.TITLE);
          String colorHex = "#" + String.format("%06X", event.newRegion.color().getColor().asRGB());
          chatMessage.getChatComponents().write(0, WrappedChatComponent.fromJson("{text:\"" + event.newRegion.name() + "\", color: \""+colorHex+"\"}"));
          try {
            protocolManager.sendServerPacket(event.player, chatMessage);
          } catch (Exception e) {
          }
          chatMessage = protocolManager.createPacket(PacketType.Play.Server.TITLE);
          chatMessage.getChatComponents().write(0, WrappedChatComponent.fromJson("{text:\"Population: " + pop + " Altitude: "+ altitude + "\", color: \"#ffffff\"}"));
          chatMessage.getTitleActions().write(0, TitleAction.SUBTITLE);
          try {
            protocolManager.sendServerPacket(event.player, chatMessage);
          } catch (Exception e) {
          }
          chatMessage = protocolManager.createPacket(PacketType.Play.Server.TITLE);
          chatMessage.getChatComponents().write(0, WrappedChatComponent.fromJson("{text:\"Now entering " + event.newRegion.name() + "\", color: \""+colorHex+"\"}"));
          chatMessage.getTitleActions().write(0, TitleAction.ACTIONBAR);
          try {
            protocolManager.sendServerPacket(event.player, chatMessage);
          } catch (Exception e) {
          }
        } else {
          event.player.sendTitle(event.newRegion.coloredName(), "Population: " + pop + " Altitude: " + altitude);
        }
    }

    @EventHandler
    public void onPlayerNear(PlayerNearRegionPostEvent event) {
        Collection<Region> nearby = m_manager.neighborsForRegion(event.region);
        StringBuilder nearbyText = new StringBuilder();
        if (event.region.markSeenByPlayer(event.player)) {
          event.player.playSound(event.region.location(), Sound.UI_TOAST_CHALLENGE_COMPLETE, (float)1, (float)1);

          ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

          if (protocolManager != null) {
            PacketContainer chatMessage = protocolManager.createPacket(PacketType.Play.Server.TITLE);
            String colorHex = "#" + String.format("%06X", event.region.color().getColor().asRGB());
            chatMessage.getChatComponents().write(0, WrappedChatComponent.fromJson("{text:\"Region discovered\", color: \""+colorHex+"\"}"));
            try {
              protocolManager.sendServerPacket(event.player, chatMessage);
            } catch (Exception e) {
            }
            chatMessage = protocolManager.createPacket(PacketType.Play.Server.TITLE);
            chatMessage.getChatComponents().write(0, WrappedChatComponent.fromJson("{text:\"You discovered the region " + event.region.name() + "\", color: \""+colorHex+"\"}"));
            chatMessage.getTitleActions().write(0, TitleAction.SUBTITLE);
            try {
              protocolManager.sendServerPacket(event.player, chatMessage);
            } catch (Exception e) {
            }
          } else {
            //FIXME: also show pop/alt subtitle
            event.player.sendTitle("Region Discovered", "You discovered the region " + event.region.coloredName());
          }

          // TODO: Make this configurable and disablable
          event.player.giveExp(10);
        }
        for(Region region : nearby) {
            nearbyText.append(" ");
            nearbyText.append(region.coloredName());
        }

        event.player.playSound(event.region.location(), Sound.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, (float)0.5, (float)0.6);
        event.player.sendMessage("Nearby regions:" + nearbyText.toString());


        World w = event.player.getLocation().getWorld();
        BukkitScheduler scheduler = m_plugin.getServer().getScheduler();
        BukkitTask puffGenerator = scheduler.runTaskTimer(m_plugin, () -> {
          w.spawnParticle(Particle.REDSTONE, event.region.teleportLocation().add(0, -0.5, 0), 15, 0.1, 0.1, 0.1, event.region.dustOptions());
        }, 0, 15);

        scheduler.runTaskLater(m_plugin, () -> {
          puffGenerator.cancel();
        }, 20 * 7);
    }

    @EventHandler
    public void onPlayerAddCharge(PlayerAddRegionChargeEvent event) {
        if (event.region.charges() == 1) {
            for (Player p : m_plugin.playerWatcher().playersInRegion(event.region)) {
                if (p != event.player) {
                    p.sendMessage(event.player.getName()+" re-charged the region post.");
                }
            }
        }
        event.player.playSound(event.region.location(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, (float)0.5, (float)0.6);
        event.player.sendMessage("You re-charged the region post. It has " + event.region.charges() + " charges remaining.");
        event.player.getWorld().spawnParticle(Particle.REDSTONE, event.region.teleportLocation(), 100, 1, 1, 1, event.region.dustOptions());
    }

    @EventHandler
    public void onPlayerMoveIn(PlayerMoveInEvent event) {
        /*RegionPostBuilder builder = new RegionPostBuilder(event.region);
        builder.fireworks();
        for (Player p : m_plugin.playerWatcher().playersInRegion(event.region)) {
            if (p != event.player) {
              p.sendMessage(event.player.getName()+" has moved in to the region.");
            }
        }*/
    }
}
