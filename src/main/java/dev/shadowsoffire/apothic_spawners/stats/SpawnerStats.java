package dev.shadowsoffire.apothic_spawners.stats;

import java.util.function.Consumer;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class SpawnerStats {

    public static final Registry<SpawnerStat<?>> REGISTRY = new RegistryBuilder<SpawnerStat<?>>(ResourceKey.createRegistryKey(ApothicSpawners.loc("spawner_stat"))).create();

    public static final SpawnerStat<Integer> MIN_DELAY = register("min_delay", new VanillaStat(s -> s.spawner.minSpawnDelay, (s, v) -> s.spawner.minSpawnDelay = v));

    public static final SpawnerStat<Integer> MAX_DELAY = register("max_delay", new VanillaStat(s -> s.spawner.maxSpawnDelay, (s, v) -> s.spawner.maxSpawnDelay = v));

    public static final SpawnerStat<Integer> SPAWN_COUNT = register("spawn_count", new VanillaStat(s -> s.spawner.spawnCount, (s, v) -> s.spawner.spawnCount = v));

    public static final SpawnerStat<Integer> MAX_NEARBY_ENTITIES = register("max_nearby_entities", new VanillaStat(s -> s.spawner.maxNearbyEntities, (s, v) -> s.spawner.maxNearbyEntities = v));

    public static final SpawnerStat<Integer> REQ_PLAYER_RANGE = register("req_player_range", new VanillaStat(s -> s.spawner.requiredPlayerRange, (s, v) -> s.spawner.requiredPlayerRange = v));

    public static final SpawnerStat<Integer> SPAWN_RANGE = register("spawn_range", new VanillaStat(s -> s.spawner.spawnRange, (s, v) -> s.spawner.spawnRange = v));

    public static final SpawnerStat<Float> INITIAL_HEALTH = register("initial_health", new PercentageStat(1F));

    public static final SpawnerStat<Boolean> IGNORE_PLAYERS = register("ignore_players", new BooleanStat(false));

    public static final SpawnerStat<Boolean> IGNORE_CONDITIONS = register("ignore_conditions", new BooleanStat(false));

    public static final SpawnerStat<Boolean> REDSTONE_CONTROL = register("redstone_control", new BooleanStat(false));

    public static final SpawnerStat<Boolean> IGNORE_LIGHT = register("ignore_light", new BooleanStat(false));

    public static final SpawnerStat<Boolean> NO_AI = register("no_ai", new BooleanStat(false));

    public static final SpawnerStat<Boolean> SILENT = register("silent", new BooleanStat(false));

    public static final SpawnerStat<Boolean> YOUTHFUL = register("youthful", new BooleanStat(false));

    public static final SpawnerStat<Boolean> BURNING = register("burning", new BooleanStat(false));

    public static final SpawnerStat<Integer> ECHOING = register("echoing", new LevelStat(0));

    public static void bootstrap() {}

    public static void generateTooltip(ApothSpawnerTile tile, Consumer<Component> list) {
        for (SpawnerStat<?> stat : REGISTRY) {
            Component comp = stat.getTooltip(tile);
            if (!comp.getString().isEmpty()) {
                list.accept(comp);
            }
        }
    }

    private static <T extends SpawnerStat<?>> T register(String id, T t) {
        Registry.register(REGISTRY, ApothicSpawners.loc(id), t);
        return t;
    }

}
