package cc.i9mc.onebedwars.game.event;

import cc.i9mc.k8sgameack.events.ACKGameEndEvent;
import cc.i9mc.onebedwars.events.BedwarsGameEndEvent;
import cc.i9mc.onebedwars.game.Game;
import org.bukkit.Bukkit;

public class EndEvent extends GameEvent {
    public EndEvent() {
        super("游戏结束！", 30, 5);
    }

    public void excute(Game game) {
        Bukkit.getPluginManager().callEvent(new BedwarsGameEndEvent());
        Bukkit.getPluginManager().callEvent(new ACKGameEndEvent());
        Bukkit.shutdown();
    }
}
