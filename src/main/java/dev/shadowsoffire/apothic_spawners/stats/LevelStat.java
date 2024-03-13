package dev.shadowsoffire.apothic_spawners.stats;

import java.util.Optional;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.network.chat.Component;

public class LevelStat extends CustomStat<Integer> {

    public LevelStat(Integer defaultValue) {
        super(defaultValue);
    }

    @Override
    public Codec<Integer> getValueCodec() {
        return Codec.INT;
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

    @Override
    public Component getTooltipImpl(ApothSpawnerTile spawner) {
        return SpawnerStat.createTooltip(this, this.formatValue(this.getValue(spawner)));
    }

    @Override
    public String formatValue(Integer value) {
        if (value < 0) {
            return Component.literal("-").append(Component.translatable("enchantment.level." + Math.abs(value))).getString();
        }
        return Component.translatable("enchantment.level." + Math.abs(value)).getString();
    }

}
