package cc.i9mc.onebedwars.game;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.utils.PlayerUtil;
import cc.i9mc.onebedwars.utils.SoundUtil;
import com.google.common.base.Predicate;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class TeamWither extends EntityWither {
    private static final Predicate<Entity> bq = new Predicate<Entity>() {
        public boolean a(Entity entity) {
            return ((entity instanceof EntityLiving)) && (((EntityLiving) entity).getMonsterType() != EnumMonsterType.UNDEAD);
        }

        public boolean apply(Entity entity) {
            return a(entity);
        }
    };
    private final Game game;
    private int[] bn = new int[2];
    private int[] bo = new int[2];
    private int bp;
    private GameTeam gameTeam = null;
    private boolean warning = false;
    private boolean deepRed = true;
    private Map<GamePlayer, Float> damagerList = new HashMap<>();

    public TeamWither(World world) {
        super(world);
        game = OneBedwars.getInstance().getGame();
    }

    public void setTeam(GameTeam gameTeam) {
        if (gameTeam == null) {
            this.gameTeam = gameTeam;
        }
        setCustomName(gameTeam.getTeamColor().getChatColor() + gameTeam.getTeamColor().getName() + " §l凋零");
        setCustomNameVisible(true);
    }

    public GameTeam getGameTeam() {
        if (gameTeam != null) {
            return gameTeam;
        }
        String name = ChatColor.stripColor(getCustomName()).substring(0, 1);
        GameTeam gameTeam = game.getTeamByName(name);
        if (this.gameTeam == null) {
            this.gameTeam = gameTeam;
        }
        return gameTeam;
    }

    @Override
    public EntityLiving getGoalTarget() {
        for (Player player : PlayerUtil.getNearbyPlayers(getBukkitEntity().getLocation(), 10D)) {
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            if (gamePlayer.isSpectator() || getGameTeam().isInTeam(gamePlayer)) {
                continue;
            }
            return ((CraftPlayer) player).getHandle();
        }
        return null;
    }

    @Override
    protected void E() {
        if (cl() > 0) {
            int i = cl() - 1;
            if (i <= 0) {
                ExplosionPrimeEvent event = new ExplosionPrimeEvent(getBukkitEntity(), 7.0F, false);
                this.world.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    this.world.createExplosion(this, this.locX, this.locY + getHeadHeight(), this.locZ, event.getRadius(), event.getFire(), this.world.getGameRules().getBoolean("mobGriefing"));
                }

                int viewDistance = this.world.getServer().getViewDistance() * 16;
                for (EntityPlayer player : MinecraftServer.getServer().getPlayerList().players) {
                    double deltaX = this.locX - player.locX;
                    double deltaZ = this.locZ - player.locZ;
                    double distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
                    if ((this.world.spigotConfig.witherSpawnSoundRadius <= 0) || (distanceSquared <= this.world.spigotConfig.witherSpawnSoundRadius * this.world.spigotConfig.witherSpawnSoundRadius)) {
                        if (distanceSquared > viewDistance * viewDistance) {
                            double deltaLength = Math.sqrt(distanceSquared);
                            double relativeX = player.locX + deltaX / deltaLength * viewDistance;
                            double relativeZ = player.locZ + deltaZ / deltaLength * viewDistance;
                            player.playerConnection.sendPacket(new PacketPlayOutWorldEvent(1013, new BlockPosition((int) relativeX, (int) this.locY, (int) relativeZ), 0, true));
                        } else {
                            player.playerConnection.sendPacket(new PacketPlayOutWorldEvent(1013, new BlockPosition((int) this.locX, (int) this.locY, (int) this.locZ), 0, true));
                        }
                    }
                }
            }

            r(i);
            if (this.ticksLived % 10 == 0) {
                heal(10.0F, EntityRegainHealthEvent.RegainReason.WITHER_SPAWN);
            }
        } else {
            if (getGoalTarget() != null) {
                b(0, getGoalTarget().getId());
            } else {
                b(0, 0);
            }

            if (this.bp > 0) {
                this.bp -= 1;
                if ((this.bp == 0) && (this.world.getGameRules().getBoolean("mobGriefing"))) {
                    int i = MathHelper.floor(this.locY);
                    int j = MathHelper.floor(this.locX);
                    int j1 = MathHelper.floor(this.locZ);
                    boolean flag = false;

                    for (int k1 = -1; k1 <= 1; k1++) {
                        for (int l1 = -1; l1 <= 1; l1++) {
                            for (int i2 = 0; i2 <= 3; i2++) {
                                int j2 = j + k1;
                                int k2 = i + i2;
                                int l2 = j1 + l1;
                                BlockPosition blockposition = new BlockPosition(j2, k2, l2);
                                Block block = this.world.getType(blockposition).getBlock();

                                if ((block.getMaterial() != Material.AIR) && (a(block))) {
                                    if (!CraftEventFactory.callEntityChangeBlockEvent(this, j2, k2, l2, Blocks.AIR, 0).isCancelled()) {
                                        flag = (this.world.setAir(blockposition, true)) || (flag);
                                    }
                                }
                            }
                        }
                    }

                    if (flag) {
                        this.world.a(null, 1012, new BlockPosition(this), 0);
                    }
                }
            }

            if (this.ticksLived % 20 == 0) {
                heal(1.0F, EntityRegainHealthEvent.RegainReason.REGEN);
            }
        }
    }

    private void a(int i, EntityLiving entityliving) {
        a(i, entityliving.locX, entityliving.locY + entityliving.getHeadHeight() * 0.5D, entityliving.locZ, (i == 0) && (this.random.nextFloat() < 0.001F));
    }

    private void a(int i, double d0, double d1, double d2, boolean flag) {
        this.world.a(null, 1014, new BlockPosition(this), 0);
        double d3 = t(i);
        double d4 = u(i);
        double d5 = v(i);
        double d6 = d0 - d3;
        double d7 = d1 - d4;
        double d8 = d2 - d5;
        EntityWitherSkull entitywitherskull = new EntityWitherSkull(this.world, this, d6, d7, d8);
        if (flag) {
            entitywitherskull.setCharged(true);
        }

        entitywitherskull.locY = d4;
        entitywitherskull.locX = d3;
        entitywitherskull.locZ = d5;
        this.world.addEntity(entitywitherskull);
    }

    private double t(int i) {
        if (i <= 0) {
            return this.locX;
        }
        float f = (this.aI + 180 * (i - 1)) / 180.0F * 3.141593F;
        float f1 = MathHelper.cos(f);

        return this.locX + f1 * 1.3D;
    }

    private double u(int i) {
        return i <= 0 ? this.locY + 3.0D : this.locY + 2.2D;
    }

    private double v(int i) {
        if (i <= 0) {
            return this.locZ;
        }
        float f = (this.aI + 180 * (i - 1)) / 180.0F * 3.141593F;
        float f1 = MathHelper.sin(f);

        return this.locZ + f1 * 1.3D;
    }

    @Override
    public void move(double d0, double d1, double d2) {

    }

    @Override
    protected void dropDeathLoot(boolean flag, int i) {

    }

    @Override
    public void die() {
        game.broadcastSound(SoundUtil.get("ENDERDRAGON_GROWL", "ENDERDRAGON_GROWL"), 1, 1);
        game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        game.broadcastMessage(" ");
        game.broadcastMessage("§c§l" + getGameTeam().getName() + " §a的凋零阵亡了!");
        game.broadcastMessage(" ");
        game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        game.broadcastTeamTitle(getGameTeam(), 1, 20, 1, "§c§l凋零阵亡", "§c死亡将无法复活");
        getGameTeam().setWitherDead(true);

        boolean allDead = true;
        for (GameTeam gameTeam : game.getGameTeams()) {
            if (!gameTeam.isWitherDead()) {
                allDead = false;
                break;
            }
        }

        if (!game.isWorldBorder() && allDead) {
            game.getEventManager().setExcuteSeconds();
            game.broadcastTitle(10, 40, 10, "§c所有的凋零都死了！", "§f边界开启收缩！");
            game.broadcastMessage("§c§l所有的凋零已经死亡！边界开启收缩！");
        }

        for (GamePlayer gamePlayer : damagerList.keySet()) {
            new BukkitRunnable() {
                int i = 0;

                @Override
                public void run() {
                    if (i == 5) {
                        cancel();
                        return;
                    }
                    gamePlayer.sendActionBar("§6+3个金币");
                    i++;
                }
            }.runTaskTimerAsynchronously(OneBedwars.getInstance(), 0, 10);
            gamePlayer.sendMessage("§6+3个金币 (凋零伤害奖励)");
            OneBedwars.getInstance().getEcon().depositPlayer(gamePlayer.getPlayer(), 1);
        }

        super.die();
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.equals(DamageSource.GENERIC)) {
            return super.damageEntity(damagesource, f);
        } else if (damagesource != null && damagesource.getEntity() != null && damagesource.getEntity().getBukkitEntity() != null && damagesource.getEntity().getBukkitEntity() instanceof CraftPlayer) {
            Player player = ((Player) damagesource.getEntity().getBukkitEntity());
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            if (!gamePlayer.isSpectator() && !getGameTeam().isInTeam(gamePlayer)) {
                if (!warning) {
                    warning = true;
                    deepRed = !deepRed;
                    game.broadcastTeamMessage(getGameTeam(), (deepRed ? "§4" : "§c") + "§l我方凋零正在被攻击！");
                    game.broadcastTeamTitle(getGameTeam(), 0, 40, 0,null, (deepRed ? "§4" : "§c") + "§l我方凋零正在被攻击！");
                    game.broadcastTeamActionBar(getGameTeam(), (deepRed ? "§4" : "§c") + "§l我方凋零正在被攻击！");
                    game.broadcastTeamSound(getGameTeam(), Sound.NOTE_PLING, 1F, 2F);

                    Bukkit.getScheduler().runTaskLater(OneBedwars.getInstance(), () -> {
                        game.broadcastTeamSound(getGameTeam(), Sound.NOTE_PLING, 1F, 3F);
                        game.broadcastTeamActionBar(getGameTeam(), (deepRed ? "§4" : "§c") + "§l我方凋零正在被攻击！");
                    }, 11L);
                    Bukkit.getScheduler().runTaskLater(OneBedwars.getInstance(), () -> {
                        game.broadcastTeamSound(getGameTeam(), Sound.NOTE_PLING, 1F, 3F);
                        game.broadcastTeamActionBar(getGameTeam(), (deepRed ? "§4" : "§c") + "§l我方凋零正在被攻击！");
                    }, 22L);
                    Bukkit.getScheduler().runTaskLater(OneBedwars.getInstance(), () -> warning = false, 60L);
                }
                damagerList.put(gamePlayer, damagerList.getOrDefault(gamePlayer, 0F) + (f / 2F));
                return super.damageEntity(damagesource, f / 2F);
            }
        }
        return true;
    }
}
