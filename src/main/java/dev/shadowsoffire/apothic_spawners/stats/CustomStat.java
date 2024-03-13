package dev.shadowsoffire.apothic_spawners.stats;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerStat;

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

}
