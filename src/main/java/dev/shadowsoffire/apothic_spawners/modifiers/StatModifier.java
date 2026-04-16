package dev.shadowsoffire.apothic_spawners.modifiers;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/**
 * Holds information needed to modify a spawner stat.
 *
 * @see SpawnerStat#applyModifier(ApothSpawnerTile, Object, Optional, Optional)
 */
public record StatModifier<T>(SpawnerStat<T> stat, T value, Optional<T> min, Optional<T> max, Mode mode) {

    private static final Map<SpawnerStat<?>, MapCodec<StatModifier<?>>> CODEC_CACHE = new ConcurrentHashMap<>();
    private static final Map<SpawnerStat<?>, StreamCodec<RegistryFriendlyByteBuf, StatModifier<?>>> STREAM_CODEC_CACHE = new ConcurrentHashMap<>();

    public static final Codec<StatModifier<?>> CODEC = Codec.lazyInitialized(() -> SpawnerStats.REGISTRY.byNameCodec().<StatModifier<?>>dispatch(StatModifier::stat, StatModifier::modifierCodec));
    public static final StreamCodec<RegistryFriendlyByteBuf, StatModifier<?>> STREAM_CODEC = ByteBufCodecs.registry(SpawnerStats.REGISTRY_KEY).dispatch(StatModifier::stat, StatModifier::modifierStreamCodec);

    public StatModifier(SpawnerStat<T> stat, T value) {
        this(stat, value, Optional.empty(), Optional.empty(), Mode.ADD);
    }

    public boolean apply(ApothSpawnerTile tile) {
        return switch (this.mode) {
            case ADD -> this.stat.applyModifier(tile, this.value, this.min, this.max);
            case SET -> {
                if (this.stat.getValue(tile) == this.value) {
                    yield false;
                }
                this.stat.setValue(tile, value);
                yield true;
            }
        };
    }

    public String getFormattedValue() {
        return this.stat.formatValue(this.value);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> MapCodec<StatModifier<T>> modifierCodec(SpawnerStat<T> stat) {
        return (MapCodec) CODEC_CACHE.computeIfAbsent(stat, s -> (MapCodec) createModifierCodec(s));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> StreamCodec<RegistryFriendlyByteBuf, StatModifier<T>> modifierStreamCodec(SpawnerStat<T> stat) {
        return (StreamCodec) STREAM_CODEC_CACHE.computeIfAbsent(stat, s -> (StreamCodec) createModifierStreamCodec(s));
    }

    private static <T> MapCodec<StatModifier<T>> createModifierCodec(SpawnerStat<T> stat) {
        return RecordCodecBuilder.mapCodec(inst -> inst
            .group(
                stat.valueCodec().fieldOf("value").forGetter(StatModifier::value),
                stat.valueCodec().optionalFieldOf("min").forGetter(StatModifier::min),
                stat.valueCodec().optionalFieldOf("max").forGetter(StatModifier::max),
                Mode.CODEC.optionalFieldOf("mode", Mode.ADD).forGetter(StatModifier::mode))
            .apply(inst, (value, min, max, mode) -> new StatModifier<>(stat, value, min, max, mode)));
    }

    private static <T> StreamCodec<RegistryFriendlyByteBuf, StatModifier<T>> createModifierStreamCodec(SpawnerStat<T> stat) {
        // ECJ fails generic inference here, unfortunately...
        return StreamCodec.<RegistryFriendlyByteBuf, StatModifier<T>, T, Optional<T>, Optional<T>, Mode>composite(
            stat.valueStreamCodec(), StatModifier::value,
            ByteBufCodecs.optional(stat.valueStreamCodec()), StatModifier::min,
            ByteBufCodecs.optional(stat.valueStreamCodec()), StatModifier::max,
            Mode.STREAM_CODEC, StatModifier::mode,
            (value, min, max, mode) -> new StatModifier<>(stat, value, min, max, mode));
    }

    /**
     * The mode of a {@link StatModifier}. If a modifier is in "add" mode, the value is added to the existing value.
     * In "set" mode, the old value is overwritten with the new value.
     */
    public static enum Mode implements StringRepresentable {
        ADD("add"),
        SET("set");

        public static final IntFunction<Mode> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<Mode> CODEC = StringRepresentable.fromValues(Mode::values);
        public static final StreamCodec<ByteBuf, Mode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

        private String name;

        private Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

}
