package cc.i9mc.onebedwars.game;

import cc.i9mc.onebedwars.villager.MerchantCategoryManager;
import cc.i9mc.gameutils.utils.ItemBuilderUtil;
import cc.i9mc.gameutils.utils.TitleUtil;
import cc.i9mc.k8sgameack.K8SGameACK;
import cc.i9mc.k8sgameack.events.ACKGameLoadingEvent;
import cc.i9mc.k8sgameack.events.ACKGameStartEvent;
import cc.i9mc.pluginchannel.BukkitChannel;
import cc.i9mc.pluginchannel.bukkit.PBukkitChannelTask;
import cc.i9mc.rejoin.events.RejoinGameDeathEvent;
import cc.i9mc.rejoin.events.RejoinPlayerJoinEvent;
import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.database.map.MapData;
import cc.i9mc.onebedwars.events.BedwarsGameStartEvent;
import cc.i9mc.onebedwars.game.event.EventManager;
import cc.i9mc.onebedwars.scoreboards.GameBoard;
import cc.i9mc.onebedwars.scoreboards.LobbyBoard;
import cc.i9mc.onebedwars.specials.SpecialItem;
import cc.i9mc.onebedwars.utils.Util;
import com.nametagedit.plugin.NametagEdit;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

import java.util.*;

@Data
public class Game {
    private OneBedwars main;
    private EventManager eventManager;
    private MapData mapData;
    private GameState gameState;
    private boolean forceStart;

    private Location waitingLocation;

    private List<Location> blocks;
    private GameLobbyCountdown gameLobbyCountdown = null;
    private List<GameTeam> gameTeams;
    private List<GameParty> gameParties;
    private boolean worldBorder;

    private HashMap<ArmorStand, String> armorSande;
    private HashMap<ArmorStand, String> armorStand;

    private List<SpecialItem> specialItems;

    public Game(OneBedwars main, Location waitingLocation) {
        this.main = main;
        this.forceStart = false;
        this.waitingLocation = waitingLocation;
        this.gameTeams = new ArrayList<>();
        this.gameParties = new ArrayList<>();

        this.armorSande = new HashMap<>();
        this.armorStand = new HashMap<>();

        this.specialItems = new ArrayList<>();

        MerchantCategoryManager.init();
        this.eventManager = new EventManager(this);
        this.worldBorder = false;
    }

    public void loadGame(MapData mapData) {
        this.mapData = mapData;
        this.blocks = mapData.loadMap();

        for (TeamColor teamColor : TeamColor.values()) {
            MapData.Team team = mapData.getTeam(teamColor);
            if(team == null) continue;

            gameTeams.add(new GameTeam(teamColor, team.getSpawn().toLocation(), team.getWither().toLocation(), mapData.getPlayers().getTeam()));
        }

        WorldBorder worldBorder = mapData.getRegion().getPos1().toLocation().getWorld().getWorldBorder();
        worldBorder.setCenter(mapData.getWorldBorder().getX(), mapData.getWorldBorder().getZ());
        worldBorder.setSize(mapData.getWorldBorder().getSize());

        this.gameState = GameState.WAITING;
        K8SGameACK.getInstance().getExpand().put("map", mapData.getName());
        Bukkit.getPluginManager().callEvent(new ACKGameLoadingEvent(getMaxPlayers()));
    }

    public void addPlayer(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        if (gameState == GameState.RUNNING) {
            GameBoard.show(player);
            GameBoard.updateBoard();

            if (gamePlayer.getGameTeam() != null) {
                if (!gamePlayer.getGameTeam().isDead()) {
                    Util.setPlayerTeamTab();
                    Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, null, false));
                    broadcastMessage("§7" + gamePlayer.getDisplayname() + "§a重连上线");
                    return;
                }

                Bukkit.getPluginManager().callEvent(new RejoinGameDeathEvent(gamePlayer.getPlayer()));
            }

