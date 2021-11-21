package cc.i9mc.onebedwars.scoreboards;

import cc.i9mc.gameutils.utils.board.Board;
import cc.i9mc.onebedwars.events.BedwarsGameStartEvent;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.game.GameTeam;
import cc.i9mc.onebedwars.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GameBoard implements Listener {
    private static final Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
    private static Objective hp;
    private static Objective o;

    private static Game game;

    public GameBoard(Game game) {
        GameBoard.game = game;
    }

    public static Scoreboard getBoard() {
        return sb;
    }

    public static void show(Player p) {
        p.setScoreboard(sb);
    }

    public static void updateBoard() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Board board = gamePlayer.getBoard();
            Player player = gamePlayer.getPlayer();
            if (player == null) {
                continue;
            }
            hp.getScore(player.getName()).setScore((int) player.getHealth());
            o.getScore(player.getName()).setScore((int) player.getHealth());


            List<String> list = new ArrayList<>();
            list.add("§7团队 " + new SimpleDateFormat("MM", Locale.CHINESE).format(Calendar.getInstance().getTime()) + "/" + new SimpleDateFormat("dd", Locale.CHINESE).format(Calendar.getInstance().getTime()) + "/" + new SimpleDateFormat("yy", Locale.CHINESE).format(Calendar.getInstance().getTime()) + " ");
            list.add(" ");
            list.add(game.getEventManager().formattedNextEvent());
            list.add("§a" + game.getFormattedTime(game.getEventManager().getLeftTime()));
            list.add("  ");
            for (GameTeam gameTeam : game.getGameTeams()) {
                list.add(gameTeam.getName() + " " + (gameTeam.isWitherDead() ? "§7❤" : "§c❤") + "§f | " + (gameTeam.getAlivePlayers().size()) + " " + (gameTeam.isInTeam(gamePlayer) ? " §7(我的队伍)" : ""));
            }
            list.add("   ");
            list.add("§emcyc.win");

            board.send("§e§l凋零起床战争", list);
        }
    }

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        if (hp == null) {
            hp = sb.registerNewObjective("NAME_HEALTH", "health");
            hp.setDisplaySlot(DisplaySlot.BELOW_NAME);
            hp.setDisplayName(ChatColor.RED + "❤");
        }
        if (o == null) {
            o = sb.registerNewObjective("health", "dummy");
            o.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }

        Util.setPlayerTeamTab();

        for (Player player : Bukkit.getOnlinePlayers()) {
            show(player);
        }

        game.getEventManager().registerRunnable("计分板", (s, c) -> updateBoard());
    }
}
