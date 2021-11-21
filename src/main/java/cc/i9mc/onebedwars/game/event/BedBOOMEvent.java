package cc.i9mc.onebedwars.game.event;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GameTeam;
import org.bukkit.Material;
import org.bukkit.Sound;

public class BedBOOMEvent extends GameEvent {
    public BedBOOMEvent() {
        super("凋零消失", 600, 2);
    }

    @Override
    public void excute(Game game) {
        OneBedwars.getInstance().mainThreadRunnable(() -> {
            for (GameTeam gameTeam : game.getGameTeams()) {
                if (gameTeam.isWitherDead()) continue;
                gameTeam.getTeamWither().setHealth(0);
                gameTeam.setWitherDead(true);
            }
        });

        game.broadcastSound(Sound.ENDERDRAGON_GROWL, 1, 1);
        game.broadcastTitle(10, 20, 10, "§c§l凋零消失", "§e所有队伍凋零消失");
    }
}
