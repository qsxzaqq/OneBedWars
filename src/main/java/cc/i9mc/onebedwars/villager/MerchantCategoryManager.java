package cc.i9mc.onebedwars.villager;

import cc.i9mc.onebedwars.OneBedwars;
import cc.i9mc.onebedwars.utils.Util;
import com.google.common.base.Charsets;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerchantCategoryManager {
    @Getter
    private static final List<MerchantCategory> categories = new ArrayList<>();

    public static void init() {
        InputStream inputStream = OneBedwars.getInstance().getClass().getClassLoader().getResourceAsStream("shop.yml");
        if(inputStream == null) return;
        YamlConfiguration configuration =  YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, Charsets.UTF_8));
        
        HashMap<Material, MerchantCategory> categorys = new HashMap<>();
        ConfigurationSection section = configuration.getConfigurationSection("shop");
        for (String category : section.getKeys(false)) {
            String categoryName = ChatColor.translateAlternateColorCodes('&', section.getString(category + ".name"));
            Material categoryItem;
            if (Util.isNumber(section.getString(category + ".item"))) {
                categoryItem = Material.getMaterial(section.getInt(category + ".item"));
            }else {
                categoryItem = Material.getMaterial(section.getString(category + ".item"));
            }

            int order = 0;
            if (section.contains(category + ".order") && section.isInt(category + ".order")) {
                order = section.getInt(category + ".order");
            }

            List<String> lore = new ArrayList<>();
            if (section.contains(category + ".lore")) lore.addAll(section.getStringList(category + ".lore"));

            String permission = null;
            if (section.contains(category + ".permission")) {
                permission = section.getString(category + ".permission");
            }

            ArrayList<VillagerTrade> offers = new ArrayList<>();
            for (Object offer : section.getList(category + ".offers")) {
                if (offer instanceof String) {
                    if (offer.toString().equalsIgnoreCase("empty") || offer.toString().equalsIgnoreCase("null") || offer.toString().equalsIgnoreCase("e")) {
                        VillagerTrade trade = new VillagerTrade(new ItemStack(Material.AIR, 1), new ItemStack(Material.AIR, 1));
                        offers.add(trade);
                    }
                    continue;
                }

                HashMap<String, List<Map<String, Object>>> offerSection = (HashMap<String, List<Map<String, Object>>>) offer;
                if (!offerSection.containsKey("price") || !offerSection.containsKey("reward")) {
                    continue;
                }

                ItemStack item1 = ItemStack.deserialize(offerSection.get("price").get(0));

                ItemStack item2 = null;
                if (offerSection.get("price").size() == 2) item2 = ItemStack.deserialize(offerSection.get("price").get(1));

                ItemStack reward = ItemStack.deserialize(offerSection.get("reward").get(0));

                VillagerTrade tradeObj;

                if (item2 != null) {
                    tradeObj = new VillagerTrade(item1, item2, reward);
                } else {
                    tradeObj = new VillagerTrade(item1, reward);
                }

                offers.add(tradeObj);
            }

            categorys.put(categoryItem, new MerchantCategory(categoryName, categoryItem, offers, lore, order, permission));
        }

        categories.addAll(categorys.values());
        categories.sort(new MerchantCategoryComparator());
    }
}
