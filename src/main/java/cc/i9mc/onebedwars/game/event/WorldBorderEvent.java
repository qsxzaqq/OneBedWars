package cc.i9mc.onebedwars.game.event;

import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.utils.SoundUtil;

public class WorldBorderEvent extends GameEvent {
    public WorldBorderEvent() {
        super("开始收缩边界", 10, 3);
    }

    @Override
    public void excute(Game game) {
        game.setWorldBorder(true);
        game.getMapData().getRegion().getPos1().toLocation().getWorld().getWorldBorder().setSize(game.getMapData().getWorldBorder().getTo(), game.getMapData().getWorldBorder().getSpeed());
    }

    @Override
    public void excuteRunnbale(Game game, int seconds) {
        game.broadcastSound(SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);
        game.broadcastTitle(1, 20, 1, "§c§l边界即将开始收缩", "§e§l" + seconds);
    }
}
