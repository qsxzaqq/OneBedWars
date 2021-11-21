package cc.i9mc.onebedwars.listeners;

import cc.i9mc.gameutils.utils.board.Board;
import cc.i9mc.nick.Nick;
import cc.i9mc.watchnmslreport.BukkitReport;
import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Collections;

public class JoinListener implements Listener {
    private final Game game = OneBedwars.getInstance().getGame();

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (game.getGameState() == GameState.RUNNING && GamePlayer.get(player.getUniqueId()).getGameTeam() != null) {
            event.allow();
            return;
        }

        if ((player.hasPermission("bw.*") || BukkitReport.getInstance().getStaffs().containsKey(player.getName()))) {
            event.allow();

            if(game.getGameState() == GameState.RUNNING) {
                return;
            }
        }

        if (GamePlayer.getOnlinePlayers().size() >= game.getMaxPlayers()) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "开始了");
            return;
        }

        if (game.getGameState() == GameState.RUNNING) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "开始了");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
        if (GamePlayer.get(event.getUniqueId()) != null) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.create(event.getUniqueId(), event.getName());
        if (game.getGameState() == GameState.RUNNING) {
            gamePlayer.setSpectator();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer == null) {
            player.kickPlayer("进你个锤子");
            return;
        }
        gamePlayer.setBoard(new Board(player, "SB", Collections.singletonList("Test")));
        gamePlayer.setDisplayname(Nick.get().getCache().getOrDefault(player.getName(), player.getName()));
        game.addPlayer(gamePlayer);
    }
}
