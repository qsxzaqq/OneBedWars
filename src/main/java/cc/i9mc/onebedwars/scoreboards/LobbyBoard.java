package cc.i9mc.onebedwars.scoreboards;

import cc.i9mc.gameutils.GameUtilsAPI;
import cc.i9mc.gameutils.utils.board.Board;
import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.database.PlayerData;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GameLobbyCountdown;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.game.GameState;
import cc.i9mc.onebedwars.types.ModeType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class LobbyBoard implements Listener {
    private static final Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
    private static Objective hp;
    private static Objective o;


    private static Game game;

    public LobbyBoard(Game game) {
        LobbyBoard.game = game;
    }

    public static Scoreboard getBoard() {
        return sb;
    }

    public static void show(Player p) {
        if (hp == null) {
            hp = sb.registerNewObjective("NAME_HEALTH", "health");
            hp.setDisplaySlot(DisplaySlot.BELOW_NAME);
            hp.setDisplayName(ChatColor.GOLD + "✫");
        }
        if (o == null) {
            o = sb.registerNewObjective("health", "dummy");
            o.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }

        p.setScoreboard(sb);
    }

    public static void updateBoard() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Board board = gamePlayer.getBoard();
            Player player = gamePlayer.getPlayer();
            if (player == null) {
                continue;
            }
            PlayerData playerData = gamePlayer.getPlayerData();
            hp.getScore(player.getName()).setScore(OneBedwars.getInstance().getLevel((playerData.getKills() * 2) + (playerData.getDestroyedBeds() * 10) + (playerData.getWins() * 15)));
            o.getScore(player.getName()).setScore(OneBedwars.getInstance().getLevel((playerData.getKills() * 2) + (playerData.getDestroyedBeds() * 10) + (playerData.getWins() * 15)));

            List<String> list = new ArrayList<>();
            list.add(" ");
            list.add("§f地图: §a" + game.getMapData().getName());
            list.add("§f队伍: §a" + game.getMapData().getPlayers().getTeam() + "人 " + game.getGameTeams().size() + "队");
            list.add("§f作者: §a" + game.getMapData().getAuthor());
            list.add("  ");
            list.add("§f玩家: §a" + GamePlayer.getOnlinePlayers().size() + "/" + game.getMaxPlayers());
            list.add("   ");
            list.add(getCountdown());
            list.add("    ");
            list.add("§f你的模式: §a" + (playerData.getModeType() == ModeType.DEFAULT ? "普通模式" : "经验模式"));
            list.add("     ");
            list.add("§f版本: §a" + OneBedwars.getInstance().getDescription().getVersion());
            list.add("§f服务器: §a" + GameUtilsAPI.getServerName());
            list.add("      ");
            list.add("§emcyc.win");

            board.send("§e§l凋零起床战争", list);
        }
    }

    private static String getCountdown() {
        Game game = OneBedwars.getInstance().getGame();
        GameLobbyCountdown gameLobbyCountdown = game.getGameLobbyCountdown();

        if (gameLobbyCountdown != null) {
            return gameLobbyCountdown.getCountdown() + "秒后开始";
        } else if (game.getGameState() == GameState.WAITING) {
            return "§f等待中...";
        }

        return null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        LobbyBoard.updateBoard();
    }
}
