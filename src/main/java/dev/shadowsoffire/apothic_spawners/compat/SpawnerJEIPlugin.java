package dev.shadowsoffire.apothic_spawners.compat;

import java.util.List;

import dev.shadowsoffire.apothic_spawners.ASConfig;
import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class SpawnerJEIPlugin implements IModPlugin {

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        List<SpawnerModifier> recipes = SpawnerRecipeCache.getRecipes();

        reg.addRecipes(SpawnerCategory.TYPE, recipes);

        if (ASConfig.spawnerSilkLevel == -1) {
            reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM_STACK, ApothicSpawners.lang("info", "spawner.no_silk"));
        }
        else if (ASConfig.spawnerSilkLevel == 0) {
            reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM_STACK, ApothicSpawners.lang("info", "always_drop"));
        }
        else {
            Minecraft.getInstance().level.holder(Enchantments.SILK_TOUCH).ifPresent(silk -> {
                reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM_STACK,
                    ApothicSpawners.lang("info", "spawner", ((MutableComponent) Enchantment.getFullname(silk, ASConfig.spawnerSilkLevel)).withStyle(ChatFormatting.DARK_BLUE)));
            });
        }

        Minecraft.getInstance().level.holder(ASObjects.CAPTURING_ENCH).ifPresent(capturing -> {
            Float dropChance = capturing.value().effects().get(ASObjects.CAPTURING);
            if (dropChance == null) dropChance = 0.005F;
            for (Item i : BuiltInRegistries.ITEM) {
                if (i instanceof SpawnEggItem) {
                    reg.addIngredientInfo(new ItemStack(i), VanillaTypes.ITEM_STACK, ApothicSpawners.lang("info", "capturing", ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(dropChance * 100)));
                }
            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addCraftingStation(SpawnerCategory.TYPE, Blocks.SPAWNER);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new SpawnerCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public Identifier getPluginUid() {
        return ApothicSpawners.loc("spawner");
    }

}
