package cc.i9mc.onebedwars.shop;

import cc.i9mc.onebedwars.game.Game;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.types.ModeType;
import cc.i9mc.onebedwars.utils.SoundUtil;
import cc.i9mc.onebedwars.utils.Util;
import cc.i9mc.onebedwars.villager.MerchantCategory;
import cc.i9mc.onebedwars.villager.VillagerTrade;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class NewItemShop {
    private final List<MerchantCategory> categories;
    private MerchantCategory currentCategory = null;
    private boolean oneStackPerShift;
    private final Game game;

    public NewItemShop(List<MerchantCategory> categories, Game game) {
        this.categories = categories;
        this.game = game;
    }

    private void addCategoriesToInventory(Inventory inventory, Player player) {
        for (MerchantCategory category : this.categories) {

            if (category.getMaterial() == null) {
                continue;
            }

            if (category.getPermission() != null && !player.hasPermission(category.getPermission())) {
                continue;
            }

            ItemStack is = new ItemStack(category.getMaterial(), 1);
            ItemMeta im = is.getItemMeta();

            if (Util.isColorable(is)) {
                is.setDurability(GamePlayer.get(player.getUniqueId()).getGameTeam().getDyeColor().getWoolData());
            }
            if (this.currentCategory != null && this.currentCategory.equals(category)) {
                im.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            im.setDisplayName(category.getName());
            im.setLore(category.getLore());
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
            is.setItemMeta(im);

            inventory.addItem(is);
        }

    }

    private boolean buyItem(VillagerTrade trade, ItemStack item, Player player) {
        PlayerInventory inventory = player.getInventory();
        boolean success = true;

       // if(GamePlayer.get(player.getUniqueId()).getPlayerData().getModeType() == ModeType.EXPERIENCE){
            player.setLevel(player.getLevel() - this.getXP(trade.getItem1(), trade.getItem2()));
        /*}else {
            int item1ToPay = trade.getItem1().getAmount();
            Iterator<?> stackIterator = inventory.all(trade.getItem1().getType()).entrySet().iterator();

            int firstItem1 = inventory.first(trade.getItem1());
            if (firstItem1 > -1) {
                inventory.clear(firstItem1);
            } else {
                // pay
                while (stackIterator.hasNext()) {
                    Entry<Integer, ? extends ItemStack> entry =
                            (Entry<Integer, ? extends ItemStack>) stackIterator.next();
                    ItemStack stack = entry.getValue();

                    int endAmount = stack.getAmount() - item1ToPay;
                    if (endAmount < 0) {
                        endAmount = 0;
                    }

                    item1ToPay = item1ToPay - stack.getAmount();
                    stack.setAmount(endAmount);
                    inventory.setItem(entry.getKey(), stack);

                    if (item1ToPay <= 0) {
                        break;
                    }
                }
            }

            if (trade.getItem2() != null) {
                int item2ToPay = trade.getItem2().getAmount();
                stackIterator = inventory.all(trade.getItem2().getType()).entrySet().iterator();

                int firstItem2 = inventory.first(trade.getItem2());
                if (firstItem2 > -1) {
                    inventory.clear(firstItem2);
                } else {
                    // pay item2
                    while (stackIterator.hasNext()) {
                        Entry<Integer, ? extends ItemStack> entry =
                                (Entry<Integer, ? extends ItemStack>) stackIterator.next();
                        ItemStack stack = entry.getValue();

                        int endAmount = stack.getAmount() - item2ToPay;
                        if (endAmount < 0) {
                            endAmount = 0;
                        }

                        item2ToPay = item2ToPay - stack.getAmount();
                        stack.setAmount(endAmount);
                        inventory.setItem(entry.getKey(), stack);

                        if (item2ToPay <= 0) {
                            break;
                        }
                    }
                }
            }
        }*/

        ItemStack addingItem = item.clone();
        ItemMeta meta = addingItem.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore.size() > 0) {
            //if(GamePlayer.get(player.getUniqueId()).getPlayerData().getModeType() == ModeType.EXPERIENCE){
                lore.remove(lore.size() - 1);
         /*   }else {
                lore.remove(lore.size() - 1);
                if (trade.getItem2() != null) {
                    lore.remove(lore.size() - 1);
                }
            }*/
        }

        meta.setLore(lore);
        addingItem.setItemMeta(meta);

        HashMap<Integer, ItemStack> notStored = inventory.addItem(addingItem);
        if (notStored.size() > 0) {
            ItemStack notAddedItem = notStored.get(0);
            int removingAmount = addingItem.getAmount() - notAddedItem.getAmount();
            addingItem.setAmount(removingAmount);
            inventory.removeItem(addingItem);

            // restore
            inventory.addItem(trade.getItem1());
            if (trade.getItem2() != null) {
                inventory.addItem(trade.getItem2());
            }

            success = false;
        }

        player.updateInventory();
        return success;
    }

    private int getBuyInventorySize(int sizeCategories, int sizeOffers) {
        return this.getInventorySize(sizeCategories) + this.getInventorySize(sizeOffers);
    }

    public List<MerchantCategory> getCategories() {
        return this.categories;
    }

    private int getCategoriesSize(Player player) {
        int size = 0;
        for (MerchantCategory cat : this.categories) {
            if (cat.getMaterial() == null) {
                continue;
            }

            if (cat.getPermission() != null && !player.hasPermission(cat.getPermission())) {
                continue;
            }

            size++;
        }

        return size;
    }

    private MerchantCategory getCategoryByMaterial(Material material) {
        for (MerchantCategory category : this.categories) {
            if (category.getMaterial() == material) {
                return category;
            }
        }

        return null;
    }

    private int getInventorySize(int itemAmount) {
        int nom = (itemAmount % 9 == 0) ? 9 : (itemAmount % 9);
        return itemAmount + (9 - nom);
    }

    private VillagerTrade getTradingItem(MerchantCategory category, ItemStack stack, Game game, Player player) {
        for (VillagerTrade trade : category.getOffers()) {
            if (trade.getItem1().getType() == Material.AIR
                    && trade.getRewardItem().getType() == Material.AIR) {
                continue;
            }
            ItemStack iStack = this.toItemStack(trade, player, game);
            if (iStack.getType() == Material.ENDER_CHEST && stack.getType() == Material.ENDER_CHEST) {
                return trade;
            } else if ((iStack.getType() == Material.POTION)) {
                if (iStack.getItemMeta().equals(stack.getItemMeta())) {
                    return trade;
                }
            } else if (iStack.equals(stack)) {
                return trade;
            }
        }

        return null;
    }

    private void handleBuyInventoryClick(InventoryClickEvent ice, Game game, Player player) {
        int sizeCategories = this.getCategoriesSize(player);
        List<VillagerTrade> offers = this.currentCategory.getOffers();
        int sizeItems = offers.size();
        int totalSize = this.getBuyInventorySize(sizeCategories, sizeItems);

        ItemStack item = ice.getCurrentItem();
        boolean cancel = false;
        int bought = 0;

        if (this.currentCategory == null) {
            player.closeInventory();
            return;
        }

        if (ice.getRawSlot() < sizeCategories) {
            // is category click
            ice.setCancelled(true);

            if (item == null) {
                return;
            }

            if (item.getType().equals(this.currentCategory.getMaterial())) {
                // back to default category view
                this.currentCategory = null;
                this.openCategoryInventory(player);
            } else {
                // open the clicked buy inventory
                this.handleCategoryInventoryClick(ice, game, player);
            }
        } else if (ice.getRawSlot() < totalSize) {
            // its a buying item
            ice.setCancelled(true);

            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            MerchantCategory category = this.currentCategory;
            VillagerTrade trade = this.getTradingItem(category, item, game, player);

            if (trade == null) {
                return;
            }

            player.playSound(player.getLocation(), SoundUtil.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), 1f, 1f);
            if (!this.hasEnoughResource(player, trade)) {
                player.sendMessage("§c没有足够资源购买！");
                return;
            }

            if (ice.isShiftClick()) {
                while (this.hasEnoughResource(player, trade) && !cancel) {
                    cancel = !this.buyItem(trade, ice.getCurrentItem(), player);
                    if (!cancel && oneStackPerShift) {
                        bought = bought + item.getAmount();
                        cancel = ((bought + item.getAmount()) > 64);
                    }
                }
            } else {
                this.buyItem(trade, ice.getCurrentItem(), player);
            }
        } else {
            ice.setCancelled(ice.isShiftClick());
        }
    }

    private void handleCategoryInventoryClick(InventoryClickEvent ice, Game game, Player player) {

        int catSize = this.getCategoriesSize(player);
        int sizeCategories = this.getInventorySize(catSize) + 9;
        int rawSlot = ice.getRawSlot();

        if (rawSlot >= this.getInventorySize(catSize) && rawSlot < sizeCategories) {
            ice.setCancelled(true);

            if (ice.getCurrentItem().getType() == Material.BUCKET) {
                oneStackPerShift = false;
                player.playSound(player.getLocation(), SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);
                this.openCategoryInventory(player);
                return;
            } else if (ice.getCurrentItem().getType() == Material.LAVA_BUCKET) {
                oneStackPerShift = true;
                player.playSound(player.getLocation(), SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);
                this.openCategoryInventory(player);
                return;
            }
        }

        if (rawSlot >= sizeCategories) {
            if (ice.isShiftClick()) {
                ice.setCancelled(true);
                return;
            }

            ice.setCancelled(false);
            return;
        }

        MerchantCategory clickedCategory = this.getCategoryByMaterial(ice.getCurrentItem().getType());
        if (clickedCategory == null) {
            if (ice.isShiftClick()) {
                ice.setCancelled(true);
                return;
            }

            ice.setCancelled(false);
            return;
        }

        this.openBuyInventory(clickedCategory, player);
    }

    public void handleInventoryClick(InventoryClickEvent ice, Game game, Player player) {
        if (!this.hasOpenCategory()) {
            this.handleCategoryInventoryClick(ice, game, player);
        } else {
            this.handleBuyInventoryClick(ice, game, player);
        }
    }

    private boolean hasEnoughResource(Player player, VillagerTrade trade) {
        ItemStack item1 = trade.getItem1();
        ItemStack item2 = trade.getItem2();
        PlayerInventory inventory = player.getInventory();

        //if(GamePlayer.get(player.getUniqueId()).getPlayerData().getModeType() == ModeType.EXPERIENCE){
            return player.getLevel() >= getXP(item1, item2);
        /*}else {
            if (item2 != null) {
                return inventory.contains(item1.getType(), item1.getAmount())
                        && inventory.contains(item2.getType(), item2.getAmount());
            } else {
                return inventory.contains(item1.getType(), item1.getAmount());
            }
        }*/
    }

    public boolean hasOpenCategory() {
        return (this.currentCategory != null);
    }

    private void openBuyInventory(MerchantCategory category, Player player) {
        List<VillagerTrade> offers = category.getOffers();
        int sizeCategories = this.getCategoriesSize(player);
        int sizeItems = offers.size();
        int invSize = this.getBuyInventorySize(sizeCategories, sizeItems);

        player.playSound(player.getLocation(), SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);

        this.currentCategory = category;
        Inventory buyInventory = Bukkit
                .createInventory(player, invSize, "§8道具商店");
        this.addCategoriesToInventory(buyInventory, player);

        for (int i = 0; i < offers.size(); i++) {
            VillagerTrade trade = offers.get(i);
            if (trade.getItem1().getType() == Material.AIR
                    && trade.getRewardItem().getType() == Material.AIR) {
                continue;
            }

            int slot = (this.getInventorySize(sizeCategories)) + i;
            ItemStack tradeStack = this.toItemStack(trade, player, game);

            buyInventory.setItem(slot, tradeStack);
        }

        player.openInventory(buyInventory);
    }

    public void openCategoryInventory(Player player) {
        int catSize = this.getCategoriesSize(player);
        int nom = (catSize % 9 == 0) ? 9 : (catSize % 9);
        int size = (catSize + (9 - nom)) + 9;

        Inventory inventory = Bukkit.createInventory(player, size, "§8道具商店");
        this.addCategoriesToInventory(inventory, player);

        ItemStack stack;
        if (oneStackPerShift) {
            stack = new ItemStack(Material.BUCKET, 1);
            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName(ChatColor.AQUA + "当前:" + ChatColor.WHITE + "One stack per shift click");
            meta.setLore(new ArrayList<>());
            stack.setItemMeta(meta);
        } else {
            stack = new ItemStack(Material.LAVA_BUCKET, 1);
            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName(ChatColor.AQUA + "当前: " + ChatColor.WHITE + "Multiply stacks per shift click");
            meta.setLore(new ArrayList<>());
            stack.setItemMeta(meta);
        }

        inventory.setItem(size - 4, stack);
        player.openInventory(inventory);
    }

    public void setCurrentCategory(MerchantCategory category) {
        this.currentCategory = category;
    }

    private ItemStack toItemStack(VillagerTrade trade, Player player, Game game) {
        ItemStack tradeStack = trade.getRewardItem().clone();
        ItemMeta meta = tradeStack.getItemMeta();
        ItemStack item1 = trade.getItem1();
        ItemStack item2 = trade.getItem2();
        if(tradeStack.getType().equals(Material.LEATHER_HELMET) || tradeStack.getType().equals(Material.LEATHER_CHESTPLATE) || tradeStack.getType().equals(Material.LEATHER_LEGGINGS) || tradeStack.getType().equals(Material.LEATHER_BOOTS)){
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
            leatherArmorMeta.setColor(GamePlayer.get(player.getUniqueId()).getGameTeam().getColor());
        }else if (Util.isColorable(tradeStack)) {
            tradeStack.setDurability(GamePlayer.get(player.getUniqueId()).getGameTeam().getDyeColor().getWoolData());
        }
        List<String> lores = meta.getLore();
        if (lores == null) {
            lores = new ArrayList<>();
        }

       // if(GamePlayer.get(player.getUniqueId()).getPlayerData().getModeType() == ModeType.EXPERIENCE){
            lores.add("§a" + this.getXP(item1, item2) + " 经验");
       /* }else {
            lores.add(ChatColor.WHITE + String.valueOf(item1.getAmount()) + " " + getName(item1));
            if (item2 != null)
                lores.add(ChatColor.WHITE + String.valueOf(item2.getAmount()) + " " + getName(item2));
        }*/

        meta.setLore(lores);
        tradeStack.setItemMeta(meta);
        return tradeStack;
    }

    public int getXP(ItemStack itemStack, ItemStack itemStack2) {
        int xp = 0;

        if(itemStack.getType() == Material.CLAY_BRICK){
            xp = itemStack.getAmount();
        }else if(itemStack.getType() == Material.IRON_INGOT){
            xp = itemStack.getAmount() * 10;
        }else if(itemStack.getType() == Material.GOLD_INGOT){
            xp = itemStack.getAmount() * 50;
        }

        if(itemStack2 != null) xp += getXP(itemStack2, null);

        return xp;
    }

    public String getName(ItemStack itemStack){
        if(itemStack.getType() == Material.CLAY_BRICK) return "§4铜";
        else if(itemStack.getType() == Material.IRON_INGOT) return "§f铁";
        else if(itemStack.getType() == Material.GOLD_INGOT) return "§6金";

        return null;
    }
}