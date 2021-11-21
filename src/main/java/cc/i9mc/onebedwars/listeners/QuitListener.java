package cc.i9mc.onebedwars.listeners;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    private final Game game = OneBedwars.getInstance().getGame();

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        game.removePlayers(GamePlayer.get(event.getPlayer().getUniqueId()));
    }
}
