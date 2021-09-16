package me.grabsky.azure.util;

import org.bukkit.enchantments.Enchantment;

public class Enchantments {
    
    public static Enchantment fromVanillaName(String name) {
        return switch (name.toLowerCase()) {
            case "aqua_affinity" -> Enchantment.WATER_WORKER;
            case "bane_of_arthropods" -> Enchantment.DAMAGE_ARTHROPODS;
            case "binding_curse" -> Enchantment.BINDING_CURSE;
            case "blast_protection" -> Enchantment.PROTECTION_EXPLOSIONS;
            case "channeling" -> Enchantment.CHANNELING;
            case "depth_strider" -> Enchantment.DEPTH_STRIDER;
            case "efficiency" -> Enchantment.DIG_SPEED;
            case "feather_falling" -> Enchantment.PROTECTION_FALL;
            case "fire_aspect" -> Enchantment.FIRE_ASPECT;
            case "fire_protection" -> Enchantment.PROTECTION_FIRE;
            case "flame" -> Enchantment.ARROW_FIRE;
            case "fortune" -> Enchantment.LOOT_BONUS_BLOCKS;
            case "frost_walker" -> Enchantment.FROST_WALKER;
            case "impaling" -> Enchantment.IMPALING;
            case "infinity" -> Enchantment.ARROW_INFINITE;
            case "knockback" -> Enchantment.KNOCKBACK;
            case "looting" -> Enchantment.LOOT_BONUS_MOBS;
            case "loyalty" -> Enchantment.LOYALTY;
            case "luck_of_the_sea" -> Enchantment.LUCK;
            case "lure" -> Enchantment.LURE;
            case "mending" -> Enchantment.MENDING;
            case "multishot" -> Enchantment.MULTISHOT;
            case "piercing" -> Enchantment.PIERCING;
            case "power" -> Enchantment.ARROW_DAMAGE;
            case "projectile_protection" -> Enchantment.PROTECTION_PROJECTILE;
            case "protection" -> Enchantment.PROTECTION_ENVIRONMENTAL;
            case "punch" -> Enchantment.ARROW_KNOCKBACK;
            case "quick_charge" -> Enchantment.QUICK_CHARGE;
            case "respiration" -> Enchantment.OXYGEN;
            case "riptide" -> Enchantment.RIPTIDE;
            case "sharpness" -> Enchantment.DAMAGE_ALL;
            case "silk_touch" -> Enchantment.SILK_TOUCH;
            case "smite" -> Enchantment.DAMAGE_UNDEAD;
            case "soul_speed" -> Enchantment.SOUL_SPEED;
            case "sweeping" -> Enchantment.SWEEPING_EDGE;
            case "thorns" -> Enchantment.THORNS;
            case "unbreaking" -> Enchantment.DURABILITY;
            case "vanishing_curse" -> Enchantment.VANISHING_CURSE;
            default -> null;
        };
    }
}
