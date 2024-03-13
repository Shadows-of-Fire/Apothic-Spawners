package dev.shadowsoffire.apothic_spawners.modifiers;

import java.util.Optional;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public interface SpawnerStat<T> {

    /**
     * Returns a codec for the value type of this stat.
     */
    Codec<T> getValueCodec();

    /**
     * Gets the current value of this stat.
     */
    T getValue(ApothSpawnerTile spawner);

    /**
     * Sets the current value of this stat.
     */
    void setValue(ApothSpawnerTile spawner, T value);

    /**
     * Computes a tooltip to be shown in the item tooltip and Jade/TOP.
     * If the returned component is empty, the tooltip line will not be shown.
     */
    Component getTooltip(ApothSpawnerTile spawner);

    /**
     * Applies this stat change to the selected spawner.
     *
     * @param spawner The spawner tile entity.
     * @param value   The change in value being applied.
     * @param min     The minimum acceptable value, or {@link Optional#empty()} if unlimited.
     * @param max     The maximum acceptable value, or {@link Optional#empty()} if unlimited.
     * @return True, if the application was successful (was a spawner stat changed).
     */
    boolean applyModifier(ApothSpawnerTile spawner, T value, Optional<T> min, Optional<T> max);

    /**
     * Returns the ID of this spawner stat. Used to build the lang key, and to identify it in json.
     */
    default ResourceLocation getId() {
        return SpawnerStats.REGISTRY.getKey(this);
    }

    default MutableComponent name() {
        return Component.translatable(this.getId().toLanguageKey("stat"));
    }

    default MutableComponent desc() {
        return Component.translatable(this.getId().toLanguageKey("stat", "desc"));
    }
}
