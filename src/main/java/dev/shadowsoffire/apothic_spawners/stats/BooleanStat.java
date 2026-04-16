package dev.shadowsoffire.apothic_spawners.stats;

import java.util.Optional;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class BooleanStat extends CustomStat<Boolean> {

    public BooleanStat(Boolean defaultValue) {
        super(defaultValue);
    }

    @Override
    public Codec<Boolean> valueCodec() {
        return Codec.BOOL;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Boolean> valueStreamCodec() {
        return ByteBufCodecs.BOOL;
    }

    @Override
    public Component getTooltipImpl(ApothSpawnerTile spawner) {
        return this.name().withStyle(ChatFormatting.DARK_GREEN);
    }

    @Override
    public boolean applyModifier(ApothSpawnerTile spawner, Boolean value, Optional<Boolean> min, Optional<Boolean> max) {
        boolean old = this.getValue(spawner);
        this.setValue(spawner, value);
        return old != this.getValue(spawner);
    }

}
