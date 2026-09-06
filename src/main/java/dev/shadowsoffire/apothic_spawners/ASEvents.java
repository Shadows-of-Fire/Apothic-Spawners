package dev.shadowsoffire.apothic_spawners;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dev.shadowsoffire.apothic_spawners.ASConfig.ConfigPayload;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.placebo.events.ResourceReloadEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ASEvents {

    private static final MethodHandle dropFromLootTable;
    static {
        Method m = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "dropFromLootTable", ServerLevel.class, DamageSource.class, boolean.class);
        try {
            m.setAccessible(true);
            dropFromLootTable = MethodHandles.lookup().unreflect(m);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("LivingEntity#dropFromLootTable not located!");
        }
    }

    @SubscribeEvent
    public void handleCapturing(LivingDropsEvent e) {
        Entity killer = e.getSource().getEntity();
        LivingEntity killed = e.getEntity();

        if (killer instanceof LivingEntity living) {
            var capturingPair = EnchantmentHelper.getHighestLevel(living.getWeaponItem(), ASObjects.CAPTURING);
            if (capturingPair == null || killed.is(ASObjects.BLACKLISTED_FROM_SPAWNERS)) {
                return;
            }

            if (killed.level().getRandom().nextFloat() < capturingPair.getSecond() * capturingPair.getFirst()) {
                SpawnEggItem.byId(killed.getType()).ifPresent(eggItem -> {
                    ItemStack egg = new ItemStack(eggItem);
                    e.getDrops().add(new ItemEntity(killed.level(), killed.getX(), killed.getY(), killed.getZ(), egg));
                });
            }
        }
    }

    @SubscribeEvent
    public void handleEchoing(LivingDropsEvent e) throws Throwable {
        int echoes = e.getEntity().getPersistentData().getIntOr(SpawnerStats.ECHOING.getId().toString(), 0);
        if (echoes > 0) {
            e.getEntity().captureDrops(new ArrayList<>());
            for (int i = 0; i < echoes; i++) {
                dropFromLootTable.invoke(e.getEntity(), (ServerLevel) e.getEntity().level(), e.getSource(), true);
            }
            e.getDrops().addAll(e.getEntity().captureDrops(null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void handleEchoingXp(LivingExperienceDropEvent e) {
        int echoes = e.getEntity().getPersistentData().getIntOr(SpawnerStats.ECHOING.getId().toString(), 0);
        if (echoes > 0) {
            e.setDroppedExperience(e.getDroppedExperience() * (1 + echoes));
        }
    }

@SubscribeEvent
@SuppressWarnings("deprecation")
public void handleUseItem(RightClickBlock e) {
    if (e.getLevel().getBlockEntity(e.getPos()) instanceof ApothSpawnerTile) {
        ItemStack s = e.getItemStack();
        if (s.getItem() instanceof SpawnEggItem) {
            EntityType<?> type = SpawnEggItem.getType(s);
            if (type != null && type.builtInRegistryHolder().is(ASObjects.BLACKLISTED_FROM_SPAWNERS)) {
                e.setCanceled(true);
            }
        }
    }
}

@SubscribeEvent
@SuppressWarnings("deprecation")
public void handleTooltips(ItemTooltipEvent e) {
    ItemStack s = e.getItemStack();
    if (s.getItem() instanceof SpawnEggItem) {
        EntityType<?> type = SpawnEggItem.getType(s);
        if (type != null && type.builtInRegistryHolder().is(ASObjects.BLACKLISTED_FROM_SPAWNERS)) {
            e.getToolTip().add(ApothicSpawners.lang("misc", "banned").withStyle(ChatFormatting.GRAY));
        }
    }
}

    @SubscribeEvent
    public void tickDumbMobs(EntityTickEvent.Pre e) {
        if (e.getEntity() instanceof Mob mob) {
            if (!mob.level().isClientSide() && mob.isNoAi() && mob.getPersistentData().getBooleanOr("apotheosis:movable", false)) {
                mob.setNoAi(false);
                mob.travel(new Vec3(mob.xxa, mob.zza, mob.yya));
                mob.setNoAi(true);
            }
        }
    }

    @SubscribeEvent
    public void dumbMobsCantTeleport(EntityTeleportEvent e) {
        if (e.getEntity().getPersistentData().getBooleanOr("apotheosis:movable", false)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void reload(ResourceReloadEvent e) {
        if (e.getSide().isServer()) {
            ASConfig.load();
        }
    }

    @SubscribeEvent
    public void sync(OnDatapackSyncEvent e) {
        e.sendRecipes(ASObjects.SPAWNER_MODIFIER.get());
        e.getRelevantPlayers().forEach(p -> {
            PacketDistributor.sendToPlayer(p, new ConfigPayload());
        });
    }

    @SubscribeEvent
    public void split(MobSplitEvent e) {
        if (e.getParent().isNoAi()) {
            boolean isMoveable = e.getParent().getPersistentData().getBooleanOr("apotheosis:movable", false);
            if (isMoveable) {
                e.getChildren().forEach(mob -> mob.getPersistentData().putBoolean("apotheosis:movable", true));
            }
        }
    }

    @SubscribeEvent
    public void onDespawn(MobDespawnEvent e) throws Throwable {
        Mob mob = e.getEntity();
        // Don't block peaceful despawns
        boolean isPeaceful = e.getLevel().getDifficulty() == Difficulty.PEACEFUL;
        if (isPeaceful && !mob.getType().isAllowedInPeaceful()) {
            return;
        }

        if (EntitySpawnReason.isSpawner(mob.getSpawnType()) && ASConfig.entityDespawnDelay >= mob.tickCount) {
            e.setResult(MobDespawnEvent.Result.DENY);
        }
    }
}
