package dev.shadowsoffire.apothic_spawners.modifiers;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public interface SpawnerStat<T> {

    /**
     * Returns a codec that can de/serialize a stat modifier for this stat.
     */
    Codec<StatModifier<T>> getModifierCodec();

    /**
     * Gets the current value of this stat.
     */
    T getValue(ApothSpawnerTile spawner);

    /**
     * Computes a tooltip to be shown in the item tooltip and Jade/TOP.
     * If the returned component is empty, the tooltip line will not be shown.
     */
    Component getTooltip(ApothSpawnerTile spawner);

    /**
     * Applies this stat change to the selected spawner.
     *
     * @param value   The change in value being applied.
     * @param min     The minimum acceptable value.
     * @param max     The maximum acceptable value.
     * @param spawner The spawner tile entity.
     * @return If the application was successful (was a spawner stat changed).
     */
    boolean apply(T value, T min, T max, ApothSpawnerTile spawner);

    /**
     * Returns the ID of this spawner stat. Used to build the lang key, and to identify it in json.
     */
    default ResourceLocation getId() {
        return SpawnerStats.REGISTRY.getKey(this);
    }

    default MutableComponent name() {
        return Component.translatable("stat.apotheosis." + this.getId().getPath());
    }

    default MutableComponent desc() {
        return Component.translatable("stat.apotheosis." + this.getId().getPath() + ".desc");
    }
}
