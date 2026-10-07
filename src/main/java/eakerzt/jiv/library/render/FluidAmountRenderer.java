package eakerzt.jiv.library.render;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.common.platform.IPlatformFluidHelperInternal;
import eakerzt.jiv.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.OptionalLong;

/** Slot amount labels use the displayed fluid rather than a fixed recipe amount. */
public final class FluidAmountRenderer {
    private static final String[] SUFFIXES = {"K", "M", "G", "T", "P", "E"};
    private FluidAmountRenderer() {}

    public static String format(long amount) {
        if (amount < 10_000) return Long.toString(amount);
        long divisor = 1_000;
        int unit = 0;
        while (amount / divisor >= 1_000 && unit < SUFFIXES.length - 1) {
            divisor *= 1_000;
            unit++;
        }
        return amount / divisor + SUFFIXES[unit];
    }

    public static OptionalLong getAmount(ITypedIngredient<?> ingredient) {
        return getAmount(Services.PLATFORM.getFluidHelper(), ingredient);
    }

    private static <T> OptionalLong getAmount(IPlatformFluidHelperInternal<T> helper, ITypedIngredient<?> ingredient) {
        var fluid = ingredient.getIngredient(helper.getFluidIngredientType());
        if (fluid.isEmpty() || helper.isEmpty(fluid.get())) return OptionalLong.empty();
        long amount = helper.getAmount(fluid.get());
        return amount > 0 ? OptionalLong.of(amount) : OptionalLong.empty();
    }

    public static void draw(GuiGraphicsExtractor graphics, long amount, int x, int y, int width, int height) {
        if (amount <= 0 || width <= 0 || height <= 0) return;
        var font = Minecraft.getInstance().font;
        String text = format(amount);
        float scale = Math.min(1f, Math.min((float) width / font.width(text), (float) height / font.lineHeight));
        var pose = graphics.pose();
        pose.pushMatrix();
        try {
            pose.translate(x + width - font.width(text) * scale, y + height - font.lineHeight * scale);
            pose.scale(scale, scale);
            graphics.text(font, text, 0, 0, 0xffffffff, true);
        } finally {
            pose.popMatrix();
        }
    }
}
