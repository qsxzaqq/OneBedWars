package cc.i9mc.onebedwars.game.event;

import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.timer.CompassRunnable;
import cc.i9mc.onebedwars.game.timer.GeneratorRunnable;
import cc.i9mc.onebedwars.utils.SoundUtil;

public class StartEvent extends GameEvent {
    public StartEvent() {
        super("开始游戏", 5, 0);
    }

    public void excuteRunnbale(Game game, int seconds) {
        game.broadcastSound(SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);
        game.broadcastTitle(1, 20, 1, "§c§l游戏即将开始", "§e§l" + seconds);
    }

    public void excute(Game game) {
        new GeneratorRunnable(game).start();
        new CompassRunnable().start();
    }
}
