package cc.i9mc.onebedwars;

import cc.i9mc.gameutils.BukkitGameUtils;
import cc.i9mc.k8sgameack.K8SGameACK;
import cc.i9mc.k8sgameack.utils.IPUtil;
import cc.i9mc.onebedwars.commands.AdminCommand;
import cc.i9mc.onebedwars.commands.StartCommand;
import cc.i9mc.onebedwars.commands.TestCommand;
import cc.i9mc.onebedwars.database.map.MapData;
import cc.i9mc.onebedwars.database.map.MapDataSQL;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.listeners.*;
import cc.i9mc.onebedwars.scoreboards.GameBoard;
import cc.i9mc.onebedwars.scoreboards.LobbyBoard;
import cc.i9mc.onebedwars.specials.SpecialItem;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

@Getter
public final class OneBedwars extends JavaPlugin {
    public static HashMap<Integer, Integer> playerLevel = new HashMap<>();
    @Getter
    private static OneBedwars instance;
    @Getter
    private Game game;
    @Getter
    @Setter
    private MapData mapData;
    @Getter
    private Economy econ = null;
    @Getter
    private Chat chat = null;

    @Override
    public void onEnable() {
        instance = this;
        long time = System.currentTimeMillis();

        if (getConfig().getBoolean("isUP")) {
            MapDataSQL mds = new MapDataSQL();

            K8SGameACK.getInstance().getServerData().setGameType("OneBedWars");
            K8SGameACK.getInstance().getExpand().put("ver", getDescription().getVersion());
            BukkitGameUtils.getInstance().getConnectionPoolHandler().registerDatabase("bwdata");
            BukkitGameUtils.getInstance().getConnectionPoolHandler().registerDatabase("bwstats");
            BukkitGameUtils.getInstance().getPubSubListener().addChannel("MINIGame.OneBW." + IPUtil.getLocalIp());
            game = new Game(this, mds.getWaitingLoc().toLocation());

            setupEconomy();
            setupChat();

            Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
            Bukkit.getPluginManager().registerEvents(new QuitListener(), this);
            Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
            Bukkit.getPluginManager().registerEvents(new ReSpawnListener(), this);
            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
            Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
            Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
            Bukkit.getPluginManager().registerEvents(new ServerListener(), this);
            Bukkit.getPluginManager().registerEvents(new ChunkListener(), this);
            Bukkit.getPluginCommand("start").setExecutor(new StartCommand());
            Bukkit.getPluginManager().registerEvents(new LobbyBoard(game), this);
            Bukkit.getPluginManager().registerEvents(new GameBoard(game), this);
            Bukkit.getPluginManager().registerEvents(mds, this);

            SpecialItem.loadSpecials();
            loadLevel();


            Bukkit.getWorlds().forEach(world -> {
                world.setAutoSave(false);
                world.setDifficulty(Difficulty.NORMAL);
            });
            return;
        }

        Bukkit.getPluginCommand("game").setExecutor(new AdminCommand());

        Bukkit.getConsoleSender().sendMessage("[BedWars] 加载完成耗时 " + (System.currentTimeMillis() - time) + " ms");
    }

    public void mainThreadRunnable(Runnable runnable) {
        Bukkit.getScheduler().runTask(this, runnable);
    }

    private void loadLevel() {
        playerLevel.put(1, 0);
        playerLevel.put(2, 10);
        playerLevel.put(3, 25);
        playerLevel.put(4, 45);
        playerLevel.put(5, 100);
        playerLevel.put(6, 220);
        playerLevel.put(7, 450);
        playerLevel.put(8, 800);
        playerLevel.put(9, 900);
        playerLevel.put(10, 1050);
        playerLevel.put(11, 1800);
        playerLevel.put(12, 2600);
        playerLevel.put(13, 3450);
        playerLevel.put(14, 4200);
        playerLevel.put(15, 5450);
        playerLevel.put(16, 6150);
        playerLevel.put(17, 6850);
        playerLevel.put(18, 7550);
        playerLevel.put(19, 8250);
        playerLevel.put(20, 8900);
        playerLevel.put(21, 10000);
        playerLevel.put(22, 11250);
        playerLevel.put(23, 12500);
        playerLevel.put(24, 13750);
        playerLevel.put(25, 15000);
        playerLevel.put(26, 16250);
        playerLevel.put(27, 17500);
        playerLevel.put(28, 18750);
        playerLevel.put(29, 20000);
        playerLevel.put(30, 22000);
        playerLevel.put(31, 24000);
        playerLevel.put(32, 26000);
        playerLevel.put(33, 28000);
        playerLevel.put(34, 30000);
        playerLevel.put(35, 32000);
        playerLevel.put(36, 34000);
        playerLevel.put(37, 36000);
        playerLevel.put(38, 38000);
        playerLevel.put(39, 40000);
        playerLevel.put(40, 45000);
        playerLevel.put(41, 50000);
        playerLevel.put(42, 55000);
        playerLevel.put(43, 60000);
        playerLevel.put(44, 65000);
        playerLevel.put(45, 70000);
        playerLevel.put(46, 75000);
        playerLevel.put(47, 80000);
        playerLevel.put(48, 85000);
        playerLevel.put(49, 90000);
        playerLevel.put(50, 100000);
    }

    public int getLevel(int level) {
        int playerLevel = 0;
        for (Map.Entry<Integer, Integer> entry : OneBedwars.playerLevel.entrySet()) {
            if (level > entry.getValue()) {
                playerLevel = entry.getKey();
            } else break;
        }
        return playerLevel;
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp.getProvider();
    }

    private void setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
    }
}
