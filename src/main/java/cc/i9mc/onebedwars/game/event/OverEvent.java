package cc.i9mc.onebedwars.game.event;

import cc.i9mc.onebedwars.events.BedwarsGameOverEvent;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GameOverRunnable;
import org.bukkit.Bukkit;

public class OverEvent extends GameEvent {
    public OverEvent() {
        super("游戏结束", 2925, 4);
    }

    public void excute(Game game) {
        game.getEventManager().setCurrentEvent(5);
        Bukkit.getPluginManager().callEvent(new BedwarsGameOverEvent(game.getWinner()));
        new GameOverRunnable(game);
    }
}
