package dev.shadowsoffire.apothic_spawners.compat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class SpawnerClientProvider implements IBlockComponentProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnerClientProvider.class);

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (Minecraft.getInstance().hasControlDown()) {
            ApothSpawnerTile tile = new ApothSpawnerTile(BlockPos.ZERO, Blocks.SPAWNER.defaultBlockState());
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
                var input = TagValueInput.create(reporter, accessor.getLevel().registryAccess(), accessor.getServerData());
                tile.loadCustomOnly(input);
            }
            SpawnerStats.generateTooltip(tile, tooltip::add);
        }
        else {
            tooltip.add(ApothicSpawners.lang("misc", "ctrl_stats"));
        }
    }

    @Override
    public Identifier getUid() {
        return ApothicSpawners.loc("spawner");
    }

}
