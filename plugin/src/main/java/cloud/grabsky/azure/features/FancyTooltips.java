package cloud.grabsky.azure.features;

import cloud.grabsky.bedrock.components.ComponentBuilder;
import kotlin.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// NOTE: This really should not be part of this plugin.
public final class FancyTooltips implements Listener {

    public static String getTrimPatternTranslationKey(final @NotNull TrimPattern pattern) {
        return "trim_pattern." + pattern.key().namespace() + "." + pattern.key().value();
    }

    public static String getTrimMaterialTranslationKey(final @NotNull TrimMaterial material) {
        return "trim_material." + material.key().namespace() + "." + material.key().value();
    }

    private static final Component SEGMENT_SEPARATOR = Component.text("\u2006");

    public static @NotNull ItemStack create(final @NotNull ItemStack item, final @Nullable Event associatedEvent) {
        // Early returning items with no meta.
        if (item.hasItemMeta() == false)
            return item;
        // Modifying item.
        item.editMeta(meta -> {
            final @NotNull List<Component> existingLore = meta.hasLore() ? meta.lore().stream().takeWhile(c -> c.equals(SEGMENT_SEPARATOR) == false).toList() : new ArrayList<>(); // Cannot be null.
            // ...
            final @NotNull List<Component> resultLore = new ArrayList<>(existingLore);

            // Trims...
            if (meta instanceof ArmorMeta armor) {
                // Getting the trim.
                final @Nullable ArmorTrim trim = armor.getTrim();
                // Proceeding to append trims to the lore.
                if (trim != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
                    // Seperating from above...
                    resultLore.add(SEGMENT_SEPARATOR);
                    // Appending segment title. Translation key will be used in the future.
                    resultLore.add(ComponentBuilder.of("").decoration(TextDecoration.ITALIC, false).append("Upgrades:", NamedTextColor.GRAY).build());
                    // Appending (translatable) trim pattern name.
                    resultLore.add(ComponentBuilder.of(" ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_GRAY).appendTranslation(getTrimPatternTranslationKey(trim.getPattern())).build());
                    // Appending (translatable) trim material name.
                    resultLore.add(ComponentBuilder.of(" ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_GRAY).appendTranslation(getTrimMaterialTranslationKey(trim.getMaterial())).build());
                // Othwerwise, HIDE_ARMOR_TRIM flag is being removed.
                } else meta.removeItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
            }

            // Enchantments...
            final Map<NamespacedKey, Pair<Enchantment, Integer>> enchantments = new HashMap<>();
            // Adding existing enchantments to the map.
            item.getEnchantments().forEach((enchantment, level) -> enchantments.put(enchantment.getKey(), new Pair<>(enchantment, level)));
            // Adding enchantments that are about to be added from EnchantItemEvent.
            if (associatedEvent instanceof EnchantItemEvent event)
                event.getEnchantsToAdd().forEach((enchantment, level) -> enchantments.put(enchantment.getKey(), new Pair<>(enchantment, level)));
            // Proceeding to append enchantments to the lore.
            if (enchantments.isEmpty() == false) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                // Seperating from above...
                resultLore.add(SEGMENT_SEPARATOR);
                // Appending segment title. Translation key will be used in the future.
                resultLore.add(ComponentBuilder.of("").decoration(TextDecoration.ITALIC, false).append("Enchantments:", NamedTextColor.GRAY).build());
                // Iterating over "collected" enchantments map.
                enchantments.entrySet().stream()
                        // Sorting by level, from highest to lowest.
                        .sorted((a, b) -> Integer.compare(b.getValue().getSecond(), a.getValue().getSecond()))
                        .forEach((entry) -> {
                            // Creating a builder and appending (translatable) enchantment name to it.
                            final ComponentBuilder builder = ComponentBuilder.of(" ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_GRAY).appendTranslation(entry.getValue().getFirst());
                            // Appending level if greater than 0.
                            if (entry.getValue().getSecond() > 1)
                                builder.append(" ").appendTranslation("enchantment.level." + entry.getValue().getSecond());
                            // Building and adding new line.
                            resultLore.add(builder.build());
                        });
            // Othwerwise, HIDE_ENCHANTS flag is being removed.
            } else meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);

            // Applying the lore.
            meta.lore(resultLore);
        });
        // Returning modified item.
        return item;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchantItemEvent(final @NotNull EnchantItemEvent event) {
        create(event.getItem(), event); // This event's ItemStack is mutable.
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareSmithingEvent(final @NotNull PrepareSmithingEvent event) {
        if (event.getResult() == null)
            return;
        // ...
        final @NotNull ItemStack item = create(event.getResult(), event); // This event's ItemStack is immutable.
        // ...
        event.setResult(item);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareAnvilEvent(final @NotNull PrepareAnvilEvent event) {
        final var s1 = System.nanoTime();
        if (event.getInventory().getFirstItem() == null || event.getInventory().getSecondItem() == null || event.getInventory().getResult() == null)
            return;
        // ...
        if (event.getInventory().getFirstItem().getEnchantments().equals(event.getInventory().getResult().getEnchantments()))
            return;
        // ...
        final @NotNull ItemStack item = create(event.getResult(), event); // This event's ItemStack is immutable.
        final var s2 = System.nanoTime();
        System.out.println(BigDecimal.valueOf((double) (s2 - s1) / 1_000_000.0) + "ms");
        // ...
        event.setResult(item);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareGrindstoneEvent(final @NotNull PrepareGrindstoneEvent event) {
        if (event.getResult() == null)
            return;
        // ...
        final @NotNull ItemStack item = create(event.getResult(), event); // This event's ItemStack is immutable.
        // ...
        event.setResult(item);
    }

    @EventHandler
    public void onLootGenerate(final @NotNull LootGenerateEvent event) {
        // Modifying ItemStack(s) in loot list. According to Bukkit, this list is mutable.
        event.getLoot().forEach(item -> create(item, event));
    }

    @EventHandler
    public void onEntityDeath(final @NotNull EntityDeathEvent event) {
        // Modifying ItemStack(s) in loot list. It's not mentioned anywhere, but this list should be mutable.
        event.getDrops().forEach(item -> create(item, event));
    }

}
