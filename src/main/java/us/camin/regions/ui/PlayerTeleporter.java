package us.camin.regions.ui;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Particle;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import java.util.Random;
import io.papermc.lib.PaperLib;
import us.camin.regions.Plugin;

import java.util.logging.Logger;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import java.util.concurrent.CompletableFuture;
import org.bukkit.util.Vector;

public class PlayerTeleporter {
    Logger log = Logger.getLogger("Regions.PlayerTeleporter");

    private Player m_player;
    private Location m_src;
    private Location m_dest;
    private Plugin m_plugin;
    private double m_accuracy;

    public PlayerTeleporter(Player p, Location src, Location dest, Plugin plugin, double accuracy) {
      this.m_player = p;
      this.m_src = src;
      this.m_dest = dest;
      this.m_plugin = plugin;
      this.m_accuracy = accuracy;

      if (m_accuracy < 1) {
        Random rand = new Random();

        Vector travelVec = m_dest.toVector().subtract(m_src.toVector()).normalize();
        double angleDelta = (Math.PI / 3) * (rand.nextGaussian() - 0.5) * (1-accuracy);
        travelVec.rotateAroundY(angleDelta);

        double distanceToDest = m_src.distance(m_dest);
        double distanceDelta = (distanceToDest/3) * (rand.nextGaussian() - 0.5) * (1 - accuracy);
        double targetDistance = distanceToDest - distanceDelta;

        travelVec.multiply(targetDistance);

        m_dest = m_src.clone().add(travelVec);
      }
    }

    public CompletableFuture<Void> teleport() {
      CompletableFuture<Void> ret = new CompletableFuture<Void>();
      BukkitScheduler scheduler = m_plugin.getServer().getScheduler();

      BukkitTask hoverGenerator = scheduler.runTaskTimer(m_plugin, () -> applyHoverEffect(), 0, 10);
      BukkitTask particleGenerator = scheduler.runTaskTimer(m_plugin, () -> spawnHoverParticles(), 0, 1);
      BukkitTask noiseGenerator = scheduler.runTaskTimer(m_plugin, () -> {
        if (m_accuracy < 1) {
          m_player.getLocation().getWorld().playSound(m_player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, (float)0.5, (float)1.0);
          m_player.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, m_player.getLocation(), 3, 1, 1, 1);
        }
      }, 20, 15);

      if (m_accuracy < 1) {
        m_player.sendMessage("Teleporting you there-ish now...");
        m_plugin.getServer().getScheduler().runTaskLater(m_plugin, () -> {
          m_player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Oh no!" + ChatColor.RESET + ChatColor.YELLOW + " The region post is malfunctioning!");
        }, 40);
      } else {
        m_player.sendMessage("Teleporting you there now...");
      }

      playTeleportSound();

      m_plugin.getServer().getScheduler().runTaskLater(m_plugin, () -> {
            m_player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 3 + 120, 1, false, false, false));
      }, 20 * 3);

      m_plugin.getServer().getScheduler().runTaskLater(m_plugin, () -> {
          spawnTeleportStartParticles();
          PaperLib.teleportAsync(m_player, m_dest, TeleportCause.COMMAND).thenAccept(res -> {
            Block targetBlock = m_dest.getBlock();
            if (!targetBlock.isPassable() && !targetBlock.getRelative(BlockFace.DOWN).isPassable()) {
              while (!(targetBlock.isPassable() && targetBlock.getRelative(BlockFace.UP).isEmpty() && !targetBlock.getRelative(BlockFace.DOWN).isPassable())) {
                targetBlock = targetBlock.getRelative(BlockFace.UP);
              }
              m_dest = targetBlock.getLocation().add(0.5, 0, 0.5);
              m_player.teleport(m_dest, TeleportCause.COMMAND);
            }
            m_player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false, false, false));
            m_player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false, false));
            if (m_accuracy < 1) {
              m_player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 5, 1, false, true, false));
            }
            spawnEndParticles();
            particleGenerator.cancel();
            hoverGenerator.cancel();
            noiseGenerator.cancel();
            ret.complete(null);
          });
      }, 20 * 6);
      return ret;
    }

    void spawnTeleportStartParticles() {
        World world = m_dest.getWorld();
        world.spawnParticle(Particle.CLOUD, m_dest, 70, 1, 1, 1);
    }

    void spawnEndParticles() {
        final World world = m_player.getLocation().getWorld();
        world.playSound(m_dest, Sound.BLOCK_PORTAL_TRAVEL, (float)0.5, (float)1.0);
        world.spawnParticle(Particle.CLOUD, m_dest, 70, 1, 1, 1);
        world.spawnParticle(Particle.SPELL_MOB, m_dest, 5, 2, 2, 2);
        world.spawnParticle(Particle.PORTAL, m_player.getLocation(), 70, 1, 1, 1);
        if (m_accuracy < 1) {
          BukkitTask fireGenerator = m_plugin.getServer().getScheduler().runTaskTimer(m_plugin, () -> {
              world.spawnParticle(Particle.FLAME, m_player.getLocation(), 80, 1, 1, 1, 0);
          }, 0, 8);
          m_plugin.getServer().getScheduler().runTaskLater(m_plugin, () -> {
              fireGenerator.cancel();
          }, 20 * 8);
          m_player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Ouch!" + ChatColor.RESET + ChatColor.YELLOW + " You didn't have enough XP and the region post malfunctioned.");
          m_player.damage(5 * (1.0-m_accuracy));
          world.playSound(m_player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, (float)1.0, (float)1.0);
        }
    }

    void applyHoverEffect() {
      m_player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 10, 1, false, false, false));
    }

    void spawnHoverParticles() {
      World w = m_player.getLocation().getWorld();
      w.spawnParticle(Particle.SPELL_MOB, m_dest, 5, 1, 1, 1);
      w.spawnParticle(Particle.SPELL_MOB, m_src, 5, 2, 2, 2);
    }

    void playTeleportSound() {
      m_src.getWorld().playSound(m_src, Sound.BLOCK_PORTAL_TRIGGER, (float)0.5, (float)0.6);
      m_src.getWorld().playSound(m_src, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, (float)0.5, (float)1.0);
      m_src.getWorld().playSound(m_dest, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, (float)0.5, (float)1.0);
      if (m_accuracy < 1) {
          m_src.getWorld().playSound(m_src, Sound.ENTITY_ITEM_BREAK, (float)1.0, (float)1.0);
      }
    }
}
