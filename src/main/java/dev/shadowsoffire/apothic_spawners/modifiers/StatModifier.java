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
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
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

    public static final Codec<StatModifier<?>> CODEC = Codec.lazyInitialized(() -> SpawnerStats.REGISTRY.byNameCodec().dispatch(StatModifier::stat, StatModifier::modifierCodec));

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

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.stat.getId());
        buf.writeNbt(modifierCodec(this.stat).codec().encodeStart(NbtOps.INSTANCE, this).getOrThrow());
    }

    public static StatModifier<?> read(FriendlyByteBuf buf) {
        SpawnerStat<?> stat = SpawnerStats.REGISTRY.get(buf.readResourceLocation());
        return modifierCodec(stat).codec().decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow().getFirst();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> MapCodec<StatModifier<T>> modifierCodec(SpawnerStat<T> stat) {
        return (MapCodec) CODEC_CACHE.computeIfAbsent(stat, s -> (MapCodec) createModifierCodec(s));
    }

    private static <T> MapCodec<StatModifier<T>> createModifierCodec(SpawnerStat<T> stat) {
        return RecordCodecBuilder.mapCodec(inst -> inst
            .group(
                stat.getValueCodec().fieldOf("value").forGetter(StatModifier::value),
                stat.getValueCodec().optionalFieldOf("min").forGetter(StatModifier::min),
                stat.getValueCodec().optionalFieldOf("max").forGetter(StatModifier::max),
                Mode.CODEC.optionalFieldOf("mode", Mode.ADD).forGetter(StatModifier::mode))
            .apply(inst, (value, min, max, mode) -> new StatModifier<>(stat, value, min, max, mode)));
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
