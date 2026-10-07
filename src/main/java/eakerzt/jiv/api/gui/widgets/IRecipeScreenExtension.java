package eakerzt.jiv.api.gui.widgets;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.gui.inputs.RecipeSlotUnderMouse;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Per-layout extension for recipe-screen side panels and direct mouse gestures.
 * Register with {@link IRecipeExtrasBuilder#addScreenExtension} during extras creation.
 * Layout, drawing, extra bounds and slot queries use absolute screen coordinates.
 * Input uses coordinates relative to the recipe's top-left. The extension performs its own hit-testing.
 * Consuming a press captures that button through dragging and release outside the recipe.
 * Capture is cancelled when the layout changes or the screen closes.
 * These callbacks run only in the recipe screen, never in bookmarks or tooltips.
 */
public interface IRecipeScreenExtension {
    default @Nullable Component getScreenTitle() { return null; }
    default void updateScreenLayout(IGuiProperties recipeGui, int availableSideHeight) {}
    /** Occupied screen bounds. Empty panels should return no bounds. */
    default List<ScreenRectangle> getExtraGuiAreas() { return List.of(); }
    default void drawScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}
    default void drawScreenTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}
    /** Slots have absolute positions and must return an offset of zero. */
    default Optional<RecipeSlotUnderMouse> getScreenSlotUnderMouse(double mouseX, double mouseY) { return Optional.empty(); }
    /** Return true to consume the press and capture this mouse button. */
    default boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    /** Captured drags remain consumed even if this callback returns false. */
    default boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }
    /** Captured release remains consumed even if this callback returns false. */
    default boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    /** Return true to consume scrolling before ordinary page/category navigation. */
    default boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { return false; }
    default void cancelScreenInteraction() {}
}
