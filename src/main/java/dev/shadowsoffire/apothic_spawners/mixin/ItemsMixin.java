package dev.shadowsoffire.apothic_spawners.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

@Mixin(value = Items.class, remap = false)
public class ItemsMixin {

    @Shadow
    public static Item registerBlock(BlockItem pItem) {
        throw new RuntimeException("registerBlock @Shadow failed.");
    }

    @Inject(at = @At("HEAD"), method = "registerBlock(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/item/Item;", cancellable = true)
    private static void apoth_replaceSpawnerItem(Block block, CallbackInfoReturnable<Item> cir) {
        if (block == Blocks.SPAWNER) {
            cir.setReturnValue(registerBlock(new ApothSpawnerItem(block, new Item.Properties())));
        }
    }

}
