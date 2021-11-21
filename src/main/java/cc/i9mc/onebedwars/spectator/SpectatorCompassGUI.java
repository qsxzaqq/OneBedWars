package cc.i9mc.onebedwars.spectator;

import cc.i9mc.gameutils.gui.CustonGUI;
import cc.i9mc.gameutils.gui.GUIAction;
import cc.i9mc.gameutils.utils.ItemBuilderUtil;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.game.GameTeam;
import org.bukkit.entity.Player;

public class SpectatorCompassGUI extends CustonGUI {

    public SpectatorCompassGUI(Player player) {
        super(player, "§8选择一个玩家来传送", GamePlayer.getOnlinePlayers().size() <= 27 ? 27 : 54);

        int i = 0;
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            GameTeam gameTeam = gamePlayer.getGameTeam();

            if (!gamePlayer.isOnline() || gamePlayer.isSpectator()) {
                continue;
            }

            setItem(i, new ItemBuilderUtil().setOwner(gamePlayer.getName()).setDisplayName(gameTeam.getName() + " " + gamePlayer.getDisplayname()).setLores(" ", "§f血量: §8" + (int) gamePlayer.getPlayer().getHealth(), "§f饥饿: §8" + gamePlayer.getPlayer().getFoodLevel(), "§f等级: §8" + gamePlayer.getPlayer().getLevel(), "§f距离: §8" + (int) gamePlayer.getPlayer().getLocation().distance(player.getLocation())).getItem(), new GUIAction(0, () -> {
                player.teleport(gamePlayer.getPlayer());
            }, true));
            i++;
        }
    }
}
