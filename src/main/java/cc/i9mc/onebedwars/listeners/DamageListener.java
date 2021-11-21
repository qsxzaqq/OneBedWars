package cc.i9mc.onebedwars.listeners;

import cc.i9mc.gameutils.utils.ActionBarUtil;
import cc.i9mc.nick.Nick;
import cc.i9mc.onebedwars.game.*;
import cc.i9mc.rejoin.events.RejoinGameDeathEvent;
import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.events.BedwarsPlayerKilledEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftWither;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class DamageListener implements Listener {
    private final Game game = OneBedwars.getInstance().getGame();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityRegainHealth(EntityRegainHealthEvent e) {
        if (e.getEntityType() == EntityType.WITHER) {
            e.setCancelled(true);
            e.setAmount(0);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() == EntityType.VILLAGER) {
            event.setCancelled(true);
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (game.getGameLobbyCountdown() != null) {
                    if (game.getGameLobbyCountdown().getCountdown() < 3) {
                        return;
                    }
                }
                player.teleport(game.getWaitingLocation());
                return;
            }
        }

        if (game.getGameState() == GameState.RUNNING) {
            if (game.getEventManager().isOver()) {
                event.setCancelled(true);
                return;
            }

            if (gamePlayer.isSpectator()) {
                event.setCancelled(true);
                if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                    gamePlayer.getSpectatorTarget().tp();
                }
                return;
            }

            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                event.setDamage(100.0D);

                Player killer = player.getKiller();

                GameTeam gameTeam = gamePlayer.getGameTeam();

                if (killer != null) {
                    GamePlayer killerPlayer = GamePlayer.get(killer.getUniqueId());
                    if(killerPlayer == null) {
                        return;
                    }
                    GameTeam killerTeam = killerPlayer.getGameTeam();

                    if (gameTeam.isWitherDead()) {
                        new BukkitRunnable() {
                            int i = 0;

                            @Override
                            public void run() {
                                if (i == 5) {
                                    cancel();
                                    return;
                                }
                                ActionBarUtil.sendBar(killer, "§6+1个金币");
                                i++;
                            }
                        }.runTaskTimerAsynchronously(OneBedwars.getInstance(), 0, 10);
                        killer.sendMessage("§6+1个金币 (最终击杀)");
                        OneBedwars.getInstance().getEcon().depositPlayer(player, 1);
                        killerPlayer.addFinalKills();

                        game.broadcastMessage(gameTeam.getChatColor() + gamePlayer.getDisplayname() + "(" + gameTeam.getName() + "♛)[最终击杀]§e被" + killerTeam.getChatColor() + Nick.get().getCache().getOrDefault(killer.getName(), killer.getName()) + "(" + killerTeam.getName() + "♛)§e狠狠滴丢下虚空");
                    } else {
                        game.broadcastMessage(gameTeam.getChatColor() + gamePlayer.getDisplayname() + "(" + gameTeam.getName() + "♛)§e被" + killerTeam.getChatColor() + Nick.get().getCache().getOrDefault(killer.getName(), killer.getName()) + "(" + killerTeam.getName() + "♛)§e狠狠滴丢下虚空");
                        killerPlayer.addKills();
                    }
                } else {
                    game.broadcastMessage(gameTeam.getChatColor() + gamePlayer.getDisplayname() + "(" + gameTeam.getName() + "♛)§e划下了虚空");
                }
                player.setMetadata("voidPlayer", new FixedMetadataValue(OneBedwars.getInstance(), ""));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        GameTeam gameTeam = gamePlayer.getGameTeam();

        event.setDeathMessage(null);
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.getEntity().getInventory().clear();
        event.setDroppedExp(0);

        if (game.getGameState() == GameState.WAITING) {
            return;
        }

        if (gamePlayer.isSpectator()) {
            return;
        }

        if (!player.hasMetadata("voidPlayer")) {
            Player killer = player.getKiller();
            List<GamePlayer> killers = gamePlayer.getAssistsMap().getAssists(System.currentTimeMillis());
            if (killer == null && !killers.isEmpty())
                killer = killers.get(0).getPlayer();

            if (killer != null) {
                GamePlayer killerPlayer = GamePlayer.get(killer.getUniqueId());
                GameTeam killerTeam = killerPlayer.getGameTeam();

                if (gameTeam.isWitherDead()) {
                    Player finalKiller = killer;
                    new BukkitRunnable() {
                        int i = 0;

                        @Override
                        public void run() {
                            if (i == 5) {
                                cancel();
                                return;
                            }
                            ActionBarUtil.sendBar(finalKiller, "§6+1个金币");
                            i++;
                        }
                    }.runTaskTimerAsynchronously(OneBedwars.getInstance(), 0, 10);
                    killer.sendMessage("§6+1个金币 (最终击杀)");
                    OneBedwars.getInstance().getEcon().depositPlayer(player, 1);
                    killerPlayer.addFinalKills();

                    game.broadcastMessage(gameTeam.getChatColor() + gamePlayer.getDisplayname() + "(" + gameTeam.getName() + "♛)[最终击杀]§e被" + killerTeam.getChatColor() + killerPlayer.getDisplayname() + "(" + killerTeam.getName() + "♛)§e狠狠滴推倒");
                    Bukkit.getPluginManager().callEvent(new RejoinGameDeathEvent(player));
                } else {
                    game.broadcastMessage(gameTeam.getChatColor() + gamePlayer.getDisplayname() + "(" + gameTeam.getName() + "♛)§e被" + killerTeam.getChatColor() + killerPlayer.getDisplayname() + "(" + killerTeam.getName() + "♛)§e狠狠滴推倒");
                }
            }
        }

        player.removeMetadata("voidPlayer", OneBedwars.getInstance());
        Bukkit.getScheduler().runTaskLater(OneBedwars.getInstance(), () -> {
            player.spigot().respawn();
        }, 10L);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();

        if (entity instanceof Player || damager instanceof Player || damager instanceof Projectile) {
            GamePlayer gamePlayer = GamePlayer.get(entity.getUniqueId());

            if (game.getGameState() == GameState.RUNNING) {
                if (damager instanceof Player && entity instanceof Player) {
                    GamePlayer damagerPlayer = GamePlayer.get(damager.getUniqueId());

                    if (damagerPlayer.isSpectator()) {
                        event.setCancelled(true);
                    }

                    if (gamePlayer.getGameTeam().isInTeam(damagerPlayer)) {
                        event.setCancelled(true);
                    } else {
                        gamePlayer.getAssistsMap().setLastDamage(damagerPlayer, System.currentTimeMillis());
                    }
                } else if (entity instanceof Player && damager instanceof Projectile) {
                    Projectile projectile = (Projectile) damager;

                    if (projectile.getShooter() instanceof Wither) {
                        Wither wither = (Wither) projectile.getShooter();
                        TeamWither teamWither = (TeamWither) ((CraftWither) wither).getHandle();
                        if (teamWither.getGameTeam().isInTeam(gamePlayer)) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                    if (projectile.getShooter() instanceof Player) {
                        GamePlayer damagerPlayer = GamePlayer.get(((Player) projectile.getShooter()).getUniqueId());

                        if (gamePlayer.getGameTeam().isInTeam(damagerPlayer)) {
                            event.setCancelled(true);
                        } else {
                            gamePlayer.getAssistsMap().setLastDamage(damagerPlayer, System.currentTimeMillis());
                        }
                    }
                }
            }
        }
    }
}
