package cc.i9mc.onebedwars.guis;

import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.game.GameTeam;
import cc.i9mc.onebedwars.utils.SoundUtil;
import cc.i9mc.onebedwars.utils.Util;
import cc.i9mc.gameutils.gui.CustonGUI;
import cc.i9mc.gameutils.gui.GUIAction;
import cc.i9mc.gameutils.utils.ItemBuilderUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamSelectionGUI extends CustonGUI {

    public TeamSelectionGUI(Player player, Game game) {
        super(player, "§8选择队伍", 36);

        int[] slots = new int[]{11, 12, 13, 14};

        int i = 0;
        for(GameTeam team : game.getGameTeams()){
            if(i > slots.length - 1){
                break;
            }

            List<String> lore = new ArrayList<>(Arrays.asList(" ", "§7人数:", "§e(§b" + team.getGamePlayers().size() + "§e/§b" + team.getMaxPlayers() + "§e)", " ", "§7成员:"));
            for (GamePlayer gamePlayer : team.getGamePlayers()) {
                lore.add(team.getChatColor() + gamePlayer.getDisplayname());
            }
            setItem(slots[i], new ItemBuilderUtil().setType(Material.WOOL).setDurability(team.getDyeColor().getWoolData()).setAmount(team.getGamePlayers().size()).setDisplayName(team.getName()).setLores(lore).getItem(), new GUIAction(0, () -> {
                if (team.isFull()) {
                    player.sendMessage("§c这个队伍满了!");
                    return;
                }

                if ((GamePlayer.getOnlinePlayers().size() / game.getGameTeams().size()) < team.getGamePlayers().size()) {
                    player.sendMessage("§c为了队伍平衡请选择别的队伍.");
                    return;
                }

                GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
                if(gamePlayer.getGameTeam() != null) gamePlayer.setGameTeam(null);

                team.addPlayer(gamePlayer);
                team.equipPlayerWithLeather(player);
                player.getInventory().setItem(7, new ItemBuilderUtil().setType(Material.LEATHER_CHESTPLATE).setColor(team.getColor()).getItem());
                Util.setPlayerTeamTab();

                player.sendMessage("§e你加入了 " + team.getName());
                player.playSound(player.getLocation(), SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);
            }, true));

            i++;
        }

        setItem(30, new ItemBuilderUtil().setType(Material.BEACON).setDisplayName("§a自动分配").setLores("§7将你的队伍设置为自动分配").getItem(), new GUIAction(0, () -> {
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            if(gamePlayer.getGameTeam() != null) gamePlayer.setGameTeam(null);

            player.getInventory().setArmorContents(null);
            player.getInventory().setItem(7, new ItemStack(Material.AIR));
            Util.setPlayerTeamTab();

            player.sendMessage("§e将队伍设置为自动分配!");
            player.playSound(player.getLocation(), SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);
        }, true));

        setItem(32, new ItemBuilderUtil().setType(Material.ENCHANTED_BOOK).setDisplayName("§e组队说明").setLores("§7输入 /zd cj <队伍名> 来创建队伍", "§7输入 /zd yq <玩家名> 来邀请他人", "§c组队后将不能选择队伍,你和你的队员会再开局后被分配到一起!").getItem(), new GUIAction(0, () -> {
        }, true));
    }
}
