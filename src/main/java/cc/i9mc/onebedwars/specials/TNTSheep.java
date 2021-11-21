package cc.i9mc.onebedwars.specials;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.game.GameState;
import cc.i9mc.onebedwars.game.GameTeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;
import org.bukkit.scheduler.BukkitRunnable;

public class TNTSheep extends SpecialItem {
    private Game game = null;
    private Player player = null;
    private ITNTSheep sheep = null;

    @Override
    public Material getActivatedMaterial() {
        return null;
    }

    public int getEntityTypeId() {
        return 91;
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public Material getItemMaterial() {
        return Material.MONSTER_EGG;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ITNTSheep getSheep() {
        return this.sheep;
    }

    public void run(Location startLocation) {

        ItemStack usedStack = null;

        usedStack = player.getInventory().getItemInHand();
        if (((SpawnEgg) usedStack.getData()).getSpawnedType() != EntityType.SHEEP) {
            return;
        }
        usedStack.setAmount(usedStack.getAmount() - 1);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), usedStack);
        player.updateInventory();

        final GamePlayer gamePlayer = GamePlayer.get(this.player.getUniqueId());
        final GameTeam playerTeam = gamePlayer.getGameTeam();
        GamePlayer targetPlayer = game.findTargetPlayer(gamePlayer);
        if (targetPlayer == null) {
            player.sendMessage("§c没有目标");
            return;
        }

        // as task
        new BukkitRunnable() {

            @Override
            public void run() {
                final TNTSheep that = TNTSheep.this;

                try {
                    ITNTSheepRegister register = new ITNTSheepRegister();
                    TNTSheep.this.sheep = register.spawnCreature(that, startLocation, TNTSheep.this.player, targetPlayer.getPlayer(), playerTeam.getTeamColor().getDyeColor());

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            that.getSheep().getTNT().remove();
                            that.getSheep().remove();
                            that.getGame().removeSpecialItem(that);
                        }
                    }.runTaskLater(OneBedwars.getInstance(), (5 * 20) + 13);

                    TNTSheep.this.game.addSpecialItem(that);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.runTask(OneBedwars.getInstance());
    }

    public void updateTNT() {
        new BukkitRunnable() {

            @Override
            public void run() {
                final TNTSheep that = TNTSheep.this;

                if (that.game.getGameState() != GameState.RUNNING) {
                    return;
                }

                if (that.sheep == null) {
                    return;
                }

                if (that.sheep.getTNT() == null) {
                    return;
                }

                TNTPrimed old = that.sheep.getTNT();
                final int fuse = old.getFuseTicks();

                if (fuse <= 0) {
                    return;
                }

                final Entity source = old.getSource();
                final Location oldLoc = old.getLocation();
                final float yield = old.getYield();
                old.leaveVehicle();
                old.remove();

                new BukkitRunnable() {

                    @Override
                    public void run() {
                        TNTPrimed primed = (TNTPrimed) that.getGame().getMapData().getTeams().get(0).getSpawn().toLocation().getWorld().spawnEntity(oldLoc, EntityType.PRIMED_TNT);
                        primed.setFuseTicks(fuse);
                        primed.setYield(yield);
                        primed.setIsIncendiary(false);
                        that.sheep.setPassenger(primed);
                        that.sheep.setTNT(primed);
                        that.sheep.setTNTSource(source);

                        if (primed.getFuseTicks() >= 60) {
                            that.updateTNT();
                        }
                    }
                }.runTaskLater(OneBedwars.getInstance(), 3L);
            }

        }.runTaskLater(OneBedwars.getInstance(), 60L);
    }

}