            gamePlayer.toSpectator(null, null);
            return;
        }

        GamePlayer.getOnlinePlayers().forEach((gamePlayer1 -> {
            gamePlayer1.getPlayer().hidePlayer(player);
            gamePlayer1.getPlayer().showPlayer(player);
            player.hidePlayer(player);
            player.showPlayer(player);
        }));

        player.spigot().respawn();
        player.setGameMode(GameMode.ADVENTURE);
        player.getEnderChest().clear();
        gamePlayer.clean();

        Bukkit.getScheduler().runTaskAsynchronously(main, () ->
                PBukkitChannelTask.createTask()
                        .channel(BukkitChannel.getInst().getBukkitChannel())
                        .sender(player)
                        .command("BungeeParty", "data", player.getName())
                        .result((result) -> {
                            List<String> results = Arrays.asList(result);

                            Player player1 = Bukkit.getPlayerExact(results.get(1));
                            if (player1 == null) return;
                            if (getPlayerParty(GamePlayer.get(player1.getUniqueId())) != null) return;

                            String teamName = results.get(0);

                            LinkedList<GamePlayer> gamePlayers = new LinkedList<>();
                            for (int i = 1; i < results.size(); i++) {
                                if (Bukkit.getPlayerExact(results.get(i)) == null) return;
                                gamePlayers.add(GamePlayer.get(Bukkit.getPlayerExact(results.get(i)).getUniqueId()));
                            }

                            if (gameState == GameState.RUNNING) return;
                            if (gamePlayers.size() == 1) return;

                            GameParty gameParty = new GameParty(Game.this, GamePlayer.get(player1.getUniqueId()), gamePlayers);
                            broadcastMessage("§a队长§e" + result[1] + "§a带着他的队伍§e" + teamName + "§a加入了");

                            GameTeam lowest = getLowestTeam();
                            for (GamePlayer gamePlayer1 : gameParty.getPlayers()) {
                                if (gamePlayer1.getGameTeam() == null) {
                                    if (!lowest.addPlayer(gamePlayer1)) {
                                        lowest = getLowestTeam();
                                    }
                                }
                            }
                        }).run()
        );

        TitleUtil.sendTitle(player, 0, 30, 5, "§e§l凋零起床战争", mapData.getAuthor().equals("unknown") ? "§b游戏地址: PLAY.MCYC.WIN" : "§b建筑师: " + mapData.getAuthor() + " 游戏地址: PLAY.MCYC.WIN");
        broadcastMessage("§7" + ChatColor.translateAlternateColorCodes('&', main.getChat().getPlayerPrefix(player)).replace("[VIP]", "") + gamePlayer.getDisplayname() + "§e加入游戏!");
        NametagEdit.getApi().setPrefix(player, main.getChat().getPlayerPrefix(player).replace("[VIP]", ""));
        if (!gamePlayer.getName().equals(gamePlayer.getDisplayname())) {
            NametagEdit.getApi().setSuffix(player, "" + gamePlayer.getDisplayname() + "");
        }

        player.teleport(waitingLocation);

        LobbyBoard.show(player);
        LobbyBoard.updateBoard();

       // player.getInventory().addItem(new ItemBuilderUtil().setType(Material.PAPER).setDisplayName("§a资源类型选择§7(右键选择)").getItem());
        player.getInventory().addItem(new ItemBuilderUtil().setType(Material.BED).setDisplayName("§a团队选择§7(右键选择)").getItem());
        player.getInventory().setItem(8, new ItemBuilderUtil().setType(Material.SLIME_BALL).setDisplayName("§c离开游戏§7(右键离开)").getItem());

        if (isStartable()) {
            if (gameState == GameState.WAITING && getGameLobbyCountdown() == null) {
                GameLobbyCountdown lobbyCountdown = new GameLobbyCountdown(this);
                lobbyCountdown.runTaskTimer(main, 20L, 20L);
                setGameLobbyCountdown(lobbyCountdown);
            }
        }
    }

    public void removePlayers(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        if (gameState == GameState.WAITING) {
            broadcastMessage("§7" + gamePlayer.getDisplayname() + "§e离开游戏");
            if (gamePlayer.getGameTeam() != null) gamePlayer.setGameTeam(null);
        }

        if (gameState == GameState.RUNNING && gamePlayer.isSpectator()) {
            return;
        }

        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }

        if (gameTeam.isWitherDead()) {
            Bukkit.getPluginManager().callEvent(new RejoinGameDeathEvent(player));
        }

        if (gameTeam.getAlivePlayers().isEmpty()) {
            if (!gameTeam.isWitherDead()) {
                gameTeam.setWitherDead(true);
            }
        }
    }

    public GameParty getPlayerParty(GamePlayer gamePlayer) {
        for (GameParty gameParty : gameParties) {
            if (gameParty.isInTeam(gamePlayer)) {
                return gameParty;
            }
        }
        return null;
    }

    public void addParty(GameParty gameParty) {
        gameParties.add(gameParty);
    }

    public void removeParty(GameParty gameParty) {
        gameParties.remove(gameParty);
    }

    public int getMaxPlayers() {
        return mapData.getTeams().size() * mapData.getPlayers().getTeam();
    }

    boolean hasEnoughPlayers() {
        return GamePlayer.getOnlinePlayers().size() >= mapData.getPlayers().getMin();
    }

    public GameTeam getLowestTeam() {
        GameTeam lowest = null;
        for (GameTeam gameTeam : gameTeams) {
            if (lowest == null) {
                lowest = gameTeam;
                continue;
            }

            if (!gameTeam.isFull() && gameTeam.getGamePlayers().size() < lowest.getGamePlayers().size()) {
                lowest = gameTeam;
            }
        }

        return lowest;
    }


    public void moveFreePlayersToTeam() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            if (gamePlayer.getGameTeam() == null) {
                GameTeam lowest = getLowestTeam();
                lowest.addPlayer(gamePlayer);
            }
        }
    }

    public void teleportPlayersToTeamSpawn() {
        for (GameTeam gameTeam : this.gameTeams) {
            for (GamePlayer gamePlayer : gameTeam.getAlivePlayers()) {
                Player player = gamePlayer.getPlayer();

                player.setVelocity(new Vector(0, 0, 0));
                player.setFallDistance(0.0F);
                player.teleport(gameTeam.getSpawn());
            }
        }
    }

    public GameTeam getTeam(TeamColor teamColor) {
        for (GameTeam gameTeam : gameTeams) {
            if (gameTeam.getTeamColor() == teamColor) {
                return gameTeam;
            }
        }
        return null;
    }

    public void addSpecialItem(SpecialItem specialItem) {
        this.specialItems.add(specialItem);
    }

    public void removeSpecialItem(SpecialItem specialItem) {
        this.specialItems.remove(specialItem);
    }

    public void broadcastTitle(Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> gamePlayer.sendTitle(fadeIn, stay, fadeOut, title, subTitle));
    }

    public void broadcastTeamTitle(GameTeam gameTeam, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> gamePlayer.sendTitle(fadeIn, stay, fadeOut, title, subTitle));
    }

    public void broadcastTeamMessage(GameTeam gameTeam, String... texts) {
        gameTeam.getAlivePlayers().forEach(player -> Arrays.asList(texts).forEach(player::sendMessage));
    }

    public void broadcastTeamSound(GameTeam gameTeam, Sound sound, float v, float v1) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> gamePlayer.playSound(sound, v, v1));
    }


    public void broadcastTeamActionBar(GameTeam gameTeam, String s) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> gamePlayer.sendActionBar(s));
    }

    public void broadcastSpectatorMessage(String... texts) {
        GamePlayer.getSpectators().forEach(gamePlayer -> Arrays.asList(texts).forEach(gamePlayer::sendMessage));
    }

    public void broadcastMessage(String... texts) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> Arrays.asList(texts).forEach(gamePlayer::sendMessage));
    }

    public void broadcastSound(Sound sound, float v, float v1) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> gamePlayer.playSound(sound, v, v1));
    }

    public boolean isStartable() {
        return (this.hasEnoughPlayers() && this.hasEnoughTeams());
    }

    public boolean hasEnoughTeams() {
        int teamsWithPlayers = 0;
        for (GameTeam gameTeam : gameTeams) {
            if (gameTeam.getGamePlayers().size() > 0) {
                teamsWithPlayers++;
            }
        }

        List<GamePlayer> freePlayers = GamePlayer.getGamePlayers();
        freePlayers.removeAll(GamePlayer.getTeamPlayers());

        return (teamsWithPlayers > 1 || (teamsWithPlayers == 1 && freePlayers.size() >= 1) || (teamsWithPlayers == 0 && freePlayers.size() >= 2));
    }

    public String getFormattedTime(int time) {
        String minStr;
        String secStr;
        int min = (int) Math.floor(time / 60);
        int sec = time % 60;
        minStr = min < 10 ? "0" + min : String.valueOf(min);
        secStr = sec < 10 ? "0" + sec : String.valueOf(sec);
        return minStr + ":" + secStr;
    }


    public void start() {
        gameState = GameState.RUNNING;
        Bukkit.getPluginManager().callEvent(new ACKGameStartEvent());

        moveFreePlayersToTeam();
        eventManager.start();

        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Player player = gamePlayer.getPlayer();

            if (gamePlayer.getGameTeam() == null) {
                player.kickPlayer("");
                continue;
            }

            Bukkit.getPluginManager().callEvent(new RejoinPlayerJoinEvent(player));
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setGameMode(GameMode.SURVIVAL);
            gamePlayer.clean();
        }

        teleportPlayersToTeamSpawn();

        getGameTeams().forEach(team -> {
            if (team.getGamePlayers().isEmpty()) {
                team.setWitherDead(true);
            }else {
                team.spawnWither();
            }
        });

        Bukkit.getPluginManager().callEvent(new BedwarsGameStartEvent());
    }


    public boolean isOver() {
        int alives = 0;
        for (GameTeam gameTeam : gameTeams) {
            if (!gameTeam.isDead()) {
                alives += 1;
            }
        }
        return alives <= 1;
    }

    public GameTeam getWinner() {
        for (GameTeam team : gameTeams) {
            if (!team.isDead()) {
                return team;
            }
        }
        return null;
    }

    public GamePlayer findTargetPlayer(GamePlayer gamePlayer) {
        GamePlayer foundPlayer = null;
        double distance = Double.MAX_VALUE;

        ArrayList<GamePlayer> possibleTargets = new ArrayList<>(GamePlayer.getOnlinePlayers());
        possibleTargets.removeAll(gamePlayer.getGameTeam().getGamePlayers());
        possibleTargets.removeIf(GamePlayer::isSpectator);


        for (GamePlayer player1 : possibleTargets) {
            if (gamePlayer.getPlayer().getWorld() != player1.getPlayer().getWorld()) {
                continue;
            }

            double dist = gamePlayer.getPlayer().getLocation().distance(player1.getPlayer().getLocation());
            if (dist < distance) {
                foundPlayer = player1;
                distance = dist;
            }
        }

        return foundPlayer;
    }

    public GameTeam getTeamByName(String name) {
        for (GameTeam gameTeam : gameTeams) {
            if (ChatColor.stripColor(gameTeam.getTeamColor().getName()).equals(name)) {
                return gameTeam;
            }
        }
        return null;
    }
}
