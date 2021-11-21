package cc.i9mc.onebedwars.specials;

import cc.i9mc.onebedwars.specials.w.TNTSheepRegister;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntitySheep;
import net.minecraft.server.v1_8_R3.EntityTNTPrimed;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityTargetEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ITNTSheep extends EntitySheep {
    private final World world;
    private TNTPrimed primedTnt = null;

    public ITNTSheep(Location location, Player target) {
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

        this.goalSelector.a(0, new TNTSheepRegister.PathfinderGoalBedwarsPlayer(this, EntityHuman.class, 1D, false));
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
            sourceField.set(((CraftTNTPrimed) this.primedTnt).getHandle(),
                    ((CraftEntity) source).getHandle());
        } catch (Exception ignored) {
        }
    }

}
