package cc.i9mc.onebedwars.villager;

import lombok.Data;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@Data
public class MerchantCategory {
    private final Material material;
    private final List<String> lore;
    private final String name;
    private final ArrayList<VillagerTrade> offers;
    private final int order;
    private final String permission;

    public MerchantCategory(String name, Material item) {
        this(name, item, new ArrayList<>(), new ArrayList<>(), 0, "bw.base");
    }

    public MerchantCategory(String name, Material material, ArrayList<VillagerTrade> offers, List<String> lore, int order, String permission) {
        this.name = name;
        this.material = material;
        this.offers = offers;
        this.lore = lore;
        this.order = order;
        this.permission = permission;
    }

    public ArrayList<VillagerTrade> getFilteredOffers() {
        ArrayList<VillagerTrade> trades = (ArrayList<VillagerTrade>) this.offers.clone();

        trades.removeIf(trade -> trade.getItem1().getType() == Material.AIR
                && trade.getRewardItem().getType() == Material.AIR);

        return trades;
    }
}
