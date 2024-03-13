package dev.shadowsoffire.apothic_spawners.stats;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public abstract class CustomStat<T> implements SpawnerStat<T> {

    private final T defaultValue;

    public CustomStat(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getValue(ApothSpawnerTile spawner) {
        return (T) spawner.getStatsMap().getOrDefault(this, defaultValue);
    }

    @Override
    public void setValue(ApothSpawnerTile spawner, T value) {
        spawner.getStatsMap().put(this, value);
    }

    @Override
    public final Component getTooltip(ApothSpawnerTile spawner) {
        return this.getValue(spawner) == this.defaultValue ? CommonComponents.EMPTY : getTooltipImpl(spawner);
    }

    public abstract Component getTooltipImpl(ApothSpawnerTile spawner);

}
