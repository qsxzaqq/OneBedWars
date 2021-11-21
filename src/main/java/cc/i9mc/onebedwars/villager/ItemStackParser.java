package cc.i9mc.onebedwars.villager;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ItemStackParser {
    private Object configSection = null;
    @Getter
    private ItemStack finalStack = null;
    private LinkedHashMap<String, Object> linkedSection = null;
    private Material material = null;

    public ItemStackParser(Object section) {
        this.configSection = section;
    }

    private LinkedHashMap<String, Object> getLinkedMap() {
        LinkedHashMap<String, Object> linkedMap = new LinkedHashMap<String, Object>();

        if (!(this.configSection instanceof LinkedHashMap)) {
            ConfigurationSection newSection = (ConfigurationSection) this.configSection;
            for (String key : newSection.getKeys(false)) {
                linkedMap.put(key, newSection.get(key));
            }
        } else {
            linkedMap = (LinkedHashMap<String, Object>) this.configSection;
        }

        return linkedMap;
    }

    private byte getMeta() {
        return Byte.parseByte(this.linkedSection.get("meta").toString());
    }

    private short getPotionMeta() {
        return Short.parseShort(this.linkedSection.get("meta").toString());
    }

    private int getStackAmount() {
        int amount = 0;
        try {
            if (this.linkedSection.containsKey("amount")) {
                amount = Integer.parseInt(this.linkedSection.get("amount").toString());
            }
        } catch (Exception ex) {
            amount = 1;
        }

        return amount;
    }

    private boolean hasMeta() {
        return this.linkedSection.containsKey("meta");
    }

    private boolean isMetarizable() {
        return (!this.material.equals(Material.POTION) || this.material.equals(Material.valueOf("LINGERING_POTION")) || this.material.equals(Material.valueOf("SPLASH_POTION")));
    }

    private boolean isPotion() {
        return (this.material.equals(Material.POTION) || this.material.equals(Material.valueOf("LINGERING_POTION")) || this.material.equals(Material.valueOf("SPLASH_POTION")));
    }

    public ItemStack parse() {
        LinkedHashMap<String, Object> linkedMap = this.getLinkedMap();

        try {
            this.linkedSection = linkedMap;
            this.material = this.parseMaterial();
            int amount = this.getStackAmount();

            if (this.hasMeta() && this.isMetarizable()) {
                this.finalStack = new ItemStack(material, amount, this.getMeta());
            } else if (this.hasMeta() && !this.isMetarizable()) {
                this.finalStack = new ItemStack(material, amount, this.getPotionMeta());
            } else {
                this.finalStack = new ItemStack(material, amount);
            }

            if (this.linkedSection.containsKey("lore")) {
                this.parseLore();
            }

            if (this.isPotion() && this.linkedSection.containsKey("effects")) {
                this.parsePotionEffects();
            }

            if (this.linkedSection.containsKey("enchants")) {
                this.parseEnchants();
            }

            if (this.linkedSection.containsKey("name")) {
                this.parseCustomName();
            }

            return this.finalStack;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void parseCustomName() {
        String name =
                ChatColor.translateAlternateColorCodes('&', this.linkedSection.get("name").toString());
        ItemMeta im = this.finalStack.getItemMeta();

        im.setDisplayName(name);
        this.finalStack.setItemMeta(im);
    }

    private void parseEnchants() {
        if (this.isMetarizable()) {
            Enchantment en = null;
            int level = 0;

            ConfigurationSection newSection = (ConfigurationSection) (this.configSection);
            ConfigurationSection enchantSection = (ConfigurationSection) newSection.get("enchants");

            for (String key : enchantSection.getKeys(false)) {
                en = Enchantment.getByName(key.toUpperCase());
                level = Integer.parseInt(enchantSection.get(key).toString());

                if (en == null) {
                    continue;
                }

                this.finalStack.addUnsafeEnchantment(en, level);
            }
        }
    }

    private void parseLore() {
        List<String> lores = new ArrayList<String>();
        ItemMeta im = this.finalStack.getItemMeta();

        for (Object lore : (List<String>) this.linkedSection.get("lore")) {
            lores.add(ChatColor.translateAlternateColorCodes('&', lore.toString()));
        }

        im.setLore(lores);
        this.finalStack.setItemMeta(im);
    }

    private Material parseMaterial() {
        Material material = null;
        String materialString = this.linkedSection.get("item").toString();
        material = Material.getMaterial(materialString);

        return material;
    }

    private void parsePotionEffects() {
        PotionMeta customPotionMeta = (PotionMeta) this.finalStack.getItemMeta();
        for (Object potionEffect : (List<Object>) this.linkedSection.get("effects")) {
            LinkedHashMap<String, Object> potionEffectSection = (LinkedHashMap<String, Object>) potionEffect;

            if (!potionEffectSection.containsKey("type")) {
                continue;
            }

            PotionEffectType potionEffectType;
            int duration = 1;
            int amplifier = 0;

            potionEffectType =
                    PotionEffectType.getByName(potionEffectSection.get("type").toString().toUpperCase());

            if (potionEffectSection.containsKey("duration")) {
                duration = Integer.parseInt(potionEffectSection.get("duration").toString()) * 20;
            }

            if (potionEffectSection.containsKey("amplifier")) {
                amplifier = Integer.parseInt(potionEffectSection.get("amplifier").toString()) - 1;
            }

            if (potionEffectType == null) {
                continue;
            }

            customPotionMeta.addCustomEffect(new PotionEffect(potionEffectType, duration, amplifier),
                    true);
        }

        this.finalStack.setItemMeta(customPotionMeta);
    }

}
