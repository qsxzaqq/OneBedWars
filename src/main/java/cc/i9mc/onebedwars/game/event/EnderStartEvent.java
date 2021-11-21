package cc.i9mc.onebedwars.game.event;

import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.timer.CompassRunnable;
import cc.i9mc.onebedwars.game.timer.GeneratorRunnable;
import cc.i9mc.onebedwars.utils.SoundUtil;

public class EnderStartEvent extends GameEvent {
    public EnderStartEvent() {
        super("结束传送保护", 60, 1);
    }

    public void excute(Game game) {
    }
}
