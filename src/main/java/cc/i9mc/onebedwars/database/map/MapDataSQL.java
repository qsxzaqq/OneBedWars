package cc.i9mc.onebedwars.database.map;

import cc.i9mc.gameutils.BukkitGameUtils;
import cc.i9mc.gameutils.event.bukkit.BukkitPubSubMessageEvent;
import cc.i9mc.k8sgameack.utils.IPUtil;
import cc.i9mc.onebedwars.OneBedwars;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MapDataSQL implements Listener {
    private final Gson GSON = new Gson();
    private boolean load = false;

    public MapData loadMap(String mapName) {
        MapData mapData = null;

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("bwdata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM OneBWMaps Where MapName=?");
            preparedStatement.setString(1, mapName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("MapName");
                String url = resultSet.getString("URL");
                mapData = GSON.fromJson(resultSet.getString("Data"), MapData.class);

                if (new File(name).exists()) {
                    new File(name).delete();
                }

                FileUtils.copyDirectory(new File(url), new File(name));

                WorldCreator cr = new WorldCreator(name);
                cr.environment(World.Environment.NORMAL);
                World mapWorld = Bukkit.createWorld(cr);
                mapWorld.setAutoSave(false);
                mapWorld.setGameRuleValue("doMobSpawning", "false");
                mapWorld.setGameRuleValue("doFireTick", "false");
            }

            preparedStatement.close();
            resultSet.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return mapData;
    }

    public MapData.Location getWaitingLoc() {
        MapData.Location location = null;
        String wordName = null;

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("bwdata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM OneBWConfig Where configKey=?");
            preparedStatement.setString(1, "WaitingMapURL");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String URL = resultSet.getString("object");
                wordName = new File(URL).getName();

                if (new File(wordName).exists()) {
                    new File(wordName).delete();
                }

                FileUtils.copyDirectory(new File(URL), new File(wordName));

                WorldCreator cr = new WorldCreator(wordName);
                cr.environment(World.Environment.NORMAL);
                World mapWorld = Bukkit.createWorld(cr);

                mapWorld.setAutoSave(false);
                mapWorld.setGameRuleValue("doMobSpawning", "false");
                mapWorld.setGameRuleValue("doFireTick", "false");
            }

            preparedStatement = connection.prepareStatement("SELECT * FROM BWConfig Where configKey=?");
            preparedStatement.setString(1, "WaitingLoc");
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                location = GSON.fromJson(resultSet.getString("object"), MapData.Location.class);
                location.setWorld(wordName);
            }

            preparedStatement.close();
            resultSet.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return location;
    }

    @EventHandler
    public void onMessage(BukkitPubSubMessageEvent event) {
        if (event.getChannel().equals("MINIGame.OneBW." + IPUtil.getLocalIp())) {
            if (load) return;

            load = true;
            OneBedwars.getInstance().mainThreadRunnable(() -> {
                MapData mapData = loadMap(event.getMessage());
                mapData.setName(event.getMessage());
                OneBedwars.getInstance().getGame().loadGame(mapData);
                Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(OneBedwars.getInstance(), () -> {
                    Bukkit.getWorlds().forEach(world -> {
                        world.setTime(8000L);
                    });
                }, 0L, 600L);
            });
        }
    }
}
