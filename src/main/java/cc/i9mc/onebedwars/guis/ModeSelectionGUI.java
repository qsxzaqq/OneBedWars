package cc.i9mc.onebedwars.guis;

import cc.i9mc.gameutils.gui.CustonGUI;
import cc.i9mc.gameutils.gui.GUIAction;
import cc.i9mc.gameutils.utils.ItemBuilderUtil;
import cc.i9mc.onebedwars.database.PlayerData;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.types.ModeType;
import cc.i9mc.onebedwars.utils.SoundUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ModeSelectionGUI extends CustonGUI {

    public ModeSelectionGUI(Player player) {
        super(player, "§8资源类型选择", 27);

        PlayerData playerData = GamePlayer.get(player.getUniqueId()).getPlayerData();

        setItem(11, new ItemBuilderUtil().setType(Material.BED).setDisplayName("§a普通模式").setLores(" ", playerData.getModeType() == ModeType.DEFAULT ? "§a§l✔已选择" : "").getItem(), new GUIAction(0, () -> {
            playerData.setModeType(ModeType.DEFAULT);
            player.playSound(player.getLocation(), SoundUtil.get("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP"), 10.0F, 1.0F);
            player.sendMessage("§a模式选择成功!");
        }, true));

        setItem(15, new ItemBuilderUtil().setType(Material.EXP_BOTTLE).setDisplayName("§a经验模式").setLores(" ", playerData.getModeType() == ModeType.EXPERIENCE ? "§a§l✔已选择" : "").getItem(), new GUIAction(0, () -> {
            playerData.setModeType(ModeType.EXPERIENCE);
            player.playSound(player.getLocation(), SoundUtil.get("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP"), 10.0F, 1.0F);
            player.sendMessage("§a模式选择成功!");
        }, true));
    }
}
