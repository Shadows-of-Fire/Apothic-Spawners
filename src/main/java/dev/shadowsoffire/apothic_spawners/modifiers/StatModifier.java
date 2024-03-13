package dev.shadowsoffire.apothic_spawners.modifiers;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;

/**
 * Holds information needed to modify a spawner stat.
 *
 * @see SpawnerStat#applyModifier(ApothSpawnerTile, Object, Optional, Optional)
 */
public record StatModifier<T>(SpawnerStat<T> stat, T value, Optional<T> min, Optional<T> max) {

    public StatModifier(SpawnerStat<T> stat, T value) {
        this(stat, value, Optional.empty(), Optional.empty());
    }

    public static Codec<StatModifier<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() -> SpawnerStats.REGISTRY.byNameCodec().dispatch(StatModifier::stat, StatModifier::modifierCodec));

    public boolean apply(ApothSpawnerTile tile) {
        return this.stat.applyModifier(tile, this.value, this.min, this.max);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.stat.getId());
        buf.writeNbt(modifierCodec(this.stat).encodeStart(NbtOps.INSTANCE, this).getOrThrow(false, ApothicSpawners.LOGGER::error));
    }

    public String getFormattedValue() {
        return this.stat.formatValue(this.value);
    }

    public static StatModifier<?> read(FriendlyByteBuf buf) {
        SpawnerStat<?> stat = SpawnerStats.REGISTRY.get(buf.readResourceLocation());
        return modifierCodec(stat).decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow(false, ApothicSpawners.LOGGER::error).getFirst();
    }

    public static <T> Codec<StatModifier<T>> modifierCodec(SpawnerStat<T> stat) {
        return RecordCodecBuilder.create(inst -> inst
            .group(
                stat.getValueCodec().fieldOf("value").forGetter(StatModifier::value),
                ExtraCodecs.strictOptionalField(stat.getValueCodec(), "min").forGetter(StatModifier::min),
                ExtraCodecs.strictOptionalField(stat.getValueCodec(), "max").forGetter(StatModifier::max))
            .apply(inst, (value, min, max) -> new StatModifier<>(stat, value, min, max)));
    }

}
