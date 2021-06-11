package us.camin.regions.ui;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Particle;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.World;
import org.bukkit.Sound;
import io.papermc.lib.PaperLib;
import us.camin.regions.Plugin;

import java.util.logging.Logger;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import java.util.concurrent.CompletableFuture;

public class PlayerTeleporter {
    Logger log = Logger.getLogger("Regions.PlayerTeleporter");

    private Player m_player;
    private Location m_src;
    private Location m_dest;
    private Plugin m_plugin;

    public PlayerTeleporter(Player p, Location src, Location dest, Plugin plugin) {
      this.m_player = p;
      this.m_src = src;
      this.m_dest = dest;
      this.m_plugin = plugin;
    }

    public CompletableFuture<Void> teleport() {
      CompletableFuture<Void> ret = new CompletableFuture<Void>();
      BukkitScheduler scheduler = m_plugin.getServer().getScheduler();

      BukkitTask hoverGenerator = scheduler.runTaskTimer(m_plugin, () -> applyHoverEffect(), 0, 10);
      BukkitTask particleGenerator = scheduler.runTaskTimer(m_plugin, () -> spawnHoverParticles(), 0, 1);
      playTeleportSound();

      m_plugin.getServer().getScheduler().runTaskLater(m_plugin, () -> {
            m_player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 3 + 120, 1, false, false, false));
      }, 20 * 3);

      m_plugin.getServer().getScheduler().runTaskLater(m_plugin, () -> {
          spawnTeleportStartParticles();
          PaperLib.teleportAsync(m_player, m_dest, TeleportCause.COMMAND).thenAccept(res -> {
            m_player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false, false, false));
            m_player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false, false));
            spawnEndParticles();
            particleGenerator.cancel();
            hoverGenerator.cancel();
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
        World world = m_player.getLocation().getWorld();
        world.playSound(m_dest, Sound.BLOCK_PORTAL_TRAVEL, (float)0.5, (float)1.0);
        world.spawnParticle(Particle.CLOUD, m_dest, 70, 1, 1, 1);
        world.spawnParticle(Particle.SPELL_MOB, m_dest, 5, 2, 2, 2);
        world.spawnParticle(Particle.PORTAL, m_player.getLocation(), 70, 1, 1, 1);
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
    }
}
