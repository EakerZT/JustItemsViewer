package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.gui.inputs.RecipeSlotUnderMouse;
import eakerzt.jiv.api.gui.widgets.IRecipeScreenExtension;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.stream.Stream;

/** Owns screen-only extensions and pointer capture, independently of normal input simulation. */
final class RecipeScreenExtensions {
    private record Binding(IRecipeLayoutDrawable<?> layout, IRecipeScreenExtension extension) {
        double x(double x) { return x - layout.getRect().getX(); }
        double y(double y) { return y - layout.getRect().getY(); }
    }
    private List<Binding> bindings = List.of();
    private final Map<Integer, Binding> captures = new HashMap<>();

    void setLayouts(List<IRecipeLayoutWithButtons<?>> layouts) {
        var next = layouts.stream().flatMap(l -> l.getRecipeLayout().getScreenExtensions().stream()
            .map(e -> new Binding(l.getRecipeLayout(), e))).toList();
        for (var old : bindings) if (!next.contains(old)) old.extension.cancelScreenInteraction();
        captures.values().removeIf(b -> !next.contains(b));
        bindings = next;
    }
    void clear() {
        bindings.forEach(b -> b.extension.cancelScreenInteraction());
        captures.clear();
        bindings = List.of();
    }
    void update(IGuiProperties properties, int sideHeight) {
        bindings.forEach(b -> b.extension.updateScreenLayout(properties, sideHeight));
    }
    Stream<ScreenRectangle> areas() { return bindings.stream().flatMap(b -> b.extension.getExtraGuiAreas().stream()); }
    Optional<Component> title() { return bindings.stream().map(b -> b.extension.getScreenTitle()).filter(Objects::nonNull).findFirst(); }
    void draw(GuiGraphicsExtractor graphics, int x, int y) { bindings.forEach(b -> b.extension.drawScreen(graphics, x, y)); }
    void drawTooltips(GuiGraphicsExtractor graphics, int x, int y) { bindings.forEach(b -> b.extension.drawScreenTooltips(graphics, x, y)); }
    Stream<RecipeSlotUnderMouse> slots(double x, double y) {
        return bindings.stream().flatMap(b -> b.extension.getScreenSlotUnderMouse(x, y).stream());
    }
    boolean clicked(MouseButtonEvent event) {
        for (var b : bindings) if (b.extension.mouseClicked(b.x(event.x()), b.y(event.y()), event.button())) {
            captures.put(event.button(), b);
            return true;
        }
        return false;
    }
    boolean dragged(MouseButtonEvent event, double dx, double dy) {
        var b = captures.get(event.button());
        if (b == null) return false;
        b.extension.mouseDragged(b.x(event.x()), b.y(event.y()), event.button(), dx, dy);
        return true;
    }
    boolean released(MouseButtonEvent event) {
        var b = captures.remove(event.button());
        if (b == null) return false;
        b.extension.mouseReleased(b.x(event.x()), b.y(event.y()), event.button());
        return true;
    }
    boolean scrolled(double x, double y, double dx, double dy) {
        for (var b : bindings) if (b.extension.mouseScrolled(b.x(x), b.y(y), dx, dy)) return true;
        return false;
    }
}
