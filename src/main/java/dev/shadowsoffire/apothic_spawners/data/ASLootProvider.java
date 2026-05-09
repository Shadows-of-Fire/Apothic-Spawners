package dev.shadowsoffire.apothic_spawners.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import dev.shadowsoffire.apothic_spawners.ASObjects;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class ASLootProvider extends LootTableProvider {

    private ASLootProvider(PackOutput output, Set<ResourceKey<LootTable>> requiredTables, List<SubProviderEntry> subProviders, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, requiredTables, subProviders, registries);
    }

    public static ASLootProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new ASLootProvider(output, Set.of(ASObjects.UNSTABLE_SPAWNER_LOOT),
            List.of(new SubProviderEntry(GameplayLoot::new, LootContextParamSets.CHEST)), registries);
    }

    public static record GameplayLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
            output.accept(ASObjects.UNSTABLE_SPAWNER_LOOT,
                LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(48, 64))
                        .add(LootItem.lootTableItem(Items.IRON_CHAIN))));
        }
    }
}
