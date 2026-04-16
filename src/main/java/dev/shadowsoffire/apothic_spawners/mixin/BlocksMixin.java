package dev.shadowsoffire.apothic_spawners.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Mixin(value = Blocks.class, remap = false)
public class BlocksMixin {

    @ModifyArg(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Blocks;register(Ljava/lang/String;Ljava/util/function/Function;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;"),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=spawner"),
            to = @At(value = "CONSTANT", args = "stringValue=creaking_heart")),
        index = 1,
        require = 1)
    private static Function<BlockBehaviour.Properties, Block> apoth_overrideSpawnerBlock(Function<BlockBehaviour.Properties, Block> original) {
        return ApothSpawnerBlock::new;
    }

}
