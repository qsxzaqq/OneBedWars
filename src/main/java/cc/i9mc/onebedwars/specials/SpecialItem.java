package cc.i9mc.onebedwars.specials;

import cc.i9mc.onebedwars.OneBedwars;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public abstract class SpecialItem {
    @Getter
    private static final List<Class<? extends SpecialItem>> availableSpecials = new ArrayList<>();

    public static void loadSpecials() {
        SpecialItem.availableSpecials.add(MagnetShoe.class);
        SpecialItem.availableSpecials.add(ProtectionWall.class);
        SpecialItem.availableSpecials.add(TNTSheep.class);
        SpecialItem.availableSpecials.add(RescuePlatform.class);
        SpecialItem.availableSpecials.add(WarpPowder.class);
        OneBedwars.getInstance().getServer().getPluginManager().registerEvents(new MagnetShoeListener(), OneBedwars.getInstance());
        OneBedwars.getInstance().getServer().getPluginManager().registerEvents(new ProtectionWallListener(), OneBedwars.getInstance());
        OneBedwars.getInstance().getServer().getPluginManager().registerEvents(new TNTSheepListener(), OneBedwars.getInstance());
        OneBedwars.getInstance().getServer().getPluginManager().registerEvents(new RescuePlatformListener(), OneBedwars.getInstance());
        OneBedwars.getInstance().getServer().getPluginManager().registerEvents(new WarpPowderListener(), OneBedwars.getInstance());
    }

    public abstract Material getActivatedMaterial();

    public abstract Material getItemMaterial();

}
