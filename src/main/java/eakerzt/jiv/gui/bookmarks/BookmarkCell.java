package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.overlay.ingredients.IngredientGridTooltipHelper;
import eakerzt.jiv.gui.util.FocusUtil;
import eakerzt.jiv.library.gui.ingredients.TagContentTooltipComponent;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/** A material cell belonging to a bookmarked recipe, or a non-interactive layout gap. */
public final class BookmarkCell<T> implements IElement<T> {
	private List<ITypedIngredient<?>> candidates = List.of();
	private @Nullable TagContentTooltipComponent candidatesTooltip;
	private Optional<net.minecraft.tags.TagKey<?>> candidateTag = Optional.empty();
	private int selectedCandidateIndex;

	public BookmarkCell<T> withCandidates(List<ITypedIngredient<?>> candidates) {
		return withCandidates(candidates, Optional.empty(), 0);
	}

	public BookmarkCell<T> withCandidates(
			List<ITypedIngredient<?>> candidates,
			Optional<net.minecraft.tags.TagKey<?>> tag,
			int selectedIndex) {
		this.candidates = List.copyOf(candidates);
		this.candidateTag = tag;
		this.selectedCandidateIndex =
				candidates.isEmpty() ? -1 : Math.floorMod(selectedIndex, candidates.size());
		this.candidatesTooltip = null;
		return this;
	}

	public int selectedCandidateIndex() {
		return selectedCandidateIndex;
	}

	public List<ITypedIngredient<?>> candidates() {
		return candidates;
	}

	public boolean hasCandidates() {
		return role == RecipeIngredientRole.INPUT && candidates.size() > 1;
	}

	private final @Nullable ITypedIngredient<T> ingredient;
	public final @Nullable IBookmark bookmark;
	public final int group;
	public final int slot;
	public final RecipeIngredientRole role;
	public final long amount;
	public final long crafts;
	public final boolean reusable;
	public final double chance;
	public final boolean result;
	public final boolean remainder;

	public BookmarkCell(
			@Nullable ITypedIngredient<T> ingredient,
			@Nullable IBookmark bookmark,
			int group,
			int slot,
			RecipeIngredientRole role,
			long amount,
			long crafts,
			boolean reusable,
			double chance,
			boolean result,
			boolean remainder) {
		this.ingredient = ingredient;
		this.bookmark = bookmark;
		this.group = group;
		this.slot = slot;
		this.role = role;
		this.amount = amount;
		this.crafts = crafts;
		this.reusable = reusable;
		this.chance = chance;
		this.result = result;
		this.remainder = remainder;
	}

	public static BookmarkCell<?> gap(int group) {
		return new BookmarkCell<>(
				null,
				null,
				group,
				-1,
				RecipeIngredientRole.RENDER_ONLY,
				0,
				0,
				false,
				1,
				false,
				false);
	}

	@Override
	public boolean isEmptySlot() {
		return ingredient == null;
	}

	@Override
	public ITypedIngredient<T> getTypedIngredient() {
		if (ingredient == null) throw new IllegalStateException("Empty bookmark cell");
		return ingredient;
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.ofNullable(bookmark);
	}

	@Override
	public @Nullable IDrawable createRenderOverlay() {
		if (hasCandidates()) {
			var textures = eakerzt.jiv.common.Internal.getTextures();
			IDrawable badge =
					candidateTag.isPresent()
							? textures.getTagBadgeIcon()
							: textures.getListBadgeIcon();
			return new IDrawable() {
				public int getWidth() {
					return 16;
				}

				public int getHeight() {
					return 16;
				}

				public void draw(
						net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x, int y) {
					badge.draw(graphics, x + 17 - badge.getWidth(), y - 1);
				}
			};
		}
		return role == RecipeIngredientRole.OUTPUT && bookmark != null
				? bookmark.getElement().createRenderOverlay()
				: null;
	}

	@Override
	public void show(IRecipesGui gui, FocusUtil focus, List<RecipeIngredientRole> roles) {
		if (bookmark != null
				&& role == RecipeIngredientRole.OUTPUT
				&& roles.equals(List.of(RecipeIngredientRole.OUTPUT)))
			bookmark.getElement().show(gui, focus, roles);
		else gui.show(focus.createFocuses(getTypedIngredient(), roles));
	}

	@Override
	public void getTooltip(
			JivTooltip tooltip,
			IngredientGridTooltipHelper helper,
			IIngredientRenderer<T> renderer,
			IIngredientHelper<T> ingredientHelper) {
		helper.getIngredientTooltip(tooltip, getTypedIngredient(), renderer, ingredientHelper);
		if (amount >= 0 && !reusable)
			tooltip.add(Component.translatable("jiv.bookmarks.amount", amount));
		if (reusable) tooltip.add(Component.translatable("jiv.tooltip.recipe.not_consumed"));
		if (chance < 1)
			tooltip.add(
					Component.translatable(
							"jiv.tooltip.recipe.chance",
							eakerzt.jiv.library.render.RecipeSlotDecorations.exactPercent(chance)));
		if (hasCandidates()) {
			if (eakerzt.jiv.common.Internal.getClientConfigs()
					.getClientConfig()
					.tagContentTooltipEnabled()
					.get()) {
				if (candidatesTooltip == null)
					candidatesTooltip =
							new TagContentTooltipComponent(
									eakerzt.jiv.common.Internal.getJivRuntime()
											.getIngredientManager(),
									candidates);
				candidatesTooltip.setSelectedIndex(selectedCandidateIndex);
				tooltip.add(candidatesTooltip);
			}
			tooltip.add(Component.translatable("jiv.bookmarks.controls.choice"));
			tooltip.add(
					new eakerzt.jiv.common.gui.RecipeSlotOptionsTooltipComponent(
							eakerzt.jiv.common.Internal.getKeyMappings().getPauseRecipeCycling()));
		} else if (role != RecipeIngredientRole.INPUT)
			tooltip.add(Component.translatable("jiv.bookmarks.controls.quantity"));
	}

	@Override
	public boolean handleClick(UserInput input, IInternalKeyMappings keys) {
		return bookmark != null && bookmark.getElement().handleClick(input, keys);
	}

	@Override
	public boolean isVisible() {
		return bookmark == null || bookmark.isVisible();
	}

	@Override
	public void tick() {}
}
