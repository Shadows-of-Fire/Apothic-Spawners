package dev.shadowsoffire.apothic_spawners.modifiers;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Holds information needed to modify a spawner stat.
 *
 * @see SpawnerStat#applyModifier(ApothSpawnerTile, Object, Optional, Optional)
 */
public record StatModifier<T>(SpawnerStat<T> stat, T value, Optional<T> min, Optional<T> max) {

    private static final Map<SpawnerStat<?>, MapCodec<StatModifier<?>>> CODEC_CACHE = new ConcurrentHashMap<>();

    public static final Codec<StatModifier<?>> CODEC = Codec.lazyInitialized(() -> SpawnerStats.REGISTRY.byNameCodec().dispatch(StatModifier::stat, StatModifier::modifierCodec));

    public StatModifier(SpawnerStat<T> stat, T value) {
        this(stat, value, Optional.empty(), Optional.empty());
    }

    public boolean apply(ApothSpawnerTile tile) {
        return this.stat.applyModifier(tile, this.value, this.min, this.max);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.stat.getId());
        buf.writeNbt(modifierCodec(this.stat).codec().encodeStart(NbtOps.INSTANCE, this).getOrThrow());
    }

    public String getFormattedValue() {
        return this.stat.formatValue(this.value);
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
                stat.getValueCodec().optionalFieldOf("max").forGetter(StatModifier::max))
            .apply(inst, (value, min, max) -> new StatModifier<>(stat, value, min, max)));
    }

}
