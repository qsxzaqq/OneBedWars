package cc.i9mc.onebedwars.listeners;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.database.PlayerData;
import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.game.GameState;
import cc.i9mc.onebedwars.game.GameTeam;
import cc.i9mc.onebedwars.guis.ModeSelectionGUI;
import cc.i9mc.onebedwars.guis.TeamSelectionGUI;
import cc.i9mc.onebedwars.shop.NewItemShop;
import cc.i9mc.onebedwars.spectator.SpectatorCompassGUI;
import cc.i9mc.onebedwars.spectator.SpectatorSettingGUI;
import cc.i9mc.onebedwars.spectator.SpectatorSettings;
import cc.i9mc.onebedwars.types.ModeType;
import cc.i9mc.onebedwars.utils.SoundUtil;
import cc.i9mc.onebedwars.villager.MerchantCategoryManager;
import cc.i9mc.gameutils.utils.BungeeUtil;
import cc.i9mc.watchnmslreport.BukkitReport;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
    private final Game game = OneBedwars.getInstance().getGame();

    @EventHandler
    public void onEntityShoot(EntityShootBowEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player p = (Player)event.getEntity();
            if (game.getEventManager().currentEvent().getPriority() < 2) {
                WitherSkull skull = p.launchProjectile(WitherSkull.class);
                skull.setMetadata("DLBOW", new FixedMetadataValue(OneBedwars.getInstance(), null));
                skull.setYield(3.0F);
                skull.setVelocity(event.getProjectile().getVelocity());
                p.getWorld().playSound(p.getLocation(), Sound.WITHER_HURT, 1.0F, 0.0F);
                p.getWorld().playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 5);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        Entity e1 = e.getEntity();
        Entity e2 = e.getDamager();
        if (e1 instanceof Player && e2 instanceof WitherSkull && e2.hasMetadata("DLBOW")) {
            Player player = (Player)e1;
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
            e2.getWorld().playSound(e2.getLocation(), Sound.WITHER_HURT, 1.0F, 0.0F);
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (GamePlayer.get(event.getEntity().getUniqueId()).isSpectator()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void craftItem(PrepareItemCraftEvent event) {
        for (HumanEntity h : event.getViewers()) {
            if (h instanceof Player) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        GamePlayer gamePlayer =  GamePlayer.get(event.getWhoClicked().getUniqueId());

        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory().getName().equals("§8道具商店")) {
            event.setCancelled(true);
            ItemStack clickedStack = event.getCurrentItem();

            if (clickedStack == null) {
                return;
            }

            gamePlayer.getNewItemShop().handleInventoryClick(event, game, (Player) event.getWhoClicked());
        }
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material interactingMaterial = event.getMaterial();

        if (interactingMaterial == null) {
            event.setCancelled(true);
            return;
        }

        if (game.getGameState() == GameState.WAITING) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                event.setCancelled(true);
                switch (interactingMaterial) {
                    case PAPER:
                        new ModeSelectionGUI(player).open();
                        return;
                    case BED:
                        new TeamSelectionGUI(player, game).open();
                        return;
                    case SLIME_BALL:
                        BungeeUtil.send("BW-Lobby-1", player);
                        return;
                    default:
                        return;
                }
            }
        }

        if (game.getGameState() == GameState.RUNNING) {
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            GameTeam gameTeam = gamePlayer.getGameTeam();

            if (event.getAction() == Action.PHYSICAL) {
                event.setCancelled(true);
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                if (gamePlayer.isSpectator()) {
                    event.setCancelled(true);
                    return;
                }

                if (player.isSneaking() && player.getItemInHand() != null && player.getItemInHand().getType().isBlock()) {
                    return;
                }

                if (gameTeam.getTeamChests().contains(event.getClickedBlock())) {
                    player.openInventory(gameTeam.getInventory());
                } else {
                    gamePlayer.sendMessage("§c这个箱子不是你队伍的箱子!");
                }
                event.setCancelled(true);
                return;
            }

            if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && (gamePlayer.getSpectatorTarget() != null) && interactingMaterial == Material.COMPASS) {
                gamePlayer.getSpectatorTarget().tp();
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                switch (interactingMaterial) {
                    case COMPASS:
                        event.setCancelled(true);
                        if (!gamePlayer.isSpectator()) {
                            return;
                        }

                        new SpectatorCompassGUI(player).open();
                        return;
                    case REDSTONE_COMPARATOR:
                        new SpectatorSettingGUI(player).open();
                        return;
                    case PAPER:
                        event.setCancelled(true);
                        Bukkit.dispatchCommand(player, "queue join qc jdqc");
                        return;
                    case SLIME_BALL:
                        event.setCancelled(true);
                        BungeeUtil.send("BW-Lobby-1", player);
                        return;
                    case ENDER_PEARL:
                        if(game.getEventManager().currentEvent().getPriority() > 1) {
                            return;
                        }
                        player.sendMessage("§c传送保护！");
                        event.setCancelled(true);
                        return;
                    default:
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem().getItemStack();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        PlayerData playerData = gamePlayer.getPlayerData();

        if (gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        if (itemStack.getType() == Material.BED || itemStack.getType() == Material.BED_BLOCK) {
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().getDisplayName() != null) {
                return;
            }

            event.setCancelled(true);
            event.getItem().remove();
        }

        if (itemStack.getType() == Material.CLAY_BRICK || itemStack.getType() == Material.IRON_INGOT || itemStack.getType() == Material.GOLD_INGOT) {
            double xp = itemStack.getAmount();

            if (itemStack.getType() == Material.IRON_INGOT) {
                xp = xp * 10;
            }else if (itemStack.getType() == Material.GOLD_INGOT) {
                xp = xp * 50;
            }

            if (player.hasPermission("bw.xp.vip1")) {
                if (itemStack.getType() == Material.CLAY_BRICK) {
                    xp += xp * 1.3D;
                } else if (itemStack.getType() == Material.IRON_INGOT) {
                    xp += xp * 1.1D;
                } else if (itemStack.getType() == Material.GOLD_INGOT) {
                    xp += xp * 1.0D;
                }
            } else if (player.hasPermission("bw.xp.vip2")) {
                if (itemStack.getType() == Material.CLAY_BRICK) {
                    xp += xp * 1.4D;
                } else if (itemStack.getType() == Material.IRON_INGOT) {
                    xp += xp * 1.3D;
                } else if (itemStack.getType() == Material.GOLD_INGOT) {
                    xp += xp * 1.2D;
                }
            } else if (player.hasPermission("bw.xp.vip3")) {
                if (itemStack.getType() == Material.CLAY_BRICK) {
                    xp += xp * 1.6D;
                } else if (itemStack.getType() == Material.IRON_INGOT) {
                    xp += xp * 1.5D;
                } else if (itemStack.getType() == Material.GOLD_INGOT) {
                    xp += xp * 1.4D;
                }
            } else if (player.hasPermission("bw.xp.vip4")) {
                if (itemStack.getType() == Material.CLAY_BRICK) {
                    xp += xp * 1.8D;
                } else if (itemStack.getType() == Material.IRON_INGOT) {
                    xp += xp * 1.7D;
                } else if (itemStack.getType() == Material.GOLD_INGOT) {
                    xp += xp * 1.6D;
                }
            }

            if (playerData.getModeType() == ModeType.DEFAULT) {
                event.setCancelled(true);
                event.getItem().remove();

                player.playSound(player.getLocation(), SoundUtil.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);
                player.getInventory().addItem(new ItemStack(itemStack.getType(), itemStack.getAmount()));
            } else if (playerData.getModeType() == ModeType.EXPERIENCE) {
                event.setCancelled(true);
                event.getItem().remove();

                player.playSound(player.getLocation(), SoundUtil.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);
                player.setLevel((int) (player.getLevel() + xp));
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.startsWith("/report")) {
            return;
        }

        if (message.startsWith("/queue join qc jdqc")) {
            return;
        }

        if (BukkitReport.getInstance().getStaffs().containsKey(player.getName())) {
            if (event.getMessage().startsWith("/wnm") || event.getMessage().startsWith("/staff")) {
                return;
            }
        }

        if (!player.hasPermission("bw.*")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            event.setCancelled(true);
            if (gamePlayer.isSpectator()) {
                return;
            }

            NewItemShop itemShop = gamePlayer.getNewItemShop();
            if (itemShop == null) {
                itemShop = new NewItemShop(MerchantCategoryManager.getCategories(), game);
            }

            itemShop.setCurrentCategory(null);
            itemShop.openCategoryInventory(player);
            gamePlayer.setNewItemShop(itemShop);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (event.getItem().getType() != Material.POTION) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getInventory().getItemInHand().getType() == Material.GLASS_BOTTLE) {
                    player.getInventory().setItemInHand(new ItemStack(Material.AIR));
                }
            }
        }.runTaskLater(OneBedwars.getInstance(), 0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer().getUniqueId());
        if (gamePlayer.isSpectator() && game.getGameState() == GameState.RUNNING) {
            if (gamePlayer.isSpectator() && event.getRightClicked() instanceof Player && SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRSTPERSON)) {
                event.setCancelled(true);
                if (GamePlayer.get(event.getRightClicked().getUniqueId()).isSpectator()) {
                    return;
                }

                gamePlayer.sendTitle(0, 20, 0, "§a正在旁观§7" + event.getRightClicked().getName(), "§a点击左键打开菜单  §c按Shift键退出");
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                event.getPlayer().setSpectatorTarget(event.getRightClicked());
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (gamePlayer.isSpectator() && (SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRSTPERSON)) && player.getGameMode() == GameMode.SPECTATOR) {
            gamePlayer.sendTitle(0, 20, 0, "§e退出旁观模式", "");
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);
            return;
        }

        if (player.hasMetadata("等待上一次求救")) {
            return;
        }

        if (player.getLocation().getPitch() > -80) {
            return;
        }

        player.setMetadata("等待上一次求救", new FixedMetadataValue(OneBedwars.getInstance(), ""));


        GameTeam gameTeam = gamePlayer.getGameTeam();

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i > 5) {
                    player.removeMetadata("等待上一次求救", OneBedwars.getInstance());
                    cancel();
                    return;
                }

                game.broadcastTeamTitle(gameTeam, 0, 8, 0, "", gameTeam.getChatColor() + gamePlayer.getDisplayname() + " 说: §c注意,我们的床有危险！");
                game.broadcastTeamSound(gameTeam, SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);
                i++;
            }
        }.runTaskTimer(OneBedwars.getInstance(), 0, 10L);
    }
}
