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

    /** Pin substitutions for execution, keeping all original shaped-slot positions. */
    public BookmarkTransferSlots(IRecipeSlotsView source, BookmarkRecipeData data, BookmarkState state) {
        var selected = new java.util.HashMap<Integer, ITypedIngredient<?>>();
        for (var slot : data.slots()) {
            if (slot.role() == RecipeIngredientRole.INPUT) selected.put(slot.index(), slot.selected(state));
        }
        var original = source.getSlotViews();
        slots = IntStream.range(0, original.size()).mapToObj(i -> {
            var slot = original.get(i);
            var ingredient = selected.get(i);
            return ingredient == null ? slot : (IRecipeSlotView) new SelectedSlot(slot, ingredient);
        }).toList();
    }

    private record SelectedSlot(IRecipeSlotView source, ITypedIngredient<?> ingredient) implements IRecipeSlotView {
        @Override public Stream<ITypedIngredient<?>> getAllIngredients() { return Stream.of(ingredient); }
        @Override public List<@Nullable ITypedIngredient<?>> getAllIngredientsList() { return List.of(ingredient); }
        @Override public Optional<ITypedIngredient<?>> getDisplayedIngredient() { return Optional.of(ingredient); }
        @Override public Stream<ITypedIngredient<?>> getDisplayedIngredients() { return Stream.of(ingredient); }
        @Override public Optional<TagKey<?>> getTagKey() { return Optional.empty(); }
        @Override public RecipeIngredientRole getRole() { return source.getRole(); }
        @Override public boolean isNonConsumed() { return source.isNonConsumed(); }
        @Override public double getChance() { return source.getChance(); }
        @Override public void drawHighlight(GuiGraphicsExtractor graphics, int color) { source.drawHighlight(graphics, color); }
        @Override public Optional<String> getSlotName() { return source.getSlotName(); }
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
