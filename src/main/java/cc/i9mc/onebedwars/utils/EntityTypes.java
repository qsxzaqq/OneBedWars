package cc.i9mc.onebedwars.utils;

import cc.i9mc.onebedwars.game.TeamWither;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.Map;

public enum EntityTypes {
    Wither("Wither", 64, TeamWither.class);

    EntityTypes(String name, int id, Class<? extends Entity> custom) {
        addToMaps(custom, name, id);
    }

    public static void spawnEntity(Entity entity, Location loc) {
        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ((CraftWorld) loc.getWorld()).getHandle().addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    private static Object getPrivateField(String fieldName, Class clazz, Object object) {
        Field field;
        Object o = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            o = field.get(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }

    private static void addToMaps(Class clazz, String name, int id) {
        ((Map) getPrivateField("c", net.minecraft.server.v1_8_R3.EntityTypes.class, null))
                .put(name, clazz);
        ((Map) getPrivateField("d", net.minecraft.server.v1_8_R3.EntityTypes.class, null))
                .put(clazz, name);
        ((Map) getPrivateField("f", net.minecraft.server.v1_8_R3.EntityTypes.class, null))
                .put(clazz, id);
    }
}
