package cc.i9mc.onebedwars.game.event;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GameTeam;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class EventManager extends TimerTask {
    private final Game game;
    @Getter
    private final HashMap<String, Runnable> runnables = new HashMap<>();
    private final HashMap<Integer, GameEvent> events = new HashMap<>();
    private Timer timer;
    private int currentEvent = 0;
    private int seconds = 0;
    @Getter
    private boolean over = false;

    public EventManager(Game game) {
        this.game = game;
        this.registerEvent(new StartEvent());
        this.registerEvent(new EnderStartEvent());
        this.registerEvent(new BedBOOMEvent());
        this.registerEvent(new WorldBorderEvent());
        this.registerEvent(new OverEvent());
        this.registerEvent(new EndEvent());
    }

    @Override
    public void run() {
        GameEvent event = this.currentEvent();

        event.excuteRunnbale(this.game, event.getExcuteSeconds() - seconds);
        if (this.seconds >= event.getExcuteSeconds()) {
            this.seconds = 0;
            this.currentEvent = event.getPriority() + 1;
            event.excute(this.game);
        }

        if (game.isOver() && !over) {
            Bukkit.getScheduler().runTaskLater(OneBedwars.getInstance(), () -> {
                setCurrentEvent(4);
                currentEvent().excute(game);
            }, 40L);
            over = true;
        }

        for (GameTeam gameTeam : game.getGameTeams()) {
            gameTeam.getAlivePlayers().forEach(player -> {
                if (player.getPlayer().getLocation().getWorld().equals(gameTeam.getSpawn().getWorld())) {
                    player.sendActionBar("§f队伍: " + gameTeam.getChatColor() + gameTeam.getName() + "§f 追踪: " + gameTeam.getChatColor() + ((int) player.getPlayer().getLocation().distance(gameTeam.getSpawn())) + "m");
                }
            });
        }


        runnables.values().forEach(runnable -> {
            if (runnable.getSeconds() != 0) {
                if (runnable.getNextSeconds() == runnable.getSeconds()) {
                    runnable.getEvent().run(runnable.getSeconds() - runnable.getNextSeconds(), currentEvent);
                    runnable.setNextSeconds(0);
                }
                runnable.setNextSeconds(runnable.getNextSeconds() + 1);
            } else {
                runnable.getEvent().run(seconds, currentEvent);
            }
        });

        ++this.seconds;
    }

    public GameEvent currentEvent() {
        return this.events.getOrDefault(this.currentEvent, this.events.get(6));
    }

    public void setCurrentEvent(int priority) {
        this.seconds = 0;
        this.currentEvent = priority;
    }

    public int getLeftTime() {
        return this.currentEvent().getExcuteSeconds() - this.seconds;
    }

    public void setExcuteSeconds() {
        seconds = this.currentEvent().getExcuteSeconds();
    }

    public int getSeconds() {
        return seconds;
    }

    public String formattedNextEvent() {
        GameEvent currentEvent = this.currentEvent();
        return currentEvent instanceof EndEvent ? currentEvent.getName() : currentEvent.getName();
    }

    public void registerRunnable(String name, Runnable.Event runnable) {
        this.runnables.put(name, new Runnable(0, 0, runnable));
    }

    public void registerRunnable(String name, Runnable.Event runnable, int seconds) {
        this.runnables.put(name, new Runnable(seconds, 0, runnable));
    }

    private void registerEvent(GameEvent event) {
        this.events.put(event.getPriority(), event);
    }

    public void start() {
        if (this.timer == null) {
            timer = new Timer();
            timer.schedule(this, 0, 1000);
        }
    }

    public void stop() {
        if (this.timer != null) {
            timer.cancel();
            this.currentEvent = 0;
            this.seconds = 0;
        }
    }
}
