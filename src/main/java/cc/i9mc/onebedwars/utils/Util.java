package cc.i9mc.onebedwars.utils;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.game.*;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.nametagedit.plugin.NametagEdit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {
    public static int randInt(int min, int max) {
        Random rand = new Random();

        return rand.nextInt((max - min) + 1) + min;
    }

    public static Location getDirectionLocation(Location location, int blockOffset) {
        Location loc = location.clone();
        return loc.add(loc.getDirection().setY(0).normalize().multiply(blockOffset));
    }

    public static boolean isNumber(String numberString) {
        try {
            Integer.parseInt(numberString);
            return true;
        } catch (Exception ex) {
            // NO ERROR
            return false;
        }
    }

    public static boolean isColorable(ItemStack itemstack) {
        return (itemstack.getType().equals(Material.STAINED_CLAY) || itemstack.getType().equals(Material.WOOL) || itemstack.getType().equals(Material.CARPET) || itemstack.getType().equals(Material.STAINED_GLASS) || itemstack.getType().equals(Material.STAINED_GLASS_PANE));
    }

    public static BlockFace getCardinalDirection(Location location) {
        double rotation = (location.getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return BlockFace.NORTH;
        } else if (22.5 <= rotation && rotation < 67.5) {
            return BlockFace.NORTH_EAST;
        } else if (67.5 <= rotation && rotation < 112.5) {
            return BlockFace.EAST;
        } else if (112.5 <= rotation && rotation < 157.5) {
            return BlockFace.SOUTH_EAST;
        } else if (157.5 <= rotation && rotation < 202.5) {
            return BlockFace.SOUTH;
        } else if (202.5 <= rotation && rotation < 247.5) {
            return BlockFace.SOUTH_WEST;
        } else if (247.5 <= rotation && rotation < 292.5) {
            return BlockFace.WEST;
        } else if (292.5 <= rotation && rotation < 337.5) {
            return BlockFace.NORTH_WEST;
        } else if (337.5 <= rotation && rotation < 360.0) {
            return BlockFace.NORTH;
        } else {
            return BlockFace.NORTH;
        }
    }

    public static void dropTargetBlock(Block targetBlock) {
        if (targetBlock.getType().equals(Material.BED_BLOCK)) {
            Block bedHead;
            Block bedFeet;
            Bed bedBlock = (Bed) targetBlock.getState().getData();

            if (!bedBlock.isHeadOfBed()) {
                bedFeet = targetBlock;
                bedHead = getBedNeighbor(bedFeet);
            } else {
                bedHead = targetBlock;
                bedFeet = getBedNeighbor(bedHead);
            }

            bedHead.setType(Material.AIR);
        } else {
            targetBlock.setType(Material.AIR);
        }
    }

    private static Block getBedNeighbor(Block head) {
        if (isBedBlock(head.getRelative(BlockFace.EAST))) {
            return head.getRelative(BlockFace.EAST);
        } else if (isBedBlock(head.getRelative(BlockFace.WEST))) {
            return head.getRelative(BlockFace.WEST);
        } else if (isBedBlock(head.getRelative(BlockFace.SOUTH))) {
            return head.getRelative(BlockFace.SOUTH);
        } else {
            return head.getRelative(BlockFace.NORTH);
        }
    }

    private static boolean isBedBlock(Block isBed) {
        if (isBed == null) {
            return false;
        }

        return (isBed.getType() == Material.BED || isBed.getType() == Material.BED_BLOCK);
    }

    public static void setFlying(Player player) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ABILITIES);
        packet.getModifier().writeDefaults();
        packet.getFloat().write(0, 0.05F);
        packet.getBooleans().write(1, true);
        packet.getBooleans().write(2, true);
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    public static void spawnParticle(List<GamePlayer> gamePlayers, Location loc) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getModifier().writeDefaults();
        packet.getParticles().write(0, EnumWrappers.Particle.FIREWORKS_SPARK);
        packet.getBooleans().write(0, false);
        packet.getFloat().write(0, (float) loc.getX());
        packet.getFloat().write(1, (float) loc.getY());
        packet.getFloat().write(2, (float) loc.getZ());
        packet.getFloat().write(3, 0.0F);
        packet.getFloat().write(4, 0.0F);
        packet.getFloat().write(5, 0.0F);
        packet.getFloat().write(6, 0.0F);
        packet.getIntegers().write(0, 1);
        gamePlayers.forEach(gamePlayer -> {
            try {
                protocolManager.sendServerPacket(gamePlayer.getPlayer(), packet);
            } catch (InvocationTargetException e1) {
                e1.printStackTrace();
            }
        });
    }

    public static Vector getPosition(Location location1, Location location2, double Y) {
        double X = location1.getX() - location2.getX();
        double Z = location1.getZ() - location2.getZ();
        return new Vector(X, Y, Z);
    }

    public static void setPlayerTeamTab() {
        Game game = OneBedwars.getInstance().getGame();

        for (TeamColor teamColor : TeamColor.values()) {
            GameTeam gameTeam = game.getTeam(teamColor);
            if (gameTeam == null) {
                continue;
            }

            gameTeam.getAlivePlayers().forEach(gamePlayer -> {
                Player player = gamePlayer.getPlayer();
                NametagEdit.getApi().clearNametag(player);
                NametagEdit.getApi().setNametag(player, gameTeam.getName() + " ", gamePlayer.getName().equals(gamePlayer.getDisplayname()) ? "" : gamePlayer.getDisplayname());
            });

            if(game.getGameState() == GameState.WAITING){
                List<GamePlayer> players = new ArrayList<>(GamePlayer.getGamePlayers());
                players.removeAll(GamePlayer.getTeamPlayers());
                players.forEach(player -> {
                    if(player.isOnline()) {
                        NametagEdit.getApi().clearNametag(player.getPlayer());
                        NametagEdit.getApi().setNametag(player.getPlayer(), OneBedwars.getInstance().getChat().getPlayerPrefix(player.getPlayer()).replace("[VIP]", ""), player.getName().equals(player.getDisplayname()) ? "" : player.getDisplayname());
                    }
                });
            }
        }
    }
}
