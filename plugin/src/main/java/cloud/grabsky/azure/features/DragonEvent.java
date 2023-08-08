package cloud.grabsky.azure.features;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.bedrock.components.ComponentBuilder;
import com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent;
import com.destroystokyo.paper.event.entity.EnderDragonShootFireballEvent;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DragonEvent implements Listener {

    private final @NotNull Azure plugin;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull World world;

    @Getter(AccessLevel.PUBLIC)
    private @Nullable EnderDragon dragon;

    @Getter(AccessLevel.PUBLIC)
    private @Nullable BossBar bossBar;

    public DragonEvent(final @NotNull Azure plugin, final @NotNull World world) {
        this.plugin = plugin;
        this.world = world;
        // ...
        this.refresh();
        // ...
        world.spawnEntity(new Location(world, 0, 120, 0), EntityType.ENDER_DRAGON, CreatureSpawnEvent.SpawnReason.CUSTOM, (entity) -> {
            this.bossBar = Bukkit.createBossBar("Ender Dragon", BarColor.PURPLE, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC, BarFlag.CREATE_FOG, BarFlag.DARKEN_SKY);
            // ...
            final EnderDragon dragon = (EnderDragon) entity;
            // ...
            final AttributeInstance attr = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(400.0d);
                dragon.setHealth(attr.getBaseValue());
            }
            dragon.setPhase(EnderDragon.Phase.CIRCLING);
            // ...
            this.dragon = dragon;
            // ...
            world.getPlayers().forEach(player -> bossBar.addPlayer(player));
        });
    }

    public void refresh() {
        HandlerList.unregisterAll(this);
        // ...
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void cancel() {
        HandlerList.unregisterAll(this);
        // ...
        if (this.dragon != null)
            this.dragon.remove();
        // ...
        if (this.bossBar != null)
            this.bossBar.removeAll();
    }

    @EventHandler
    public void onDragonGetHit(final EntityDamageEvent event) {
        if (this.dragon != null && event.getEntity() == this.dragon && this.bossBar != null) {
            plugin.getComponentLogger().info(ComponentBuilder.of("Dragon has been damaged by ").append(event.getDamage() + "", NamedTextColor.YELLOW).append("... It's now at " + this.dragon.getHealth() + "HP").build());
            // ...
            final AttributeInstance attr = this.dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                final double progress = this.dragon.getHealth() / attr.getBaseValue();
                // Delaying this for the next tick in case dragon decides to die.
                plugin.getBedrockScheduler().run(1L, (task) -> {
                    this.bossBar.setProgress(dragon.getHealth() > 1.0 ? progress : 0);
                });

            }

        }
    }

    @EventHandler
    public void onWorldChange(final PlayerChangedWorldEvent event) {
        if (event.getPlayer().getWorld() == this.world)
            if (this.bossBar != null)
                this.bossBar.addPlayer(event.getPlayer());
        else if (event.getFrom() == this.world && this.bossBar != null)
            bossBar.removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onDragonDeath(final EntityDeathEvent event) {
        if (event.getEntity() == this.dragon && this.bossBar != null) {
            this.bossBar.removeAll();
        }
    }

    @EventHandler
    public void onDragonFireballShoot(final EnderDragonShootFireballEvent event) {
        // ...
    }

    @EventHandler
    public void onDragonPhaseChange(final EnderDragonChangePhaseEvent event) {
        plugin.getComponentLogger().info(ComponentBuilder.of("Trying to change phase to ").append(event.getNewPhase() + "...", NamedTextColor.GRAY).build());
        // Letting dragon do his thing after he's already dead...
        if (event.getEntity().getHealth() <= 1.0)
            return;
        // ...
        if (event.getNewPhase() != EnderDragon.Phase.DYING && event.getNewPhase() != EnderDragon.Phase.CIRCLING)
            event.setNewPhase(EnderDragon.Phase.CIRCLING);
        // ...
        plugin.getComponentLogger().info(ComponentBuilder.of("Dragon state changed to ").append(event.getNewPhase() + "", NamedTextColor.GREEN).build());
    }

    @EventHandler
    public void onDragonFireBall(final EnderDragonFireballHitEvent event) {
        // ...
    }

}
