package dev.shadowsoffire.apothic_spawners.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

@Mixin(value = Items.class, remap = false)
public class ItemsMixin {

    @ModifyVariable(
        method = "registerItem(Lnet/minecraft/resources/ResourceKey;Ljava/util/function/Function;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;",
        at = @At("HEAD"),
        ordinal = 0,
        argsOnly = true)
    private static Function<Item.Properties, Item> apoth_replaceSpawnerFactory(Function<Item.Properties, Item> factory, ResourceKey<Item> key, Function<Item.Properties, Item> factory2, Item.Properties props) {
        if ("minecraft".equals(key.identifier().getNamespace()) && "spawner".equals(key.identifier().getPath())) {
            return p -> new ApothSpawnerItem(Blocks.SPAWNER, p);
        }
        return factory;
    }

}
