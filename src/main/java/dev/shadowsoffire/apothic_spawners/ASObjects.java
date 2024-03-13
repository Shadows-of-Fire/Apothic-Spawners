package dev.shadowsoffire.apothic_spawners;

import java.util.function.Supplier;

import dev.shadowsoffire.apothic_spawners.advancements.ModifierTrigger;
import dev.shadowsoffire.apothic_spawners.advancements.SpawnEggItemPredicate;
import dev.shadowsoffire.apothic_spawners.enchantment.CapturingEnchant;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ASObjects {

    private static final DeferredHelper HELPER = DeferredHelper.create(ApothicSpawners.MODID);

    public static final Supplier<RecipeType<SpawnerModifier>> SPAWNER_MODIFIER = HELPER.recipe("spawner_modifier", () -> new RecipeType<SpawnerModifier>(){});

    public static final Supplier<SpawnerModifier.Serializer> SPAWNER_MODIFIER_SERIALIZER = HELPER.recipeSerializer("spawner_modifier", () -> SpawnerModifier.SERIALIZER);

    public static final Supplier<CapturingEnchant> CAPTURING = HELPER.enchant("capturing", () -> new CapturingEnchant());

    public static final Supplier<ModifierTrigger> MODIFIER_TRIGGER = HELPER.custom("spawner_modifier", Registries.TRIGGER_TYPE, ModifierTrigger::new);

    static {
        HELPER.custom("spawn_egg", NeoForgeRegistries.Keys.ITEM_PREDICATE_SERIALIZERS, () -> SpawnEggItemPredicate.CODEC);
    }

    public static void bootstrap(IEventBus bus) {
        bus.register(HELPER);
        SpawnerStats.bootstrap();
    }
}
