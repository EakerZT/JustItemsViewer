package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Keep shaped transfer indices intact while excluding removed material slots. */
public final class BookmarkTransferSlots implements IRecipeSlotsView {
    private final List<IRecipeSlotView> slots;

    public BookmarkTransferSlots(IRecipeSlotsView source, Set<Integer> removedSlots) {
        var original = source.getSlotViews();
        slots = IntStream.range(0, original.size()).mapToObj(i -> {
            IRecipeSlotView slot = original.get(i);
            return removedSlots.contains(i) && slot.getRole() == RecipeIngredientRole.INPUT
                    ? (IRecipeSlotView) new EmptySlot(slot) : slot;
        }).toList();
    }

    @Override public List<IRecipeSlotView> getSlotViews() { return slots; }

    private record EmptySlot(IRecipeSlotView source) implements IRecipeSlotView {
        @Override public Stream<ITypedIngredient<?>> getAllIngredients() { return Stream.empty(); }
        @Override public List<@Nullable ITypedIngredient<?>> getAllIngredientsList() { return List.of(); }
        @Override public Optional<ITypedIngredient<?>> getDisplayedIngredient() { return Optional.empty(); }
        @Override public Stream<ITypedIngredient<?>> getDisplayedIngredients() { return Stream.empty(); }
        @Override public Optional<TagKey<?>> getTagKey() { return Optional.empty(); }
        @Override public RecipeIngredientRole getRole() { return source.getRole(); }
        @Override public boolean isNonConsumed() { return source.isNonConsumed(); }
        @Override public double getChance() { return source.getChance(); }
        @Override public void drawHighlight(GuiGraphicsExtractor graphics, int color) { source.drawHighlight(graphics, color); }
        @Override public Optional<String> getSlotName() { return source.getSlotName(); }
    }
}
