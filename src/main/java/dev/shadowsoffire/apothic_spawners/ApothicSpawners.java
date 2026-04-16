package dev.shadowsoffire.apothic_spawners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apothic_spawners.ASConfig.ConfigPayload;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.data.ASEnchantmentProvider;
import dev.shadowsoffire.apothic_spawners.data.ASRecipeProvider;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.placebo.datagen.DataGenBuilder;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@Mod(ApothicSpawners.MODID)
public class ApothicSpawners {

    public static final String MODID = "apothic_spawners";
    public static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis : Spawner");

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

    @SubscribeEvent
    public void data(GatherDataEvent.Client event) {
        DataProvider.INDENT_WIDTH.set(4);
        DataGenBuilder.create(MODID)
            .registry(Registries.ENCHANTMENT, ASEnchantmentProvider::bootstrap)
            .provider(ASRecipeProvider::new)
            .build(event);
    }

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static MutableComponent lang(String type, String path, Object... args) {
        return Component.translatable(type + "." + MODID + "." + path, args);
    }

}
