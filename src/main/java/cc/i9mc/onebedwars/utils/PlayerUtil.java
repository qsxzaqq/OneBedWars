package cc.i9mc.onebedwars.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtil {
    public static List<Player> getNearbyPlayers(Entity entity, double radius) {
        List<Player> players = new ArrayList<>();
        for (Entity e : entity.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Player) {
                players.add((Player) e);
            }
        }
        return players;
    }

    public static List<Player> getNearbyPlayers(Location location, double radius) {
        List<Player> players = new ArrayList<>();
        for (Entity e : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (e instanceof Player && e.getLocation().distance(location) <= radius) {
                players.add((Player) e);
            }
        }
        return players;
    }
}
