package cc.i9mc.onebedwars.specials.w;

import cc.i9mc.onebedwars.OneBedwars;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class TNTSheepRegister {

    public void registerEntities(int entityId) {
        try {
            Class<?> entityTypeClass = EntityTypes.class;
            Field c = entityTypeClass.getDeclaredField("c");
            c.setAccessible(true);
            HashMap c_map = (HashMap) c.get(null);
            c_map.put("TNTSheep", TNTSheep.class);

            Field d = entityTypeClass.getDeclaredField("d");
            d.setAccessible(true);
            HashMap d_map = (HashMap) d.get(null);
            d_map.put(TNTSheep.class, "TNTSheep");

            Field e = entityTypeClass.getDeclaredField("e");
            e.setAccessible(true);
            HashMap e_map = (HashMap) e.get(null);
            e_map.put(entityId, TNTSheep.class);

            Field f = entityTypeClass.getDeclaredField("f");
            f.setAccessible(true);
            HashMap f_map = (HashMap) f.get(null);
            f_map.put(TNTSheep.class, entityId);

            Field g = entityTypeClass.getDeclaredField("g");
            g.setAccessible(true);
            HashMap g_map = (HashMap) g.get(null);
            g_map.put("TNTSheep", entityId);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public TNTSheep spawnCreature(final cc.i9mc.onebedwars.specials.TNTSheep specialItem, final Location location, final Player owner, Player target, final DyeColor color) {
        final TNTSheep sheep = new TNTSheep(location, target);

        ((CraftWorld) location.getWorld()).getHandle().addEntity(sheep, SpawnReason.NATURAL);
        sheep.setPosition(location.getX(), location.getY(), location.getZ());
        ((CraftSheep) sheep.getBukkitEntity()).setColor(color);

        new BukkitRunnable() {

            @Override
            public void run() {
                TNTPrimed primedTnt = (TNTPrimed) location.getWorld()
                        .spawnEntity(location.add(0.0, 1.0, 0.0), EntityType.PRIMED_TNT);
                sheep.getBukkitEntity().setPassenger(primedTnt);
                sheep.setTNT(primedTnt);

                try {
                    Field sourceField = EntityTNTPrimed.class.getDeclaredField("source");
                    sourceField.setAccessible(true);
                    sourceField.set(((CraftTNTPrimed) primedTnt).getHandle(),
                            ((CraftLivingEntity) owner).getHandle());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                sheep.getTNT().setYield((float) (sheep.getTNT().getYield() * 2.0));
                sheep.getTNT().setFuseTicks(Math.round(5 * 20));
                sheep.getTNT().setIsIncendiary(false);
                specialItem.updateTNT();
            }
        }.runTaskLater(OneBedwars.getInstance(), 5L);

        return sheep;
    }

    public static class PathfinderGoalBedwarsPlayer extends PathfinderGoalMeleeAttack {
        private final EntityCreature creature;

        public PathfinderGoalBedwarsPlayer(EntityCreature name, Class<? extends net.minecraft.server.v1_8_R3.Entity> name2, double name3, boolean name4) {
            super(name, name2, name3, name4);
            this.creature = name;
        }

        @Override
        public void e() {
            this.creature.getNavigation().a(this.creature.getGoalTarget());
        }

    }

    public static class TNTSheep extends EntitySheep {
        private TNTPrimed primedTnt = null;
        private final World world;

        public TNTSheep(Location location, Player target) {
            super(((CraftWorld) location.getWorld()).getHandle());

            this.world = location.getWorld();

            this.locX = location.getX();
            this.locY = location.getY();
            this.locZ = location.getZ();

            try {
                Field b = this.goalSelector.getClass().getDeclaredField("b");
                b.setAccessible(true);
                b.set(this.goalSelector, new ArrayList<>());
                this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(128D);
                this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.6);
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.goalSelector.a(0, new PathfinderGoalBedwarsPlayer(this, EntityHuman.class, 1D, false));
            this.setGoalTarget(((CraftPlayer) target).getHandle(), EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, false);
            ((Creature) this.getBukkitEntity()).setTarget(target);
        }

        public Location getLocation() {
            return new Location(this.world, this.locX, this.locY, this.locZ);
        }

        public TNTPrimed getTNT() {
            return this.primedTnt;
        }

        public void setTNT(TNTPrimed tnt) {
            this.primedTnt = tnt;
        }

        public void remove() {
            this.getBukkitEntity().remove();
        }

        public void setPassenger(TNTPrimed tnt) {
            this.getBukkitEntity().setPassenger(tnt);
        }

        public void setTNTSource(Entity source) {
            if (source == null) {
                return;
            }

            try {
                Field sourceField = EntityTNTPrimed.class.getDeclaredField("source");
                sourceField.setAccessible(true);
                sourceField.set(((CraftTNTPrimed) this.primedTnt).getHandle(), ((CraftEntity) source).getHandle());
            } catch (Exception ignored) {
            }
        }

    }
}
