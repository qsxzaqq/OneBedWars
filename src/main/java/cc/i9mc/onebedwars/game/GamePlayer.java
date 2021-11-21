package cc.i9mc.onebedwars.game;

import cc.i9mc.onebedwars.shop.NewItemShop;
import cc.i9mc.gameutils.utils.ActionBarUtil;
import cc.i9mc.gameutils.utils.ItemBuilderUtil;
import cc.i9mc.gameutils.utils.TitleUtil;
import cc.i9mc.gameutils.utils.board.Board;
import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.database.PlayerData;
import cc.i9mc.onebedwars.spectator.SpectatorSettings;
import cc.i9mc.onebedwars.spectator.SpectatorTarget;
import cc.i9mc.onebedwars.utils.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GamePlayer {
    private static final ConcurrentHashMap<UUID, GamePlayer> gamePlayers = new ConcurrentHashMap<>();

    @Getter
    private final UUID uuid;
    @Getter
    private final String name;
    @Getter
    private final AssistsMap assistsMap;
    @Getter
    private final PlayerData playerData;
    @Setter
    @Getter
    private String displayname;
    @Getter
    @Setter
    private Board board;
    @Getter
    private boolean spectator;
    @Getter
    @Setter
    private SpectatorTarget spectatorTarget;
    @Getter
    @Setter
    private GameTeam gameTeam;
    @Getter
    private final PlayerCompass playerCompass;
    @Getter
    private int kills;
    @Getter
    private int finalKills;
    @Getter
    @Setter
    private NewItemShop newItemShop;

    public GamePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        assistsMap = new AssistsMap(this);
        playerData = new PlayerData(this);
        playerCompass = new PlayerCompass(this);
    }

    public static GamePlayer create(UUID uuid, String name) {
        GamePlayer gamePlayer = get(uuid);
        if (gamePlayer != null) {
            return gamePlayer;
        }
        gamePlayer = new GamePlayer(uuid, name);
        gamePlayers.put(uuid, gamePlayer);
        return gamePlayer;
    }

    public static GamePlayer get(UUID uuid) {
        for (GamePlayer gamePlayer : gamePlayers.values()) {
            if (gamePlayer.getUuid().equals(uuid)) {
                return gamePlayer;
            }
        }
        return null;
    }

    public static List<GamePlayer> getGamePlayers() {
        return new ArrayList<>(gamePlayers.values());
    }

    public static List<GamePlayer> getTeamPlayers() {
        List<GamePlayer> teamPlayers = new ArrayList<>();
        for (GamePlayer gamePlayer : gamePlayers.values()) {
            if (gamePlayer.getGameTeam() != null) {
                teamPlayers.add(gamePlayer);
            }
        }
        return teamPlayers;
    }

    public static List<GamePlayer> getOnlinePlayers() {
        List<GamePlayer> onlinePlayers = new ArrayList<>();
        for (GamePlayer gamePlayer : gamePlayers.values()) {
            if (gamePlayer.isOnline()) {
                onlinePlayers.add(gamePlayer);
            }
        }
        return onlinePlayers;
    }

    public static List<GamePlayer> getSpectators() {
        List<GamePlayer> spectators = new ArrayList<>();
        for (GamePlayer gamePlayer : gamePlayers.values()) {
            if (gamePlayer.isSpectator()) {
                spectators.add(gamePlayer);
            }
        }
        return spectators;
    }

    public static List<GamePlayer> sortFinalKills() {
        List<GamePlayer> list = new ArrayList<>(getOnlinePlayers());
        list.sort((player1, player2) -> player2.getFinalKills() - player1.getFinalKills());
        return list;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    public void sendActionBar(String message) {
        if (!isOnline()) return;
        ActionBarUtil.sendBar(getPlayer(), message);
    }

    public void sendTitle(int fadeIn, int stay, int fadeOut, String title, String subTitle) {
        if (!isOnline()) return;
        TitleUtil.sendTitle(getPlayer(), fadeIn, stay, fadeOut, title, subTitle);
    }

    public void sendMessage(String message) {
        if (!isOnline()) return;
        getPlayer().sendMessage(message);
    }

    public void playSound(Sound sound, float volume, float pitch) {
        if (!isOnline()) return;
        getPlayer().playSound(getPlayer().getLocation(), sound, volume, pitch);
    }

    public void setSpectator() {
        spectator = true;
    }

    public void toSpectator(String title, String subTitle) {
        spectator = true;
        spectatorTarget = new SpectatorTarget(this, null);

        Player player = getPlayer();
        sendTitle(10, 20, 10, title, subTitle);
        getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.getPlayer().hidePlayer(player));
        player.spigot().setCollidesWithEntities(false);
        player.setGameMode(GameMode.ADVENTURE);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        SpectatorSettings spectatorSettings = SpectatorSettings.get(this);
        if (spectatorSettings.getOption(SpectatorSettings.Option.NIGHTVISION)) {
            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
        }

        player.getInventory().setItem(0, new ItemBuilderUtil().setType(Material.COMPASS).setDisplayName("§a§l传送器§7(右键打开)").getItem());
        player.getInventory().setItem(4, new ItemBuilderUtil().setType(Material.REDSTONE_COMPARATOR).setDisplayName("§c§l旁观者设置§7(右键打开)").getItem());
        player.getInventory().setItem(7, new ItemBuilderUtil().setType(Material.PAPER).setDisplayName("§b§l快速加入§7(右键加入)").getItem());
        player.getInventory().setItem(8, new ItemBuilderUtil().setType(Material.SLIME_BALL).setDisplayName("§c§l离开游戏§7(右键离开)").getItem());

        player.setAllowFlight(true);
        Util.setFlying(player);
        player.teleport(OneBedwars.getInstance().getGame().getGameTeams().get(0).getSpawn());

        if (gameTeam != null && !gameTeam.getAlivePlayers().isEmpty()) {
            spectatorTarget.setTarget(gameTeam.getAlivePlayers().get(0));
        }
    }

    public void addKills() {
        kills += 1;
    }

    public void addFinalKills() {
        finalKills += 1;
    }

    public void setLastDamage(GamePlayer damager, long time) {
        assistsMap.setLastDamage(damager, time);
    }

    public void clean() {
        Player player = getPlayer();
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setExp(0.0F);
        player.setLevel(0);
        player.setSneaking(false);
        player.setSprinting(false);
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        player.setExhaustion(0.0f);
        player.setMaxHealth(20.0D);
        player.setHealth(20.0f);
        player.setFireTicks(0);

        PlayerInventory inv = player.getInventory();
        inv.setArmorContents(new ItemStack[4]);
        inv.setContents(new ItemStack[]{});
        player.getActivePotionEffects().forEach((potionEffect -> player.removePotionEffect(potionEffect.getType())));
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GamePlayer)) {
            return false;
        }

        GamePlayer gamePlayer = (GamePlayer) obj;
        return uuid.equals(gamePlayer.getUuid());
    }

    public static class PlayerCompass {
        @Getter
        private final GamePlayer gamePlayer;
        @Getter
        private final Player player;

        public PlayerCompass(GamePlayer gamePlayer) {
            this.gamePlayer = gamePlayer;
            this.player = gamePlayer.getPlayer();
        }

        public void sendClosestPlayer() {
            GamePlayer closestPlayer = OneBedwars.getInstance().getGame().findTargetPlayer(gamePlayer);

            if (closestPlayer != null) {
                gamePlayer.sendActionBar("§f玩家 " + closestPlayer.getGameTeam().getChatColor() + closestPlayer.getDisplayname() + " §f距离您 " + ((int) closestPlayer.getPlayer().getLocation().distance(player.getLocation())) + "m");
                player.setCompassTarget(closestPlayer.getPlayer().getLocation());
            } else {
                gamePlayer.sendActionBar("§c没有目标");
            }
        }
    }
}
