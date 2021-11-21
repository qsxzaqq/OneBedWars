package cc.i9mc.onebedwars.specials;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.game.GameState;
import cc.i9mc.onebedwars.utils.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class TNTSheepListener implements Listener {
    public TNTSheepListener() {
        try {
            ITNTSheepRegister register = new ITNTSheepRegister();
            register.registerEntities(91);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getCause().equals(DamageCause.CUSTOM) || event.getCause().equals(DamageCause.VOID)
                || event.getCause().equals(DamageCause.FALL)) {
            return;
        }

        if (event.getEntity() instanceof ITNTSheep) {
            event.setDamage(0.0);
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!(event.getDamager() instanceof TNTPrimed)) {
            return;
        }

        TNTPrimed damager = (TNTPrimed) event.getDamager();

        if (!(damager.getSource() instanceof Player)) {
            return;
        }

        GamePlayer damagerPlayer = GamePlayer.get(damager.getSource().getUniqueId());
        GamePlayer player = GamePlayer.get(event.getEntity().getUniqueId());
        Game game = OneBedwars.getInstance().getGame();

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (damagerPlayer.isSpectator() || player.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        if (damagerPlayer.getGameTeam().isInTeam(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_AIR)
                || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();

        Game game = OneBedwars.getInstance().getGame();

        if (game.getGameState() != GameState.RUNNING && !game.getEventManager().isOver()) {
            return;
        }

        TNTSheep creature = new TNTSheep();

        if (!event.getMaterial().equals(creature.getItemMaterial())) {
            return;
        }

        if (GamePlayer.get(player.getUniqueId()).isSpectator()) {
            return;
        }

        Location startLocation = null;
        if (event.getClickedBlock() == null
                || event.getClickedBlock().getRelative(BlockFace.UP).getType() != Material.AIR) {
            startLocation = player.getLocation().getBlock()
                    .getRelative(Util.getCardinalDirection(player.getLocation())).getLocation();
        } else {
            startLocation = event.getClickedBlock().getRelative(BlockFace.UP).getLocation();
        }

        creature.setPlayer(player);
        creature.setGame(game);
        creature.run(startLocation);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractOtherUser(PlayerInteractEntityEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        Game game = OneBedwars.getInstance().getGame();

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (event.getRightClicked() == null) {
            return;
        }

        if (event.getRightClicked() instanceof ITNTSheep) {
            event.setCancelled(true);
            return;
        }

        if (event.getRightClicked().getVehicle() != null && event.getRightClicked().getVehicle() instanceof ITNTSheep) {
            event.setCancelled(true);
        }
    }
}
