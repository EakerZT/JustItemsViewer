package eakerzt.jiv.library.render;

import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Ingredient-independent slot display metadata. No recipe execution semantics. */
public record RecipeSlotDecorations(boolean nonConsumed, boolean showChance, double chance) {
    private static final Identifier NON_CONSUMED_TEXTURE = Identifier.fromNamespaceAndPath("jiv", "textures/gui/non_consumed.png");
    public static final RecipeSlotDecorations NONE = new RecipeSlotDecorations(false,false,1);

    public RecipeSlotDecorations { validateChance(chance); }

    public static void validateChance(double chance) {
        if (!Double.isFinite(chance) || chance < 0 || chance > 1)
            throw new IllegalArgumentException("chance must be finite and between 0 and 1");
    }

    public static String exactPercent(double chance) {
        validateChance(chance);
        return BigDecimal.valueOf(chance).movePointRight(2).stripTrailingZeros().toPlainString();
    }

    public static String compactPercent(double chance) {
        validateChance(chance);
        if (chance > 0 && chance < 0.0001) return "<0.01%";
        if (chance < 1 && chance > 0.9999) return ">99.99%";
        return BigDecimal.valueOf(chance).movePointRight(2).setScale(2,RoundingMode.HALF_UP)
            .stripTrailingZeros().toPlainString()+"%";
    }

    public void addTooltip(ITooltipBuilder tooltip) {
        if (nonConsumed) tooltip.add(Component.translatable("jiv.tooltip.recipe.not_consumed"));
        if (showChance && chance < 1) tooltip.add(Component.translatable("jiv.tooltip.recipe.chance",exactPercent(chance)));
    }

    public void draw(GuiGraphicsExtractor graphics,int x,int y,int width,int height) {
        if (nonConsumed) drawNonConsumed(graphics,x,y,width,height);
        // Keep the probability inside the slot, aligned to its top-right corner.
        if (showChance && chance < 1) drawLabel(graphics,compactPercent(chance),x,y,width,Math.min(height,6),0xffffff00);
    }

    private static void drawNonConsumed(GuiGraphicsExtractor graphics,int x,int y,int width,int height) {
        if(width<=0 || height<=0)return;
        // Fit the built-in 11x7 texture into the top-left, leaving the candidate badge free.
        float scale=Math.min(8f/11,Math.min((float)width/11,(float)height/7));
        var pose=graphics.pose();pose.pushMatrix();
        try {
            pose.translate(x,y);pose.scale(scale,scale);
            graphics.blit(RenderPipelines.GUI_TEXTURED,NON_CONSUMED_TEXTURE,0,0,0,0,11,7,11,7);
        } finally {pose.popMatrix();}
    }

    private static void drawLabel(GuiGraphicsExtractor graphics,String text,int x,int y,int width,int height,int color) {
        if (width<=0 || height<=0) return;
        var font=Minecraft.getInstance().font;
        int textWidth=font.width(text);
        float scale=Math.min(0.65f,Math.min((float)width/textWidth,(float)height/font.lineHeight));
        var pose=graphics.pose();pose.pushMatrix();
        try {
            pose.translate(x+width-textWidth*scale,y);pose.scale(scale,scale);
            graphics.text(font,text,0,0,color,true);
        } finally { pose.popMatrix(); }
    }
}
