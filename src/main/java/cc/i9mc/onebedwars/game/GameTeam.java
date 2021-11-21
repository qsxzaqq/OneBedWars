package cc.i9mc.onebedwars.game;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.utils.EntityTypes;
import cc.i9mc.onebedwars.utils.PlayerUtil;
import lombok.Data;
import net.minecraft.server.v1_8_R3.DamageSource;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameTeam {
    private final TeamColor teamColor;
    private final Location spawn;
    private final Location wither;
    private int maxPlayers;
    private TeamWither teamWither = null;
    private boolean witherDead = false;

    private List<Block> teamChests;
    private Inventory inventory;

    public GameTeam(TeamColor teamColor, Location spawn, Location wither, int maxPlayers) {
        this.spawn = spawn;
        this.wither = wither;
        this.teamColor = teamColor;
        this.maxPlayers = maxPlayers;
        this.teamChests = new ArrayList<>();
    }

    public void spawnWither() {
        if (teamWither != null) {
            return;
        }

        if(!wither.getChunk().isLoaded()){
            wither.getChunk().load();
        }
        teamWither = new TeamWither(((CraftWorld) Bukkit.getWorld("world")).getHandle());
        teamWither.setTeam(this);
        Game game = OneBedwars.getInstance().getGame();
        double maxHealth = 200;
        ((Wither) teamWither.getBukkitEntity()).setMaxHealth(maxHealth);
        ((Wither) teamWither.getBukkitEntity()).setHealth(maxHealth);
        EntityTypes.spawnEntity(teamWither, wither);

        Bukkit.getScheduler().runTaskTimer(OneBedwars.getInstance(), new Runnable() {
            private int second = 0;

            @Override
            public void run() {
                if (!isWitherDead()) {
                    if (second == 5) {
                        if (teamWither.getHealth() >= 3F && game.getEventManager().currentEvent().getPriority() > 0) {
                            teamWither.damageEntity(DamageSource.GENERIC, 2F);
                        }
                        for (Player other : PlayerUtil.getNearbyPlayers(wither, 10D)) {
                            GamePlayer gameOther = GamePlayer.get(other.getUniqueId());
                            if (gameOther.isSpectator() || !isInTeam(gameOther)) {
                                continue;
                            }
                            other.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0));
                            other.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0));
                        }
                        second = 0;
                    }
                    teamWither.setCustomName(getTeamColor().getChatColor() + getTeamColor().getName() + " §l凋零");
                }
                second += 1;
            }
        }, 0L, 20L);
    }

    public ChatColor getChatColor() {
        return teamColor.getChatColor();
    }

    public DyeColor getDyeColor() {
        return teamColor.getDyeColor();
    }

    public Color getColor() {
        return teamColor.getColor();
    }

    public List<GamePlayer> getGamePlayers() {
        List<GamePlayer> gamePlayers = new ArrayList<>();
        for (GamePlayer gamePlayer : GamePlayer.getGamePlayers()) {
            if (gamePlayer.getGameTeam() == this) {
                gamePlayers.add(gamePlayer);
            }
        }

        return gamePlayers;
    }

    public List<GamePlayer> getAlivePlayers() {
        List<GamePlayer> alivePlayers = new ArrayList<>();
        for (GamePlayer gamePlayer : getGamePlayers()) {
            if (gamePlayer.isOnline() && !gamePlayer.isSpectator()) {
                alivePlayers.add(gamePlayer);
            }
        }
        return alivePlayers;
    }

    public boolean isInTeam(GamePlayer gamePlayer) {
        for (GamePlayer player : getGamePlayers()) {
            if (player.equals(gamePlayer)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInTeam(GamePlayer removePlayer, GamePlayer gamePlayer) {
        for (GamePlayer player : getGamePlayers()) {
            if (player.equals(gamePlayer) && !player.equals(removePlayer)) {
                return true;
            }
        }
        return false;
    }

    public boolean addPlayer(GamePlayer gamePlayer) {
        if (isFull() || isInTeam(gamePlayer)) {
            return false;
        }
        gamePlayer.setGameTeam(this);
        return true;
    }

    public boolean isFull() {
        return getGamePlayers().size() >= maxPlayers;
    }

    public boolean isDead() {
        for (GamePlayer gamePlayer : getGamePlayers()) {
            if ((gamePlayer.isOnline()) && (!gamePlayer.isSpectator())) {
                return false;
            }
        }
        return true;
    }

    public void equipPlayerWithLeather(Player player) {
        // helmet
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setColor(this.getColor());
        helmet.setItemMeta(meta);

        // chestplate
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        meta = (LeatherArmorMeta) chestplate.getItemMeta();
        meta.setColor(this.getColor());
        chestplate.setItemMeta(meta);

        // leggings
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        meta = (LeatherArmorMeta) leggings.getItemMeta();
        meta.setColor(this.getColor());
        leggings.setItemMeta(meta);

        // boots
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
        meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(this.getColor());
        boots.setItemMeta(meta);

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
        player.updateInventory();
    }

    public String getName() {
        return teamColor.getName();
    }

    public void removeChest(Block chest) {
        teamChests.remove(chest);
        if (teamChests.size() == 0) {
            this.setInventory(null);
        }
    }

    public void createTeamInventory() {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.ENDER_CHEST, "队伍箱子");
        this.setInventory(inventory);
    }
}