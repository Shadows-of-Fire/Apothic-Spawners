package dev.shadowsoffire.apothic_spawners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.shadowsoffire.apothic_spawners.ASConfig.ConfigPayload;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@Mod(ApothicSpawners.MODID)
public class ApothicSpawners {

    public static final String MODID = "apothic_spawners";
    public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Spawner");

    public ApothicSpawners(IEventBus bus) {
        bus.register(this);
        ASObjects.bootstrap(bus);
        NeoForge.EVENT_BUS.register(new ASEvents());
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            ObfuscationReflectionHelper.<BlockEntityType<?>, BlockEntityType.BlockEntitySupplier<?>>setPrivateValue(BlockEntityType.class, BlockEntityType.MOB_SPAWNER, ApothSpawnerTile::new, "factory");
            ASConfig.load();
            TabFillingRegistry.registerSimple(Items.SPAWNER, CreativeModeTabs.TOOLS_AND_UTILITIES);
            PayloadHelper.registerPayload(new ConfigPayload.Provider());
        });
    }

    @SubscribeEvent
    public void regs(NewRegistryEvent e) {
        e.register(SpawnerStats.REGISTRY);
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }

}
