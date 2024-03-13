package dev.shadowsoffire.apothic_spawners.stats;

import java.util.Optional;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PercentageStat extends CustomStat<Float> {

    public PercentageStat(Float defaultValue) {
        super(defaultValue);
    }

    @Override
    public Codec<Float> getValueCodec() {
        return Codec.floatRange(-1, 1);
    }

    @Override
    public Component getTooltipImpl(ApothSpawnerTile spawner) {
        return SpawnerStat.createTooltip(this, this.formatValue(this.getValue(spawner)));
    }

    @Override
    public boolean applyModifier(ApothSpawnerTile spawner, Float value, Optional<Float> min, Optional<Float> max) {
        Float old = this.getValue(spawner);
        this.setValue(spawner, this.clamp(old + value, min, max));
        return old != this.getValue(spawner);
    }

    @Override
    public String formatValue(Float value) {
        return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value * 100) + "%";
    }

    private Float clamp(Float value, Optional<Float> min, Optional<Float> max) {
        if (min.isPresent()) value = Math.max(value, min.get());
        if (max.isPresent()) value = Math.min(value, max.get());
        return value;
    }

}
