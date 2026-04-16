package dev.shadowsoffire.apothic_spawners.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerItem;
import net.minecraft.world.item.ItemStack;

/**
 * Prevents vanilla's {@code Spawner.appendHoverText} from firing for our custom spawner item.
 * <p>
 * In {@code ItemStack.addDetailsToTooltip}, vanilla checks {@code this.is(Items.SPAWNER)} before
 * calling {@code Spawner.appendHoverText}. We wrap that {@code is()} call to return false
 * when the item is an {@link ApothSpawnerItem}, so the vanilla tooltip block is skipped entirely.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin {

    @WrapOperation(
        method = {"addDetailsToTooltip", "getTooltipLines"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z",
            ordinal = 0,
            remap = false),
        remap = false,
        require = 1)
    private boolean apoth_skipVanillaSpawnerTooltip(ItemStack self, Object predicate, Operation<Boolean> original) {
        if (self.getItem() instanceof ApothSpawnerItem) {
            return false;
        }
        return original.call(self, predicate);
    }

}
