package dev.shadowsoffire.apothic_spawners.data;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.modifiers.StatModifier;
import dev.shadowsoffire.apothic_spawners.modifiers.StatModifier.Mode;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.placebo.datagen.LegacyRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class ASRecipeProvider extends LegacyRecipeProvider {

    public ASRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ApothicSpawners.MODID);
    }

    @Override
    public String getName() {
        return "Apothic Spawners Recipes";
    }

    @Override
    protected void genRecipes(RecipeOutput recipeOutput, HolderLookup.Provider registries) {
        // Forward modifiers
        addModifier("spawner_modifiers/min_delay", Items.SUGAR, intChange(SpawnerStats.MIN_DELAY, -10, 20, null));
        addModifier("spawner_modifiers/max_delay", Items.CLOCK, intChange(SpawnerStats.MAX_DELAY, -20, 20, null));
        addModifier("spawner_modifiers/spawn_count", Items.FERMENTED_SPIDER_EYE, intChange(SpawnerStats.SPAWN_COUNT, 2, null, 16));
        addModifier("spawner_modifiers/max_nearby", Items.GHAST_TEAR, intChange(SpawnerStats.MAX_NEARBY_ENTITIES, 2, null, 32));
        addModifier("spawner_modifiers/player_range", Items.PRISMARINE_CRYSTALS, intChange(SpawnerStats.REQ_PLAYER_RANGE, 4, null, 48));
        addModifier("spawner_modifiers/spawn_range", Items.PISTON, intChange(SpawnerStats.SPAWN_RANGE, 2, null, 32));
        addModifier("spawner_modifiers/initial_health", Items.POINTED_DRIPSTONE, floatChange(SpawnerStats.INITIAL_HEALTH, -0.05F, 0.20F, null));
        addModifier("spawner_modifiers/ignore_players", Items.NETHER_STAR, boolSet(SpawnerStats.IGNORE_PLAYERS, true));
        addModifier("spawner_modifiers/ignore_conditions", Items.CONDUIT, boolSet(SpawnerStats.IGNORE_CONDITIONS, true));
        addModifier("spawner_modifiers/redstone_control", Items.COMPARATOR, boolSet(SpawnerStats.REDSTONE_CONTROL, true));
        addModifier("spawner_modifiers/ignore_light", Items.SOUL_LANTERN, boolSet(SpawnerStats.IGNORE_LIGHT, true));
        addModifier("spawner_modifiers/no_ai", Items.CHORUS_FRUIT, boolSet(SpawnerStats.NO_AI, true));
        addModifier("spawner_modifiers/silent", ItemTags.WOOL, boolSet(SpawnerStats.SILENT, true));
        addModifier("spawner_modifiers/youthful", Items.TURTLE_EGG, boolSet(SpawnerStats.YOUTHFUL, true));
        addModifier("spawner_modifiers/burning", Items.CAMPFIRE, boolSet(SpawnerStats.BURNING, true));
        addModifier("spawner_modifiers/echoing", Items.ECHO_SHARD, intChange(SpawnerStats.ECHOING, 1, null, 3));

        // Inverse modifiers (mainhand + quartz offhand, not consumed)
        addInverse("spawner_modifiers/_inverse/min_delay", Items.SUGAR, intChange(SpawnerStats.MIN_DELAY, 10, null, 1600));
        addInverse("spawner_modifiers/_inverse/max_delay", Items.CLOCK, intChange(SpawnerStats.MAX_DELAY, 20, null, 1600));
        addInverse("spawner_modifiers/_inverse/spawn_count", Items.FERMENTED_SPIDER_EYE, intChange(SpawnerStats.SPAWN_COUNT, -2, 1, null));
        addInverse("spawner_modifiers/_inverse/max_nearby", Items.GHAST_TEAR, intChange(SpawnerStats.MAX_NEARBY_ENTITIES, -2, 1, null));
        addInverse("spawner_modifiers/_inverse/player_range", Items.PRISMARINE_CRYSTALS, intChange(SpawnerStats.REQ_PLAYER_RANGE, -4, 1, null));
        addInverse("spawner_modifiers/_inverse/spawn_range", Items.PISTON, intChange(SpawnerStats.SPAWN_RANGE, -2, 1, null));
        addInverse("spawner_modifiers/_inverse/initial_health", Items.POINTED_DRIPSTONE, floatChange(SpawnerStats.INITIAL_HEALTH, 0.05F, null, 1F));
        addInverse("spawner_modifiers/_inverse/ignore_players", Items.NETHER_STAR, boolSet(SpawnerStats.IGNORE_PLAYERS, false));
        addInverse("spawner_modifiers/_inverse/ignore_conditions", Items.CONDUIT, boolSet(SpawnerStats.IGNORE_CONDITIONS, false));
        addInverse("spawner_modifiers/_inverse/redstone_control", Items.COMPARATOR, boolSet(SpawnerStats.REDSTONE_CONTROL, false));
        addInverse("spawner_modifiers/_inverse/ignore_light", Items.SOUL_LANTERN, boolSet(SpawnerStats.IGNORE_LIGHT, false));
        addInverse("spawner_modifiers/_inverse/no_ai", Items.CHORUS_FRUIT, boolSet(SpawnerStats.NO_AI, false));
        addInverse("spawner_modifiers/_inverse/silent", ItemTags.WOOL, boolSet(SpawnerStats.SILENT, false));
        addInverse("spawner_modifiers/_inverse/youthful", Items.TURTLE_EGG, boolSet(SpawnerStats.YOUTHFUL, false));
        addInverse("spawner_modifiers/_inverse/burning", Items.CAMPFIRE, boolSet(SpawnerStats.BURNING, false));
        addInverse("spawner_modifiers/_inverse/echoing", Items.ECHO_SHARD, intChange(SpawnerStats.ECHOING, -1, 0, null));
    }

    private void addModifier(String path, Object mainhand, StatModifier<?> change) {
        Ingredient main = createInput(false, mainhand).get(0);
        SpawnerModifier recipe = new SpawnerModifier(main, Optional.empty(), false, List.of(change));
        emitRecipe(path, recipe);
    }

    private void addInverse(String path, Object mainhand, StatModifier<?> change) {
        Ingredient main = createInput(false, mainhand).get(0);
        Ingredient offhand = createInput(false, Items.QUARTZ).get(0);
        SpawnerModifier recipe = new SpawnerModifier(main, Optional.of(offhand), false, List.of(change));
        emitRecipe(path, recipe);
    }

    private void emitRecipe(String path, Recipe<?> recipe) {
        Identifier id = Identifier.fromNamespaceAndPath(ApothicSpawners.MODID, path);
        this.recipeOutput.accept(ResourceKey.create(Registries.RECIPE, id), recipe, null);
    }

    private static StatModifier<Integer> intChange(SpawnerStat<Integer> stat, int value, Integer min, Integer max) {
        return new StatModifier<>(stat, value, Optional.ofNullable(min), Optional.ofNullable(max), Mode.ADD);
    }

    private static StatModifier<Float> floatChange(SpawnerStat<Float> stat, float value, Float min, Float max) {
        return new StatModifier<>(stat, value, Optional.ofNullable(min), Optional.ofNullable(max), Mode.ADD);
    }

    private static StatModifier<Boolean> boolSet(SpawnerStat<Boolean> stat, boolean value) {
        return new StatModifier<>(stat, value, Optional.empty(), Optional.empty(), Mode.SET);
    }

}
