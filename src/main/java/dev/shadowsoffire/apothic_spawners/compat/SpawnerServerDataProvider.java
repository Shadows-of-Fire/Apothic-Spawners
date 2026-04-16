package dev.shadowsoffire.apothic_spawners.compat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class SpawnerServerDataProvider implements IServerDataProvider<BlockAccessor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnerServerDataProvider.class);

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor access) {
        if (access.getBlockEntity() instanceof ApothSpawnerTile spw) {
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(spw.problemPath(), LOGGER)) {
                TagValueOutput output = TagValueOutput.createWithContext(reporter, access.getLevel().registryAccess());
                spw.saveCustomOnly(output);
                CompoundTag result = output.buildResult();
                result.keySet().forEach(key -> tag.put(key, result.get(key)));
            }
        }
    }

    @Override
    public Identifier getUid() {
        return ApothicSpawners.loc("spawner");
    }

}
