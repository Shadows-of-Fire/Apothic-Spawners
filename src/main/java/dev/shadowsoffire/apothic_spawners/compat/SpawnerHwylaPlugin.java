package dev.shadowsoffire.apothic_spawners.compat;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerBlock;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class SpawnerHwylaPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration reg) {
        reg.registerBlockDataProvider(new SpawnerServerDataProvider(), ApothSpawnerTile.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration reg) {
        reg.registerBlockComponent(new SpawnerClientProvider(), ApothSpawnerBlock.class);
    }

}
