package dev.shadowsoffire.apothic_spawners.advancements;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicate;

/**
 * Custom predicate which attempts to check if an item is a spawn egg or not.
 * <p>
 * Since we're relegated to components, this is the best we can do.
 */
public class SpawnEggItemPredicate implements DataComponentPredicate {

    public static final MapCodec<SpawnEggItemPredicate> CODEC = MapCodec.unit(SpawnEggItemPredicate::new);

    @Override
    public boolean matches(DataComponentGetter components) {
        return components.has(DataComponents.ENTITY_DATA);
    }

}
