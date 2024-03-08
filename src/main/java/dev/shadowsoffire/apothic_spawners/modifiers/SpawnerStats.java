package dev.shadowsoffire.apothic_spawners.modifiers;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerBlock;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class SpawnerStats {

    public static final Registry<SpawnerStat<?>> REGISTRY = new RegistryBuilder<SpawnerStat<?>>(ResourceKey.createRegistryKey(ApothicSpawners.loc("spawner_stat"))).create();

    public static final SpawnerStat<Short> MIN_DELAY = register("min_delay", new ShortStat(s -> s.spawner.minSpawnDelay, (s, v) -> s.spawner.minSpawnDelay = v));

    public static final SpawnerStat<Short> MAX_DELAY = register("max_delay", new ShortStat(s -> s.spawner.maxSpawnDelay, (s, v) -> s.spawner.maxSpawnDelay = v));

    public static final SpawnerStat<Short> SPAWN_COUNT = register("spawn_count", new ShortStat(s -> s.spawner.spawnCount, (s, v) -> s.spawner.spawnCount = v));

    public static final SpawnerStat<Short> MAX_NEARBY_ENTITIES = register("max_nearby_entities", new ShortStat(s -> s.spawner.maxNearbyEntities, (s, v) -> s.spawner.maxNearbyEntities = v));

    public static final SpawnerStat<Short> REQ_PLAYER_RANGE = register("req_player_range", new ShortStat(s -> s.spawner.requiredPlayerRange, (s, v) -> s.spawner.requiredPlayerRange = v));

    public static final SpawnerStat<Short> SPAWN_RANGE = register("spawn_range", new ShortStat(s -> s.spawner.spawnRange, (s, v) -> s.spawner.spawnRange = v));

    public static final SpawnerStat<Boolean> IGNORE_PLAYERS = register("ignore_players", new BoolStat(s -> s.ignoresPlayers, (s, v) -> s.ignoresPlayers = v));

    public static final SpawnerStat<Boolean> IGNORE_CONDITIONS = register("ignore_conditions", new BoolStat(s -> s.ignoresConditions, (s, v) -> s.ignoresConditions = v));

    public static final SpawnerStat<Boolean> REDSTONE_CONTROL = register("redstone_control", new BoolStat(s -> s.redstoneControl, (s, v) -> s.redstoneControl = v));

    public static final SpawnerStat<Boolean> IGNORE_LIGHT = register("ignore_light", new BoolStat(s -> s.ignoresLight, (s, v) -> s.ignoresLight = v));

    public static final SpawnerStat<Boolean> NO_AI = register("no_ai", new BoolStat(s -> s.hasNoAI, (s, v) -> s.hasNoAI = v));

    public static final SpawnerStat<Boolean> SILENT = register("silent", new BoolStat(s -> s.silent, (s, v) -> s.silent = v));

    public static final SpawnerStat<Boolean> BABY = register("baby", new BoolStat(s -> s.baby, (s, v) -> s.baby = v));

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

    private static abstract class Base<T> implements SpawnerStat<T> {

        protected final Function<ApothSpawnerTile, T> getter;
        protected final BiConsumer<ApothSpawnerTile, T> setter;

        private Base(Function<ApothSpawnerTile, T> getter, BiConsumer<ApothSpawnerTile, T> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public T getValue(ApothSpawnerTile spawner) {
            return this.getter.apply(spawner);
        }

    }

    private static class BoolStat extends Base<Boolean> {

        private final Codec<StatModifier<Boolean>> modifierCodec = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.BOOL.fieldOf("value").forGetter(StatModifier::value))
            .apply(inst, value -> new StatModifier<>(this, value, false, true)));

        private BoolStat(Function<ApothSpawnerTile, Boolean> getter, BiConsumer<ApothSpawnerTile, Boolean> setter) {
            super(getter, setter);
        }

        @Override
        public Codec<StatModifier<Boolean>> getModifierCodec() {
            return this.modifierCodec;
        }

        @Override
        public Component getTooltip(ApothSpawnerTile spawner) {
            return this.getValue(spawner) ? this.name().withStyle(ChatFormatting.DARK_GREEN) : CommonComponents.EMPTY;
        }

        @Override
        public boolean apply(Boolean value, Boolean min, Boolean max, ApothSpawnerTile spawner) {
            boolean old = this.getter.apply(spawner);
            this.setter.accept(spawner, value);
            return old != this.getter.apply(spawner);
        }

    }

    private static class ShortStat extends Base<Short> {

        public static final Codec<Short> BOUNDS_CODEC = Codec.intRange(-1, Short.MAX_VALUE).xmap(Integer::shortValue, Short::intValue);

        private final Codec<StatModifier<Short>> modifierCodec = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.SHORT.fieldOf("value").forGetter(StatModifier::value),
                BOUNDS_CODEC.fieldOf("min").forGetter(StatModifier::min),
                BOUNDS_CODEC.fieldOf("max").forGetter(StatModifier::max))
            .apply(inst, (value, min, max) -> new StatModifier<>(this, value, min == -1 ? 0 : min, max == -1 ? Short.MAX_VALUE : max)));

        private ShortStat(Function<ApothSpawnerTile, Integer> getter, BiConsumer<ApothSpawnerTile, Short> setter) {
            super(tile -> getter.apply(tile).shortValue(), setter);
        }

        @Override
        public Codec<StatModifier<Short>> getModifierCodec() {
            return this.modifierCodec;
        }

        @Override
        public Component getTooltip(ApothSpawnerTile spawner) {
            return ApothSpawnerBlock.concat(this.name(), this.getValue(spawner));
        }

        @Override
        public boolean apply(Short value, Short min, Short max, ApothSpawnerTile spawner) {
            int old = this.getter.apply(spawner);
            this.setter.accept(spawner, (short) Mth.clamp(old + value, min, max));
            return old != this.getter.apply(spawner);
        }

    }

}
