package cc.i9mc.onebedwars.listeners;

import cc.i9mc.gameutils.utils.TitleUtil;
import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.database.PlayerData;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.game.GameState;
import cc.i9mc.onebedwars.game.GameTeam;
import cc.i9mc.onebedwars.utils.SoundUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReSpawnListener implements Listener {
    private final List<UUID> noDamage = new ArrayList<>();
    private final Game game = OneBedwars.getInstance().getGame();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        GameTeam gameTeam = gamePlayer.getGameTeam();
        PlayerData playerData = gamePlayer.getPlayerData();

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        gamePlayer.clean();

        if (gameTeam.isWitherDead()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    TextComponent textComponent = new TextComponent("§c你凉了!想再来一局嘛? ");
                    textComponent.addExtra("§b§l点击这里!");
                    textComponent.getExtra().get(0).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/queue join qc tsqc"));
                    player.spigot().sendMessage(textComponent);

                    event.setRespawnLocation(gameTeam.getSpawn());
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0.0F);
                    player.teleport(gameTeam.getSpawn());
                    GamePlayer.getOnlinePlayers().forEach((gamePlayer1 -> gamePlayer1.getPlayer().hidePlayer(player)));

                    gamePlayer.toSpectator("§c你凉了！", "§7你没床了");
                }
            }.runTaskLater(OneBedwars.getInstance(), 1L);
            playerData.addLoses();

            return;
        }

        event.setRespawnLocation(gameTeam.getSpawn());
        player.setExp(0f);
        player.setLevel(0);
        player.teleport(gameTeam.getSpawn());
        player.setGameMode(GameMode.SURVIVAL);
        noDamage.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(OneBedwars.getInstance(), () -> noDamage.remove(player.getUniqueId()), 60);
        TitleUtil.sendTitle(player, 1, 20, 1, "§a已复活！", "§7因为你的床还在所以你复活了");
    }

    @EventHandler
    public void onDamage(EntityDamageEvent evt) {
        if (noDamage.contains(evt.getEntity().getUniqueId())) {
            evt.setCancelled(true);
        }
    }
}
