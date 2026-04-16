package dev.shadowsoffire.apothic_spawners.compat;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

public class SpawnerRecipeCache {

    private static volatile List<SpawnerModifier> RECIPES = List.of();

    private SpawnerRecipeCache() {}

    public static void rebuildFromMap(RecipeMap map) {
        Collection<RecipeHolder<SpawnerModifier>> holders = map.byType(ASObjects.SPAWNER_MODIFIER.get());
        RECIPES = holders.stream().sorted(Comparator.comparing(h -> h.id().identifier(), (id1, id2) -> -id1.compareNamespaced(id2))).map(RecipeHolder::value).toList();
    }

    public static List<SpawnerModifier> getRecipes() {
        return RECIPES;
    }

}
