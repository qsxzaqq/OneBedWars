package cc.i9mc.onebedwars.game.timer;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.database.map.MapData;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.gameutils.utils.ItemBuilderUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class GeneratorRunnable {
    private final Game game;
    private boolean timer;

    public GeneratorRunnable(Game game) {
        this.game = game;
    }

    public void start() {
        if (!this.timer) {
            timer = true;

            game.getEventManager().registerRunnable("铜刷新", (seconds, currentEvent) -> Bukkit.getScheduler().runTask(OneBedwars.getInstance(), () -> game.getMapData().getDropLocations(MapData.DropType.BRONZE).forEach((location -> location.getWorld().dropItem(location, new ItemBuilderUtil().setType(Material.CLAY_BRICK).getItem()).setVelocity(new Vector(0.0D, 0.1D, 0.0D))))), 1);
            game.getEventManager().registerRunnable("铁刷新", (seconds, currentEvent) -> Bukkit.getScheduler().runTask(OneBedwars.getInstance(), () -> game.getMapData().getDropLocations(MapData.DropType.IRON).forEach((location -> location.getWorld().dropItem(location, new ItemBuilderUtil().setType(Material.IRON_INGOT).getItem()).setVelocity(new Vector(0.0D, 0.1D, 0.0D))))), 5);
            game.getEventManager().registerRunnable("金刷新", (seconds, currentEvent) -> Bukkit.getScheduler().runTask(OneBedwars.getInstance(), () -> game.getMapData().getDropLocations(MapData.DropType.GOLD).forEach((location -> location.getWorld().dropItem(location, new ItemBuilderUtil().setType(Material.GOLD_INGOT).getItem()).setVelocity(new Vector(0.0D, 0.1D, 0.0D))))), 10);

        }
    }
}
