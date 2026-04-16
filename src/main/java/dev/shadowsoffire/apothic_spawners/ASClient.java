package dev.shadowsoffire.apothic_spawners;

import dev.shadowsoffire.apothic_spawners.compat.SpawnerRecipeCache;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = ApothicSpawners.MODID, value = Dist.CLIENT)
public class ASClient {

    public static void init() {
        NeoForge.EVENT_BUS.register(ASClient.class);
    }

    @SubscribeEvent
    public static void recipesReceived(RecipesReceivedEvent e) {
        if (e.getRecipeTypes().contains(ASObjects.SPAWNER_MODIFIER.get())) {
            SpawnerRecipeCache.rebuildFromMap(e.getRecipeMap());
        }
    }

}
