package cc.i9mc.onebedwars.specials;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.utils.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ProtectionWall extends SpecialItem {

    private final List<Block> wallBlocks;
    private Game game;
    private int livingTime = 0;
    private Player owner;

    public ProtectionWall() {
        super();
        this.wallBlocks = new ArrayList<>();
        this.owner = null;
        this.game = null;
    }

    public void create(Player player, Game game) {
        this.owner = player;
        this.game = game;

        if (player.getEyeLocation().getBlock().getType() != Material.AIR) {
            player.sendMessage("§c你不能在这里使用保护墙!");
            return;
        }

        ArrayList<ProtectionWall> livingWalls = this.getLivingWalls();
        if (!livingWalls.isEmpty()) {
            for (ProtectionWall livingWall : livingWalls) {
                int waitLeft = 10 - livingWall.getLivingTime();
                if (waitLeft > 0) {
                    player.sendMessage("§c需要 §e" + waitLeft + "秒§c 你才能使用下一个保护墙!");
                    return;
                }
            }
        }

        Location wallLocation = Util.getDirectionLocation(player.getLocation(), 2);

        ItemStack usedStack = player.getInventory().getItemInHand();
        usedStack.setAmount(usedStack.getAmount() - 1);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), usedStack);
        player.updateInventory();

        BlockFace face = Util.getCardinalDirection(player.getLocation());
        int widthStart = (int) Math.floor(((double) 5) / 2.0);

        for (int w = widthStart * (-1); w < 5 - widthStart; w++) {
            for (int h = 0; h < 3; h++) {
                Location wallBlock = wallLocation.clone();

                switch (face) {
                    case SOUTH:
                    case NORTH:
                    case SELF:
                        wallBlock.add(0, h, w);
                        break;
                    case WEST:
                    case EAST:
                        wallBlock.add(w, h, 0);
                        break;
                    case SOUTH_EAST:
                        wallBlock.add(w, h, w);
                        break;
                    case SOUTH_WEST:
                        wallBlock.add(w, h, w * (-1));
                        break;
                    case NORTH_EAST:
                        wallBlock.add(w * (-1), h, w);
                        break;
                    case NORTH_WEST:
                        wallBlock.add(w * (-1), h, w * (-1));
                        break;
                    default:
                        wallBlock = null;
                        break;
                }

                if (wallBlock == null) {
                    continue;
                }

                Block block = wallBlock.getBlock();
                if (!block.getType().equals(Material.AIR)) {
                    continue;
                }

                block.setType(Material.SANDSTONE);
                this.wallBlocks.add(block);
            }
        }

        this.createTask();
        game.addSpecialItem(this);
    }

    private void createTask() {
        new BukkitRunnable() {

            @Override
            public void run() {
                ProtectionWall.this.livingTime++;

                if (ProtectionWall.this.livingTime == 10) {
                    for (Block block : ProtectionWall.this.wallBlocks) {
                        block.getChunk().load(true);
                        block.setType(Material.AIR);
                    }
                }

                if (ProtectionWall.this.livingTime >= 10) {
                    ProtectionWall.this.game.removeSpecialItem(ProtectionWall.this);
                    this.cancel();
                }
            }
        }.runTaskTimer(OneBedwars.getInstance(), 20L, 20L);
    }

    @Override
    public Material getActivatedMaterial() {
        return null;
    }

    public Game getGame() {
        return this.game;
    }

    @Override
    public Material getItemMaterial() {
        return Material.BRICK;
    }

    public int getLivingTime() {
        return this.livingTime;
    }

    private ArrayList<ProtectionWall> getLivingWalls() {
        ArrayList<ProtectionWall> livingWalls = new ArrayList<>();
        for (SpecialItem item : game.getSpecialItems()) {
            if (item instanceof ProtectionWall) {
                ProtectionWall wall = (ProtectionWall) item;
                if (wall.getOwner().equals(this.getOwner())) {
                    livingWalls.add(wall);
                }
            }
        }
        return livingWalls;
    }

    public Player getOwner() {
        return this.owner;
    }

    public List<Block> getWallBlocks() {
        return this.wallBlocks;
    }

}
