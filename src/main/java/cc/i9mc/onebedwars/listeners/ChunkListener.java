package cc.i9mc.onebedwars.listeners;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {
    private final Game game = OneBedwars.getInstance().getGame();

    @EventHandler
    public void onUnload(ChunkUnloadEvent unload) {
        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (!game.getMapData().chunkIsInRegion(unload.getChunk().getX(), unload.getChunk().getZ())) {
            return;
        }

        unload.setCancelled(true);
    }

}
