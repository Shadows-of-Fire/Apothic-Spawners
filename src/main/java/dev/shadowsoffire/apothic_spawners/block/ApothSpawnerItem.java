package dev.shadowsoffire.apothic_spawners.block;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ApothSpawnerItem extends BlockItem {

    public ApothSpawnerItem(Block block, Item.Properties props) {
        super(block, props);
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {
        return ApothicSpawners.MODID;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("BlockEntityTag")) {
            CompoundTag tag = stack.getTag().getCompound("BlockEntityTag");
            if (tag.contains("SpawnData")) {
                try {
                    String name = tag.getCompound("SpawnData").getCompound("entity").getString("id");
                    EntityType<?> t = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(name));
                    MobCategory cat = t.getCategory();
                    ChatFormatting color = switch (cat) {
                        case AMBIENT, CREATURE -> ChatFormatting.DARK_GREEN;
                        case MONSTER -> ChatFormatting.RED;
                        case WATER_AMBIENT, UNDERGROUND_WATER_CREATURE, WATER_CREATURE, AXOLOTLS -> ChatFormatting.BLUE;
                        case MISC -> ChatFormatting.WHITE;
                    };
                    return Component.translatable("item.apotheosis.spawner", Component.translatable(t.getDescriptionId())).withStyle(color);
                }
                catch (Exception ex) {
                    super.getName(stack);
                }
            }
        }
        return super.getName(stack);
    }

}
