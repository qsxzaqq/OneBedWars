package cc.i9mc.onebedwars.villager;

import org.bukkit.inventory.ItemStack;

public class VillagerTrade {

    private final ItemStack item1;
    private final ItemStack item2;
    private final ItemStack rewardItem;

    public VillagerTrade(ItemStack item1, ItemStack item2, ItemStack rewardItem) {
        this.item1 = item1;
        this.item2 = item2;
        this.rewardItem = rewardItem;
    }

    public VillagerTrade(ItemStack item1, ItemStack rewardItem) {
        this(item1, null, rewardItem);
    }

    public ItemStack getItem1() {
        return this.item1;
    }

    public ItemStack getItem2() {
        return this.item2;
    }

    public ItemStack getRewardItem() {
        return this.rewardItem;
    }

    public boolean hasItem2() {
        return this.item2 != null;
    }

}
