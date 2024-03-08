package dev.shadowsoffire.apothic_spawners.modifiers;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;

public record StatModifier<T>(SpawnerStat<T> stat, T value, T min, T max) {

    public static Codec<StatModifier<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() -> SpawnerStats.REGISTRY.byNameCodec().dispatch(StatModifier::stat, SpawnerStat::getModifierCodec));

    public boolean apply(ApothSpawnerTile tile) {
        return this.stat.apply(this.value, this.min, this.max, tile);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.stat.getId());
        buf.writeNbt((CompoundTag) this.stat.getModifierCodec().encodeStart(NbtOps.INSTANCE, this).getOrThrow(false, ApothicSpawners.LOGGER::error));
    }

    public static StatModifier<?> read(FriendlyByteBuf buf) {
        SpawnerStat<?> stat = SpawnerStats.REGISTRY.get(buf.readResourceLocation());
        return stat.getModifierCodec().decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow(false, ApothicSpawners.LOGGER::error).getFirst();
    }

}
