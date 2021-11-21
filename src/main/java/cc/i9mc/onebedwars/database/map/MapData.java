package cc.i9mc.onebedwars.database.map;

import cc.i9mc.onebedwars.game.TeamColor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MapData {
    private final Players players;
    private final Region region;
    private final List<Team> teams;
    private final List<DropLocation> drops;
    private final WorldBorder worldBorder;
    @Setter
    private transient String name;
    @Setter
    private String author;
    private Location reSpawn;

    public MapData() {
        this.players = new Players();
        this.region = new Region();
        this.teams = new ArrayList<>();
        this.drops = new ArrayList<>();
        this.worldBorder = new WorldBorder();
    }

    public void setReSpawn(org.bukkit.Location location) {
        Location rawLocation = new Location();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        reSpawn = rawLocation;
    }

    public void addTeam(TeamColor teamColor) {
        Team team = new Team();
        team.setColor(teamColor.toString());
        teams.add(team);
    }

    public Team getTeam(TeamColor teamColor) {
        for (Team team : teams) {
            if (team.getColor().equals(teamColor.toString())) {
                return team;
            }
        }

        return null;
    }

    public void setPos1(org.bukkit.Location location) {
        Location rawLocation = new Location();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        region.setPos1(rawLocation);
    }

    public void setPos2(org.bukkit.Location location) {
        Location rawLocation = new Location();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        region.setPos2(rawLocation);
    }

    public void addDrop(DropType dropType, org.bukkit.Location location) {
        DropLocation dropLocation = new DropLocation();
        dropLocation.setWorld(location.getWorld().getName());
        dropLocation.setX(location.getX());
        dropLocation.setY(location.getY());
        dropLocation.setZ(location.getZ());
        dropLocation.setPitch(location.getPitch());
        dropLocation.setYaw(location.getYaw());
        dropLocation.setDropType(dropType);
        this.drops.add(dropLocation);
    }

    public Integer getDrops(DropType dropType) {
        return Math.toIntExact(drops.stream().filter((e) -> e.getDropType() == dropType).count());
    }

    public List<org.bukkit.Location> getDropLocations(DropType dropType) {
        return drops.stream().filter((e) -> e.getDropType() == dropType).map(Location::toLocation).collect(Collectors.toList());
    }

    public List<org.bukkit.Location> loadMap() {
        List<org.bukkit.Location> blocks = new ArrayList<>();
        org.bukkit.Location pos1 = region.getPos1().toLocation();
        org.bukkit.Location pos2 = region.getPos2().toLocation();
        for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()); x <= Math.max(pos1.getBlockX(), pos2.getBlockX()); x++) {
            for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()); y <= Math.max(pos1.getBlockY(), pos2.getBlockY()); y++) {
                for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()); z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()); z++) {
                    Block block = new org.bukkit.Location(pos1.getWorld(), x, y, z).getBlock();

                    if (block != null) {
                        if (block.getType() == Material.AIR || block.getType() == Material.BED_BLOCK || block.getType() == Material.LONG_GRASS || block.getType() == Material.DEAD_BUSH) {
                            continue;
                        }
                        System.out.println(x + ", " + y + ", " + z);
                        blocks.add(block.getLocation());
                    }
                }
            }
        }

        return blocks;
    }

    public boolean hasRegion(org.bukkit.Location location) {
        org.bukkit.Location pos1 = region.getPos1().toLocation();
        org.bukkit.Location pos2 = region.getPos2().toLocation();

        int x1 = pos1.getBlockX();
        int x2 = pos2.getBlockX();
        int y1 = pos1.getBlockY();
        int y2 = pos2.getBlockY();
        int z1 = pos1.getBlockZ();
        int z2 = pos2.getBlockZ();

        int minY = Math.min(y1, y2) - 1;
        int maxY = Math.max(y1, y2) + 1;
        int minZ = Math.min(z1, z2) - 1;
        int maxZ = Math.max(z1, z2) + 1;
        int minX = Math.min(x1, x2) - 1;
        int maxX = Math.max(x1, x2) + 1;

        if (location.getX() > minX && location.getX() < maxX) {
            if (location.getY() > minY && location.getY() < maxY) {
                return !(location.getZ() > minZ) || !(location.getZ() < maxZ);
            }
        }
        return true;
    }

    public boolean chunkIsInRegion(double x, double z) {
        org.bukkit.Location pos1 = region.getPos1().toLocation();
        org.bukkit.Location pos2 = region.getPos2().toLocation();

        int x1 = pos1.getBlockX();
        int x2 = pos2.getBlockX();
        int z1 = pos1.getBlockZ();
        int z2 = pos2.getBlockZ();

        int minZ = Math.min(z1, z2) - 1;
        int maxZ = Math.max(z1, z2) + 1;
        int minX = Math.min(x1, x2) - 1;
        int maxX = Math.max(x1, x2) + 1;

        return (x >= minX && x <= maxX && z >= minZ && z <= maxZ);
    }

    public enum DropType {
        BRONZE, IRON, GOLD
    }

    @Data
    public static class Location {
        private String world;
        private double x;
        private double y;
        private double z;
        private float pitch;
        private float yaw;

        public org.bukkit.Location toLocation() {
            return new org.bukkit.Location(Bukkit.getWorld(world), x, y, z, pitch, yaw);
        }
    }

    @Data
    public static class DropLocation extends Location {
        private DropType dropType;
    }

    @Data
    public static class Team {
        private String color;
        private Location spawn;
        private Location wither;

        public void setSpawn(org.bukkit.Location location) {
            Location rawLocation = new Location();
            rawLocation.setWorld(location.getWorld().getName());
            rawLocation.setX(location.getX());
            rawLocation.setY(location.getY());
            rawLocation.setZ(location.getZ());
            rawLocation.setPitch(location.getPitch());
            rawLocation.setYaw(location.getYaw());
            spawn = rawLocation;
        }

        public void setWither(org.bukkit.Location location) {
            Location rawLocation = new Location();
            rawLocation.setWorld(location.getWorld().getName());
            rawLocation.setX(location.getX());
            rawLocation.setY(location.getY());
            rawLocation.setZ(location.getZ());
            rawLocation.setPitch(location.getPitch());
            rawLocation.setYaw(location.getYaw());
            wither = rawLocation;
        }
    }

    @Data
    public class Players {
        private Integer team;
        private Integer min;
    }

    @Data
    public class Region {
        private Location pos1;
        private Location pos2;
    }

    @Data
    public class WorldBorder {
        private int x;
        private int z;
        private int size;
        private int to;
        private int speed;
    }
}
