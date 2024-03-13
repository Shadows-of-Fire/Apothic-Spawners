package dev.shadowsoffire.apothic_spawners.advancements;

import com.mojang.serialization.Codec;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;

public class SpawnEggItemPredicate implements ICustomItemPredicate {

    public static final Codec<SpawnEggItemPredicate> CODEC = Codec.unit(SpawnEggItemPredicate::new);

    @Override
    public boolean test(ItemStack t) {
        return t.getItem() instanceof SpawnEggItem;
    }

    @Override
    public Codec<? extends ICustomItemPredicate> codec() {
        return CODEC;
    }

}
