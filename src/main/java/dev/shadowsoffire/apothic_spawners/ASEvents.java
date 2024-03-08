package dev.shadowsoffire.apothic_spawners;

import dev.shadowsoffire.apothic_spawners.ASConfig.ConfigPayload;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.placebo.events.ResourceReloadEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.network.PacketDistributor;

public class ASEvents {

    @SubscribeEvent
    public void dropsEvent(LivingDropsEvent e) {
        ASObjects.CAPTURING.get().handleCapturing(e);
    }

    @SubscribeEvent
    public void handleUseItem(RightClickBlock e) {
        if (e.getLevel().getBlockEntity(e.getPos()) instanceof ApothSpawnerTile) {
            ItemStack s = e.getItemStack();
            if (s.getItem() instanceof SpawnEggItem egg) {
                EntityType<?> type = egg.getType(s.getTag());
                if (ASConfig.bannedMobs.contains(EntityType.getKey(type))) e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void handleTooltips(ItemTooltipEvent e) {
        ItemStack s = e.getItemStack();
        if (s.getItem() instanceof SpawnEggItem egg) {
            EntityType<?> type = egg.getType(s.getTag());
            if (ASConfig.bannedMobs.contains(EntityType.getKey(type))) e.getToolTip().add(Component.translatable("misc.apotheosis.banned").withStyle(ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public void tickDumbMobs(LivingTickEvent e) {
        if (e.getEntity() instanceof Mob mob) {
            if (!mob.level().isClientSide && mob.isNoAi() && mob.getPersistentData().getBoolean("apotheosis:movable")) {
                mob.setNoAi(false);
                mob.travel(new Vec3(mob.xxa, mob.zza, mob.yya));
                mob.setNoAi(true);
            }
        }
    }

    @SubscribeEvent
    public void dumbMobsCantTeleport(EntityTeleportEvent e) {
        if (e.getEntity().getPersistentData().getBoolean("apotheosis:movable")) {
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
        if (e.getPlayer() != null) {
            PacketDistributor.PLAYER.with(e.getPlayer()).send(new ConfigPayload());
        }
        else {
            PacketDistributor.ALL.noArg().send(new ConfigPayload());
        }
    }
}
