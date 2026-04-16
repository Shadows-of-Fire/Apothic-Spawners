package dev.shadowsoffire.apothic_spawners.compat;

import org.joml.Matrix3x2fStack;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.modifiers.StatModifier;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class SpawnerCategory implements IRecipeCategory<SpawnerModifier> {

    public static final Identifier TEXTURES = ApothicSpawners.loc("textures/gui/spawner_jei.png");
    public static final Identifier UID = ApothicSpawners.loc("spawner_modifiers");
    public static final IRecipeType<SpawnerModifier> TYPE = IRecipeType.create(ApothicSpawners.MODID, "spawner_modifiers", SpawnerModifier.class);

    private IDrawable bg;
    private IDrawable icon;
    private Component title;

    public SpawnerCategory(IGuiHelper helper) {
        this.bg = helper.drawableBuilder(TEXTURES, 0, 0, 169, 75).build();
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.SPAWNER));
        this.title = ApothicSpawners.lang("title", "spawner");
    }

    @Override
    public IRecipeType<SpawnerModifier> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 169;
    }

    @Override
    public int getHeight() {
        return 75;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SpawnerModifier recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 11, 11).add(recipe.mainHand());
        if (recipe.offHand().isPresent()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 11, 48).add(recipe.offHand().get());
        }
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, -1000, -1000).add(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.SPAWNER));
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void draw(SpawnerModifier recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.bg.draw(gfx, 0, 0);
        if (recipe.offHand().isEmpty()) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 1, 31, 0, 88, 28, 34, 256, 256);
        }

        Font font = Minecraft.getInstance().font;

        if (mouseX >= -1 && mouseX < 9 && mouseY >= 13 && mouseY < 13 + 12) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, -1, 13, 0, 75, 10, 12, 256, 256);
        }
        else if (mouseX >= -1 && mouseX < 9 && mouseY >= 50 && mouseY < 50 + 12 && recipe.offHand().isPresent()) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, -1, 50, 0, 75, 10, 12, 256, 256);
        }

        Matrix3x2fStack mvStack = gfx.pose();
        mvStack.pushMatrix();
        mvStack.translate(0, 0.5f);
        gfx.fakeItem(new ItemStack(Items.SPAWNER), 31, 29);
        mvStack.popMatrix();

        int top = 75 / 2 - recipe.statModifiers().size() * (font.lineHeight + 2) / 2 + 2;
        int left = 168;
        for (StatModifier<?> s : recipe.statModifiers()) {
            String value = s.getFormattedValue();
            Component msg = switch (s.mode()) {
                case ADD -> {
                    if ("true".equals(value)) value = "+";
                    else if ("false".equals(value)) value = "-";
                    else if (s.value() instanceof Number num && num.intValue() > 0) value = "+" + value;
                    yield ApothicSpawners.lang("misc", "concat", value, s.stat().name());
                }
                case SET -> {
                    if (s.value() instanceof Number) {
                        yield ApothicSpawners.lang("misc", "value_concat", s.stat().name(), value);
                    }
                    else {
                        if ("true".equals(value)) {
                            yield ApothicSpawners.lang("misc", "on", s.stat().name());
                        }
                        else {
                            yield ApothicSpawners.lang("misc", "off", s.stat().name());
                        }
                    }
                }
            };

            int width = font.width(msg);
            boolean hover = mouseX >= left - width && mouseX < left && mouseY >= top && mouseY < top + font.lineHeight + 1;
            gfx.text(font, msg, left - font.width(msg), top, hover ? 0xFF8080FF : 0xFF333333, false);
            top += font.lineHeight + 2;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getTooltip(ITooltipBuilder tooltip, SpawnerModifier recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= -1 && mouseX < 9 && mouseY >= 13 && mouseY < 13 + 12) {
            tooltip.add(ApothicSpawners.lang("misc", "mainhand"));
            return;
        }

        if (mouseX >= -1 && mouseX < 9 && mouseY >= 50 && mouseY < 50 + 12 && recipe.offHand().isPresent()) {
            tooltip.add(ApothicSpawners.lang("misc", "offhand"));
            if (!recipe.consumesOffhand()) {
                tooltip.add(ApothicSpawners.lang("misc", "not_consumed").withStyle(ChatFormatting.GRAY));
            }
            return;
        }

        if (mouseX >= 33 && mouseX < 33 + 16 && mouseY >= 30 && mouseY < 30 + 16) {
            tooltip.add(ApothicSpawners.lang("misc", "rclick_spawner"));
            return;
        }

        Font font = Minecraft.getInstance().font;
        int top = 75 / 2 - recipe.statModifiers().size() * (font.lineHeight + 2) / 2 + 2;
        int left = 168;
        for (StatModifier<?> s : recipe.statModifiers()) {
            Component msg = getStatMessage(s);
            int width = font.width(msg);
            if (mouseX >= left - width && mouseX < left && mouseY >= top && mouseY < top + font.lineHeight + 1) {
                tooltip.add(s.stat().name().withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE));
                tooltip.add(s.stat().desc().withStyle(ChatFormatting.GRAY));
                if (s.value() instanceof Number && s.mode() == StatModifier.Mode.ADD) {
                    StatModifier<Number> n = (StatModifier<Number>) s;
                    if (s.min().isPresent() || s.max().isPresent()) tooltip.add(Component.literal(" "));
                    if (s.min().isPresent()) tooltip.add(ApothicSpawners.lang("misc", "min_value", n.stat().formatValue(n.min().get())).withStyle(ChatFormatting.GRAY));
                    if (s.max().isPresent()) tooltip.add(ApothicSpawners.lang("misc", "max_value", n.stat().formatValue(n.max().get())).withStyle(ChatFormatting.GRAY));
                }
                return;
            }
            top += font.lineHeight + 2;
        }
    }

    private static Component getStatMessage(StatModifier<?> s) {
        String value = s.getFormattedValue();
        return switch (s.mode()) {
            case ADD -> {
                if ("true".equals(value)) value = "+";
                else if ("false".equals(value)) value = "-";
                else if (s.value() instanceof Number num && num.intValue() > 0) value = "+" + value;
                yield ApothicSpawners.lang("misc", "concat", value, s.stat().name());
            }
            case SET -> {
                if (s.value() instanceof Number) {
                    yield ApothicSpawners.lang("misc", "value_concat", s.stat().name(), value);
                }
                else {
                    if ("true".equals(value)) {
                        yield ApothicSpawners.lang("misc", "on", s.stat().name());
                    }
                    else {
                        yield ApothicSpawners.lang("misc", "off", s.stat().name());
                    }
                }
            }
        };
    }

}
