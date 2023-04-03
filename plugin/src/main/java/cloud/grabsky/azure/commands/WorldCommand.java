package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.arguments.WorldSeedArgument;
import cloud.grabsky.azure.arguments.WorldTimeArgument;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.world.WorldManager;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.RootCommandInput;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import io.papermc.paper.math.Position;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.lang.String.format;

public final class WorldCommand extends RootCommand {

    private final Azure plugin;
    private final WorldManager worlds;

    public WorldCommand(final Azure plugin) {
        super("world", null, "azure.command.world", "/world", "Manage your worlds in-game.");
        this.plugin = plugin;
        this.worlds = plugin.getWorldManager();
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        final RootCommandInput input = context.getInput();
        // ...
        if (index == 0) return CompletionsProvider.of(
                Stream.of("create", "delete", "gamerule", "info", "load", "spawnpoint", "teleport", "time", "weather").filter(literal -> sender.hasPermission(this.getPermission() + "." + literal) == true).toList()
        );
        // ...
        final String literal = input.at(1).toLowerCase();
        if (sender.hasPermission(this.getPermission() + "." + literal) == false)
            return CompletionsProvider.EMPTY;
        // ...
        return switch (literal) {
            case "create" -> switch (index) {
                case 2 -> CompletionsProvider.of(World.Environment.class);
                case 3 -> CompletionsProvider.of(WorldType.class);
                case 4 -> CompletionsProvider.of("natural");
                case 5 -> WorldSeedArgument.INSTANCE;
                case 6 -> CompletionsProvider.of("-a");
                default -> CompletionsProvider.EMPTY;
            };
            case "delete" -> (index == 1)
                    ? CompletionsProvider.of(World.class)
                    : CompletionsProvider.of("--confirm");
            case "gamerule" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> CompletionsProvider.of(GameRule.class);
                case 3 -> {
                    final GameRule<?> gameRule = GameRule.getByName(input.at(3));
                    yield (gameRule != null)
                            ? CompletionsProvider.of(gameRule.getType())
                            : CompletionsProvider.EMPTY;
                }
                default -> CompletionsProvider.EMPTY;
            };
            case "info" -> (index == 1)
                    ? CompletionsProvider.of(World.class)
                    : CompletionsProvider.EMPTY;
            case "load" -> (index == 1)
                    ? CompletionsProvider.of(Stream.of(plugin.getServer().getWorldContainer().listFiles()).filter(File::isDirectory).filter(dir -> new File(dir, "level.dat").exists() == true).map(File::getName).toList())
                    : CompletionsProvider.EMPTY;
            case "spawnpoint" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> CompletionsProvider.of("@x @y @z");
                case 3 -> CompletionsProvider.of("@y @z");
                case 4 -> CompletionsProvider.of("@z");
                default -> CompletionsProvider.EMPTY;
            };
            case "teleport" -> switch (index) {
                case 1 -> CompletionsProvider.of(Player.class);
                case 2 -> CompletionsProvider.of(World.class);
                default -> CompletionsProvider.EMPTY;
            };
            case "time" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> WorldTimeArgument.INSTANCE;
                default -> CompletionsProvider.EMPTY;
            };
            case "weather" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> CompletionsProvider.of("clear", "rain", "thunder");
                default -> CompletionsProvider.EMPTY;
            };
            // ...
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (arguments.hasNext() == false) {
            Message.of(PluginLocale.COMMAND_WORLD_HELP).send(context.getExecutor().asCommandSender());
        } else switch (arguments.next(String.class).asRequired().toLowerCase()) {
            default -> Message.of(PluginLocale.COMMAND_WORLD_HELP).send(context.getExecutor().asCommandSender());
            case "create" -> this.onWorldCreate(context, arguments);
            case "delete" -> this.onWorldDelete(context, arguments);
            case "gamerule" -> this.onWorldGamerule(context, arguments);
            case "info" -> this.onWorldInfo(context, arguments);
            case "load" -> this.onWorldLoad(context, arguments);
            case "spawnpoint" -> this.onWorldSpawnPoint(context, arguments);
            case "teleport" -> this.onWorldTeleport(context, arguments);
            case "time" -> this.onWorldTime(context, arguments);
            case "weather" -> this.onWorldWeather(context, arguments);
        }
    }

    // TO-DO: Improve "importing" of "existing" world.
    private void onWorldCreate(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".create") == true) {
            final NamespacedKey key = arguments.next(NamespacedKey.class).asRequired();
            final World.Environment environment = arguments.next(World.Environment.class).asRequired();
            final WorldType type = arguments.next(WorldType.class).asRequired();
            final String generator = arguments.next(String.class).asRequired();
            final Long seed = arguments.next(Long.class, WorldSeedArgument.INSTANCE).asOptional(null);
            final String[] flags = arguments.next(String.class, StringArgument.GREEDY).asOptional("").split(" ");
            // ...
            try {
                final World world = plugin.getWorldManager().createWorld(key, environment, type, generator, seed, containsIgnoreCase(flags, "-a"));
                // ...
                if (world != null) {
                    // Sending success message to command sender.
                    Message.of(PluginLocale.COMMAND_WORLD_CREATE_SUCCESS).placeholder("world", world.key()).send(sender);
                    return;
                }
                // Sending error message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_CREATE_FAILURE_OTHER).placeholder("world", key).send(sender);
            } catch (final IllegalStateException e) {
                // Sending error message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_CREATE_FAILURE_ALREADY_EXISTS).placeholder("world", key).send(sender);
            } catch (final IOException e) {
                e.printStackTrace();
                // Sending error message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_CREATE_FAILURE_OTHER).placeholder("world", key).send(sender);
            }
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onWorldDelete(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".delete") == true) {
            final World world = arguments.next(World.class).asRequired();
            final String[] flags = arguments.next(String.class, StringArgument.GREEDY).asOptional("").split(" ");
            // Checking if --confirm flag is present.
            if (containsIgnoreCase(flags, "--confirm") == true) {
                // Checking if specified world is NOT default/main/primary world on this server.
                if (worlds.getPrimaryWorld().key().equals(world.getKey()) == false) {
                    // Teleporting players away from the world that is about to be deleted.
                    for (final Player player : world.getPlayers()) {
                        player.teleport(worlds.getPrimaryWorld().getSpawnLocation(), TeleportCause.PLUGIN);
                    }
                    // unloading the world (no save action is performed)
                    Bukkit.unloadWorld(world, false);
                    // Deleting world directory...
                    if (deleteDirectory(world.getWorldFolder()) == true) {
                        // Sending success message to command sender.
                        Message.of(PluginLocale.COMMAND_WORLD_DELETE_SUCCESS).placeholder("world", world.key()).send(sender);
                        return;
                    }
                    // Sending error message to command sender.
                    Message.of(PluginLocale.COMMAND_WORLD_DELETE_FAILURE_OTHER)
                            .placeholder("world", world.key())
                            .send(sender);
                    return;
                }
                // Sending error message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_DELETE_FAILURE_PRIMARY_WORLD).send(sender);
                return;
            }
            // Sending confirmation message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_DELETE_CONFIRM)
                    .placeholder("input", context.getInput())
                    .placeholder("world", world.key())
                    .send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    @SuppressWarnings("unchecked")
    private void onWorldGamerule(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".gamerule") == true) {
            final World world = arguments.next(World.class).asRequired();
            final GameRule<Object> gameRule = arguments.next(GameRule.class).asRequired();
            final Object value = arguments.next(gameRule.getType()).asOptional();
            // ...
            if (value == null) {
                // Sending message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_GAMERULE_INFO)
                        .placeholder("rule", gameRule.getName())
                        .placeholder("value", world.getGameRuleValue(gameRule))
                        .send(sender);
            } else if (world.setGameRule(gameRule, value) == true) {
                // Sending success message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_GAMERULE_SET_SUCCESS)
                        .placeholder("rule", gameRule.getName())
                        .placeholder("value", value)
                        .send(sender);
                return;
            }
            // Sending error message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_GAMERULE_SET_FAILURE).placeholder("rule", gameRule.getName()).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onWorldInfo(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".info") == true) {
            final World world = arguments.next(World.class).asOptional(context.getExecutor().asPlayer().getWorld());
            // Getting spawn location of specified world.
            final Location spawnLoc = world.getSpawnLocation();
            // Sending message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_INFO)
                    .placeholder("world_name", world.getName())
                    .placeholder("world_key", world.getKey())
                    .placeholder("world_seed", world.getSeed())
                    .placeholder("world_environment", world.getEnvironment().name())
                    .placeholder("world_online", world.getPlayerCount())
                    .placeholder("spawn_x", spawnLoc.x())
                    .placeholder("spawn_y", spawnLoc.y())
                    .placeholder("spawn_z", spawnLoc.z())
                    .send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onWorldLoad(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".load") == true) {
            final NamespacedKey key = arguments.next(NamespacedKey.class).asRequired();
            try {
                // Trying to load the world...
                plugin.getWorldManager().loadWorld(key, true);
                // Sending success message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_LOAD_SUCCESS).placeholder("world", key).send(sender);
            } catch (final IllegalStateException e) {
                // Sending error message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_LOAD_FAILURE_NOT_FOUND).placeholder("world", key).send(sender);
            } catch (final IOException e) {
                e.printStackTrace();
                // Sending error message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_LOAD_FAILURE_OTHER).placeholder("world", key).send(sender);
            }
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onWorldSpawnPoint(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".spawnpoint") == true) {
            final World world = arguments.next(World.class).asRequired();
            final Position position = arguments.next(Position.class).asRequired();
            final Location location = new Location(world, position.x(), position.y(), position.z());
            // Updating spawn location.
            world.setSpawnLocation(location);
            // Sending message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_SPAWNPOINT_SET_SUCCESS)
                    .placeholder("x", format("%.2f", position.x()))
                    .placeholder("y", format("%.2f", position.y()))
                    .placeholder("z", format("%.2f", position.z()))
                    .send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onWorldTeleport(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".teleport") == true) {
            final Player target = arguments.next(Player.class).asRequired();
            final World world = arguments.next(World.class).asRequired();
            // ...
            target.teleportAsync(world.getSpawnLocation(), TeleportCause.PLUGIN);
            Message.of(PluginLocale.COMMAND_WORLD_TELEPORT)
                    .placeholder("player", target)
                    .placeholder("world", world.key())
                    .send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onWorldTime(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".time") == true) {
            final World world = arguments.next(World.class).asRequired();
            final Long time = arguments.next(Long.class, WorldTimeArgument.INSTANCE).asOptional(null);
            // ...
            if (time == null) {
                Message.of(PluginLocale.COMMAND_WORLD_TIME_INFO)
                        .placeholder("world", world.key())
                        .placeholder("time", world.getTime())
                        .send(sender);
                return;
            }
            world.setTime(time);
            Message.of(PluginLocale.COMMAND_WORLD_TIME_SET_SUCCESS)
                    .placeholder("world", world.key())
                    .placeholder("time", world.getTime())
                    .send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onWorldWeather(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".weather") == true) {
            final World world = arguments.next(World.class).asRequired();
            final String weather = arguments.next(String.class).asRequired();
            // ...
            switch (weather.toLowerCase()) {
                case "clear" -> {
                    world.setStorm(false);
                    world.setThundering(false);
                }
                case "rain" -> {
                    world.setStorm(true);
                    world.setThundering(false);
                }
                case "thunder" -> {
                    world.setStorm(true);
                    world.setThundering(true);
                }
                default -> {
                    // Sending error message to command sender.
                    Message.of(PluginLocale.COMMAND_WORLD_WEATHER_SET_FAILURE_INVALID_TYPE)
                            .placeholder("world", world.key())
                            .placeholder("weather", weather)
                            .send(sender);
                }
            }
            // Sending success message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_WEATHER_SET_SUCCESS)
                    .placeholder("world", world.key())
                    .placeholder("weather", weather)
                    .send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private static boolean containsIgnoreCase(final String[] arr, final String search) {
        for (final String element : arr) {
            if (search.equalsIgnoreCase(element) == true)
                return true;
        }
        return false;
    }

    private static boolean deleteDirectory(final File file) {
        try (final Stream<Path> walk = Files.walk(file.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

}
