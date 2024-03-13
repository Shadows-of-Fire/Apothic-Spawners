package dev.shadowsoffire.apothic_spawners.stats;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.network.chat.Component;

/**
 * Base class for implementation of vanilla stats, as they are all shorts backed by individual fields.
 */
class VanillaStat implements SpawnerStat<Integer> {

    private static final Codec<Integer> SHORT_INT = Codec.intRange(-1, Short.MAX_VALUE);

    protected final Function<ApothSpawnerTile, Integer> getter;
    protected final BiConsumer<ApothSpawnerTile, Integer> setter;

    VanillaStat(Function<ApothSpawnerTile, Integer> getter, BiConsumer<ApothSpawnerTile, Integer> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Codec<Integer> getValueCodec() {
        return SHORT_INT;
    }

    @Override
    public Integer getValue(ApothSpawnerTile spawner) {
        return this.getter.apply(spawner);
    }

    @Override
    public void setValue(ApothSpawnerTile spawner, Integer value) {
        this.setter.accept(spawner, value);
    }

    @Override
    public Component getTooltip(ApothSpawnerTile spawner) {
        return SpawnerStat.createTooltip(this, this.getValue(spawner).toString());
    }

    @Override
    public boolean applyModifier(ApothSpawnerTile spawner, Integer value, Optional<Integer> min, Optional<Integer> max) {
        Integer old = this.getValue(spawner);
        this.setValue(spawner, clamp(old + value, min, max));
        return old != this.getValue(spawner);
    }

    private Integer clamp(Integer value, Optional<Integer> min, Optional<Integer> max) {
        if (min.isPresent()) value = Math.max(value, min.get());
        if (max.isPresent()) value = Math.min(value, max.get());
        return value;
    }

}
