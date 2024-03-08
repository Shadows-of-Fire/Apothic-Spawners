package dev.shadowsoffire.apothic_spawners.modifiers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Parent class for all spawner modifiers.
 *
 * @author Shadows
 */
public class SpawnerModifier implements Recipe<Container> {

    public static final Codec<SpawnerModifier> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Ingredient.CODEC_NONEMPTY.fieldOf("mainhand").forGetter(SpawnerModifier::getMainhandInput),
            ExtraCodecs.strictOptionalField(Ingredient.CODEC_NONEMPTY, "offhand", Ingredient.EMPTY).forGetter(SpawnerModifier::getOffhandInput),
            ExtraCodecs.strictOptionalField(Codec.BOOL, "consumes_offhand", false).forGetter(SpawnerModifier::consumesOffhand),
            StatModifier.CODEC.listOf().fieldOf("stat_changes").forGetter(SpawnerModifier::getStatModifiers))
        .apply(inst, SpawnerModifier::new));

    public static final Serializer SERIALIZER = new Serializer();

    protected final Ingredient mainHand, offHand;
    protected final boolean consumesOffhand;
    protected final List<StatModifier<?>> statChanges;

    public SpawnerModifier(Ingredient mainHand, Ingredient offHand, boolean consumesOffhand, List<StatModifier<?>> stats) {
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.consumesOffhand = consumesOffhand;
        this.statChanges = ImmutableList.copyOf(stats);
    }

    /**
     * Tests if this modifier matches the held items.
     *
     * @return If this modifier matches the given items.
     */
    public boolean matches(ApothSpawnerTile tile, ItemStack mainhand, ItemStack offhand) {
        if (this.mainHand.test(mainhand)) {
            if (this.offHand == Ingredient.EMPTY) return true;
            return this.offHand.test(offhand);
        }
        return false;
    }

    /**
     * Applies this modifier.
     *
     * @return If any part of the modification was successful, and items should be consumed.
     */
    public boolean apply(ApothSpawnerTile tile) {
        boolean success = false;
        for (StatModifier<?> m : this.statChanges) {
            if (m.apply(tile)) {
                success = true;
                tile.setChanged();
            }
        }
        return success;
    }

    public boolean consumesOffhand() {
        return this.consumesOffhand;
    }

    public Ingredient getMainhandInput() {
        return this.mainHand;
    }

    public Ingredient getOffhandInput() {
        return this.offHand;
    }

    public List<StatModifier<?>> getStatModifiers() {
        return this.statChanges;
    }

    @Override
    @Deprecated
    public boolean matches(Container pContainer, Level pLevel) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack assemble(Container pContainer, RegistryAccess regs) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack getResultItem(RegistryAccess regs) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SpawnerModifier.SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ASObjects.SPAWNER_MODIFIER.get();
    }

    @Nullable
    public static SpawnerModifier findMatch(ApothSpawnerTile tile, ItemStack mainhand, ItemStack offhand) {
        return tile.getLevel().getRecipeManager().getAllRecipesFor(ASObjects.SPAWNER_MODIFIER.get())
            .stream()
            .map(RecipeHolder::value)
            .sorted((r1, r2) -> r1.offHand == Ingredient.EMPTY ? r2.offHand == Ingredient.EMPTY ? 0 : 1 : -1)
            .filter(r -> r.matches(tile, mainhand, offhand))
            .findFirst()
            .orElse(null);
    }

    public static class Serializer implements RecipeSerializer<SpawnerModifier> {

        @Override
        public Codec<SpawnerModifier> codec() {
            return CODEC;
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public SpawnerModifier fromNetwork(FriendlyByteBuf buf) {
            Ingredient mainhand = Ingredient.fromNetwork(buf);
            Ingredient offhand = buf.readBoolean() ? Ingredient.fromNetwork(buf) : Ingredient.EMPTY;
            boolean consumesOffhand = buf.readBoolean();
            List<StatModifier<?>> statChanges = new ArrayList<>();
            int size = buf.readByte();
            for (int i = 0; i < size; i++) {
                statChanges.add(StatModifier.read(buf));
            }
            return new SpawnerModifier(mainhand, offhand, consumesOffhand, statChanges);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SpawnerModifier recipe) {
            recipe.mainHand.toNetwork(buf);
            buf.writeBoolean(recipe.offHand != Ingredient.EMPTY);
            if (recipe.offHand != Ingredient.EMPTY) recipe.offHand.toNetwork(buf);
            buf.writeBoolean(recipe.consumesOffhand);
            buf.writeByte(recipe.statChanges.size());
            recipe.statChanges.forEach(m -> m.write(buf));
        }

    }
}
