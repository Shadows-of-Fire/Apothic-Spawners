package dev.shadowsoffire.apothic_spawners.block;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data.contains("SpawnData")) {
                try {
                    String name = data.copyTag().getCompound("SpawnData").getCompound("entity").getString("id");
                    EntityType<?> t = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(name));
                    if (t == null || t == EntityType.PIG && !"minecraft:pig".equals(name)) {
                        return super.getName(stack);
                    }
                    MobCategory cat = t.getCategory();
                    ChatFormatting color = switch (cat) {
                        case AMBIENT, CREATURE -> ChatFormatting.DARK_GREEN;
                        case MONSTER -> ChatFormatting.RED;
                        case WATER_AMBIENT, UNDERGROUND_WATER_CREATURE, WATER_CREATURE, AXOLOTLS -> ChatFormatting.BLUE;
                        case MISC -> ChatFormatting.WHITE;
                    };
                    return ApothicSpawners.lang("item", "spawner", Component.translatable(t.getDescriptionId())).withStyle(color);
                }
                catch (Exception ex) {
                    super.getName(stack);
                }
            }
        }
        return super.getName(stack);
    }

}
