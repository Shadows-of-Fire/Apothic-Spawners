package dev.shadowsoffire.apothic_spawners;

import java.util.function.Supplier;

import dev.shadowsoffire.apothic_spawners.advancements.ModifierTrigger;
import dev.shadowsoffire.apothic_spawners.advancements.SpawnEggItemPredicate;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;

public class ASObjects {

    private static final DeferredHelper HELPER = DeferredHelper.create(ApothicSpawners.MODID);

    public static final Supplier<RecipeType<SpawnerModifier>> SPAWNER_MODIFIER = HELPER.recipe("spawner_modifier", () -> new RecipeType<SpawnerModifier>(){});

    public static final Supplier<RecipeSerializer<SpawnerModifier>> SPAWNER_MODIFIER_SERIALIZER = HELPER.recipeSerializer("spawner_modifier", () -> SpawnerModifier.SERIALIZER);

    public static final DataComponentType<Float> CAPTURING = HELPER.enchantmentEffect("capturing", builder -> builder.persistent(Codec.floatRange(0.001F, 1F)));

    public static final ResourceKey<Enchantment> CAPTURING_ENCH = ResourceKey.create(Registries.ENCHANTMENT, ApothicSpawners.loc("capturing"));

    public static final ModifierTrigger MODIFIER_TRIGGER = HELPER.custom("spawner_modifier", Registries.TRIGGER_TYPE, new ModifierTrigger());

    public static final TagKey<EntityType<?>> BLACKLISTED_FROM_SPAWNERS = TagKey.create(Registries.ENTITY_TYPE, ApothicSpawners.loc("blacklisted_from_spawners"));

    /**
     * Loot table rolled when an unstable spawner detonates. Apothic Spawners' default version produces iron chains;
     * Apotheosis ships a same-path override that swaps in Spawner Chains when both mods are present.
     */
    public static final ResourceKey<LootTable> UNSTABLE_SPAWNER_LOOT = ResourceKey.create(Registries.LOOT_TABLE, ApothicSpawners.loc("gameplay/unstable_spawner"));

    static {
        HELPER.componentPredicate("spawn_egg", SpawnEggItemPredicate.CODEC.codec());
    }

    public static void bootstrap(IEventBus bus) {
        bus.register(HELPER);
        SpawnerStats.bootstrap();
    }
}
