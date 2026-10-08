package eakerzt.jiv.gui.bookmarks;

import com.mojang.serialization.Codec;

import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.runtime.IBookmarkManager;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.gui.bookmarks.planning.RecipeChain;
import eakerzt.jiv.gui.config.IBookmarkConfig;
import eakerzt.jiv.gui.overlay.bookmarks.BookmarkOverlay;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.overlay.ingredients.IIngredientGridSource;

import net.minecraft.core.RegistryAccess;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class BookmarkList implements IIngredientGridSource, IBookmarkManager {
	private List<IBookmark> bookmarksList;
	private final List<BookmarkPage> pages = new ArrayList<>();
	private int namespace;
	private @Nullable IBookmark draggedBookmark;
	private int draggedInput = -1;
	private int draggedGroup = -1;
	private List<IBookmark> dragOriginalOrder = List.of();
	private final Map<IBookmark, BookmarkState> dragOriginalStates = new IdentityHashMap<>();

	public void beginDrag(IBookmark bookmark, int inputSlot) {
		draggedBookmark = bookmark;
		draggedGroup = -1;
		draggedInput = inputSlot;
		dragOriginalOrder = new ArrayList<>(bookmarksList);
		dragOriginalStates.clear();
		for (IBookmark entry : bookmarksList) dragOriginalStates.put(entry, state(entry).copy());
		refreshDragLayout();
	}

	public void finishDrag(boolean commit) {
		if (draggedBookmark == null) return;
		if (!commit) {
			bookmarksList.clear();
			bookmarksList.addAll(dragOriginalOrder);
			page().states.clear();
			page().states.putAll(dragOriginalStates);
		}
		draggedBookmark = null;
		draggedGroup = -1;
		dragOriginalOrder = List.of();
		dragOriginalStates.clear();
		refreshDragLayout();
		if (commit) changed();
	}

	public void beginGroupDrag(int group) {
		var members = getGroupBookmarks(group);
		if (group == 0 || members.isEmpty()) return;
		beginDrag(members.getFirst(), -1);
		draggedGroup = group;
		refreshDragLayout();
	}

	private record LayoutGroup(int id, List<IBookmark> members) {}

	private List<LayoutGroup> layoutGroups() {
		List<LayoutGroup> result = new ArrayList<>();
		java.util.Set<Integer> seen = new java.util.HashSet<>();
		List<IBookmark> defaults = new ArrayList<>();
		for (IBookmark bookmark : bookmarksList) {
			int group = state(bookmark).group;
			if (group == 0) {
				defaults.add(bookmark);
				continue;
			}
			if (!seen.add(group)) continue;
			if (!defaults.isEmpty()) {
				result.add(new LayoutGroup(0, List.copyOf(defaults)));
				defaults.clear();
			}
			result.add(new LayoutGroup(group, getGroupBookmarks(group)));
		}
		if (!defaults.isEmpty()) result.add(new LayoutGroup(0, List.copyOf(defaults)));
		return result;
	}

	private void normalizeLayoutOrder() {
		var ordered = layoutGroups().stream().flatMap(group -> group.members().stream()).toList();
		bookmarksList.clear();
		bookmarksList.addAll(ordered);
	}

	public void moveBookmarkToGroup(IBookmark bookmark, int index, int group) {
		if (draggedInput >= 0 && draggedBookmark != null) return;
		normalizeLayoutOrder();
		int old = identityIndex(bookmark);
		if (old < 0 || index < 0 || index >= bookmarksList.size()) return;
		if (bookmarksList.stream()
				.anyMatch(b -> b != bookmark && state(b).group == group && b.equals(bookmark)))
			return;
		bookmarksList.remove(old);
		bookmarksList.add(index, bookmark);
		state(bookmark).group = group;
		changed();
	}

	public boolean moveGroupRelative(int group, IBookmark target, boolean after) {
		if (group == 0 || state(target).group == group) return false;
		normalizeLayoutOrder();
		var members = new ArrayList<>(getGroupBookmarks(group));
		if (members.isEmpty() || identityIndex(target) < 0) return false;
		int targetGroup = state(target).group;
		bookmarksList.removeIf(b -> state(b).group == group);
		int index = identityIndex(target);
		if (targetGroup != 0) {
			var targets = getGroupBookmarks(targetGroup);
			index = identityIndex(after ? targets.getLast() : targets.getFirst());
		}
		bookmarksList.addAll(index + (after ? 1 : 0), members);
		changed();
		return true;
	}

	private void refreshDragLayout() {
		layout = null;
		for (SourceListChangedListener listener : listeners) listener.onSourceListChanged();
	}

	private boolean isDraggedCell(IElement<?> element) {
		return draggedBookmark != null
				&& (draggedGroup >= 0
						? element.getBookmark()
								.filter(b -> state(b).group == draggedGroup)
								.isPresent()
						: element.getBookmark().orElse(null) == draggedBookmark)
				&& (draggedInput < 0
						|| element instanceof BookmarkCell<?> cell && cell.slot == draggedInput);
	}

	private <T> IElement<T> dragCell(IElement<T> element) {
		return isDraggedCell(element) ? new BookmarkDragPlaceholder<>(element) : element;
	}

	private int layoutColumns;
	private List<IElement<?>> layout;
	private final Map<IBookmark, BookmarkRecipeData> recipeData = new IdentityHashMap<>();
	private final Map<Integer, RecipeChain.Result> calculations = new HashMap<>();

	private final IRecipeManager recipeManager;
	private final IFocusFactory focusFactory;
	private final IIngredientManager ingredientManager;
	private final RegistryAccess registryAccess;
	private final IBookmarkConfig bookmarkConfig;
	private final IClientConfig clientConfig;
	private final IGuiHelper guiHelper;
	private final ICodecHelper codecHelper;
	private final List<SourceListChangedListener> listeners = new ArrayList<>();
	private final BookmarkFactory bookmarkFactory;
	private final Codec<IBookmark> bookmarkCodec;

	public BookmarkList(
			IRecipeManager recipeManager,
			IFocusFactory focusFactory,
			IIngredientManager ingredientManager,
			RegistryAccess registryAccess,
			IBookmarkConfig bookmarkConfig,
			IClientConfig clientConfig,
			IGuiHelper guiHelper,
			ICodecHelper codecHelper,
			BookmarkFactory bookmarkFactory,
			Codec<IBookmark> bookmarkCodec) {
		pages.add(new BookmarkPage());
		bookmarksList = pages.getFirst().bookmarks;
		this.recipeManager = recipeManager;
		this.focusFactory = focusFactory;
		this.ingredientManager = ingredientManager;
		this.registryAccess = registryAccess;
		this.bookmarkConfig = bookmarkConfig;
		this.clientConfig = clientConfig;
		this.guiHelper = guiHelper;
		this.codecHelper = codecHelper;
		this.bookmarkFactory = bookmarkFactory;
		this.bookmarkCodec = bookmarkCodec;
	}

	public boolean add(IBookmark value) {
		if (!addToListWithoutNotifying(value, clientConfig.bookmarkAddPosition().get().isFront())) {
			return false;
		}
		notifyListenersOfChange();
		save();
		return true;
	}

	public void moveBookmark(IBookmark previousBookmark, IBookmark newBookmark, int offset) {
		if (!bookmarksList.contains(newBookmark) || !bookmarksList.contains(previousBookmark)) {
			return;
		}
		int i = identityIndex(previousBookmark);
		moveBookmark(newBookmark, Math.floorMod(i + offset, bookmarksList.size()));
	}

	public void moveBookmark(IBookmark bookmark, int index) {
		if (draggedBookmark != null && draggedInput >= 0) return;
		normalizeLayoutOrder();
		int oldIndex = identityIndex(bookmark);
		if (oldIndex < 0 || index < 0 || index >= bookmarksList.size() || oldIndex == index) {
			return;
		}
		Objects.checkIndex(index, bookmarksList.size());
		IBookmark target = bookmarksList.get(index);
		bookmarksList.remove(oldIndex);
		if (target != null) {
			int targetGroup = state(target).group;
			if (draggedBookmark != null
					&& bookmarksList.stream()
							.anyMatch(b -> state(b).group == targetGroup && b.equals(bookmark))) {
				bookmarksList.add(oldIndex, bookmark);
				return;
			}
			state(bookmark).group = targetGroup;
		}
		bookmarksList.add(index, bookmark);

		notifyListenersOfChange();
		save();
	}

	public void moveBookmarkToFront(IBookmark value) {
		moveBookmark(value, 0);
	}

	public boolean contains(IBookmark value) {
		return find(value, 0) != null;
	}

	@Override
	public boolean contains(ITypedIngredient<?> ingredient) {
		return contains(bookmarkFactory.create(ingredient));
	}

	public <T> boolean onElementBookmarked(
			IElement<T> element, UserInput input, BookmarkOverlay bookmarkOverlay) {
		if (bookmarkOverlay.isBookmarkElementUnderMouse(
				element, input.getMouseX(), input.getMouseY())) {
			var ingredientBookmark =
					element.getBookmark().filter(IngredientBookmark.class::isInstance);
			if (ingredientBookmark.isPresent()) return remove(ingredientBookmark.get());
		}

		ITypedIngredient<T> ingredient = element.getTypedIngredient();
		IBookmark bookmark = bookmarkFactory.create(ingredient);
		toggleInGroup(bookmark, 0, 0);
		return true;
	}

	@Override
	public boolean add(ITypedIngredient<?> ingredient) {
		IBookmark bookmark = bookmarkFactory.create(ingredient);
		return add(bookmark);
	}

	public void toggleBookmark(IBookmark bookmark) {
		if (remove(bookmark)) {
			return;
		}
		add(bookmark);
	}

	public boolean remove(IBookmark ingredient) {
		IBookmark existing =
				bookmarksList.stream()
						.filter(b -> b == ingredient)
						.findFirst()
						.orElseGet(() -> find(ingredient, 0));
		if (existing == null) return false;
		bookmarksList.remove(identityIndex(existing));
		page().states.remove(existing);
		recipeData.remove(existing);
		notifyListenersOfChange();
		save();
		return true;
	}

	@Override
	public boolean remove(ITypedIngredient<?> ingredient) {
		return remove(bookmarkFactory.create(ingredient));
	}

	public void setFromConfigFile(List<IBookmark> bookmarks) {
		BookmarkPage page = new BookmarkPage();
		for (IBookmark bookmark : bookmarks)
			if (!page.bookmarks.contains(bookmark)) page.bookmarks.add(bookmark);
		setWorkspace(List.of(page), 0);
	}

	private boolean addToListWithoutNotifying(IBookmark value, boolean addToFront) {
		if (find(value, 0) != null) return false;
		if (addToFront) bookmarksList.addFirst(value);
		else bookmarksList.add(value);
		page().state(value);
		return true;
	}

	@Override
	public List<IElement<?>> getElements() {
		if (layoutColumns == 0)
			return bookmarksList.stream().<IElement<?>>map(b -> dragCell(b.getElement())).toList();
		if (layout == null) layout = buildLayout();
		return draggedBookmark == null
				? layout
				: layout.stream().<IElement<?>>map(this::dragCell).toList();
	}

	private List<IElement<?>> buildLayout() {
		List<IElement<?>> cells = new ArrayList<>();
		calculations.clear();
		for (var segment : layoutGroups()) {
			int groupId = segment.id();
			BookmarkGroup group = page().groups.get(groupId);
			List<IBookmark> members = segment.members();
			if (members.isEmpty()) continue;
			pad(cells, groupId);
			List<RecipeChain.Recipe> recipes = new ArrayList<>();
			for (IBookmark member : getGroupBookmarks(groupId))
				if (member instanceof RecipeBookmark<?, ?> recipe) {
					BookmarkRecipeData data =
							recipeData.computeIfAbsent(
									member,
									ignored ->
											BookmarkRecipeData.create(
													recipe, recipeManager, focusFactory));
					recipes.add(data.asRecipe(member, state(member), ingredientManager));
				}
			var result = RecipeChain.calculate(recipes, group.linked);
			calculations.put(groupId, result);
			for (IBookmark member : members) {
				BookmarkState state = state(member);
				if (group.todo) pad(cells, groupId);
				if (member instanceof RecipeBookmark<?, ?>) {
					BookmarkRecipeData data = recipeData.get(member);
					long crafts = result.crafts().getOrDefault(member, state.multiplier);
					List<BookmarkRecipeData.DisplaySlot> ordered =
							new ArrayList<>(data.displaySlots(state, ingredientManager));
					ordered.sort(
							Comparator.comparing(
									display ->
											display.slot().role() != RecipeIngredientRole.OUTPUT));
					for (BookmarkRecipeData.DisplaySlot display : ordered) {
						BookmarkRecipeData.Slot slot = display.slot();
						if (slot.role() != RecipeIngredientRole.INPUT
								&& slot.role() != RecipeIngredientRole.OUTPUT) continue;
						boolean output = slot.role() == RecipeIngredientRole.OUTPUT;
						if ((!group.todo || group.collapsed || state.collapsed) && !output)
							continue;
						if (!output
								&& group.todo
								&& layoutColumns > 1
								&& cells.size() % layoutColumns == 0)
							cells.add(BookmarkCell.gap(groupId));
						var ingredient = display.ingredient();
						var key = BookmarkRecipeData.key(ingredient, ingredientManager);
						boolean finalOutput = output && result.outputs().containsKey(key);
						boolean remainder = output && result.remainders().containsKey(key);
						if (group.linked && group.collapsed && output && !finalOutput && !remainder)
							continue;
						long amount =
								slot.reusable()
										? display.amount()
										: RecipeChain.multiply(display.amount(), crafts);
						cells.add(
								new BookmarkCell<>(
												ingredientManager.normalizeTypedIngredient(
														ingredient),
												member,
												groupId,
												slot.index(),
												slot.role(),
												amount,
												crafts,
												slot.reusable(),
												slot.chance(),
												finalOutput,
												remainder)
										.withCandidates(
												slot.candidates(),
												slot.tagKey(),
												state.choices.getOrDefault(slot.index(), 0)));
					}
					if (data.slots().isEmpty()) cells.add(member.getElement());
				} else {
					cells.add(
							new BookmarkCell<>(
									member.getElement().getTypedIngredient(),
									member,
									groupId,
									-1,
									RecipeIngredientRole.RENDER_ONLY,
									state.multiplier,
									0,
									false,
									1,
									false,
									false));
				}
			}
			pad(cells, groupId);
		}
		return List.copyOf(cells);
	}

	private void pad(List<IElement<?>> cells, int group) {
		while (cells.size() % layoutColumns != 0) cells.add(BookmarkCell.gap(group));
	}

	public void setLayoutColumns(int columns) {
		columns = Math.max(1, columns);
		if (layoutColumns != columns) {
			layoutColumns = columns;
			layout = null;
		}
	}

	public BookmarkPage page() {
		return pages.get(namespace);
	}

	public BookmarkState state(IBookmark bookmark) {
		return page().state(bookmark);
	}

	public List<BookmarkPage> getPages() {
		return List.copyOf(pages);
	}

	public int getNamespace() {
		return namespace;
	}

	public void setWorkspace(List<BookmarkPage> loaded, int selected) {
		pages.clear();
		pages.addAll(loaded);
		if (pages.isEmpty()) pages.add(new BookmarkPage());
		namespace = Math.clamp(selected, 0, pages.size() - 1);
		bookmarksList = page().bookmarks;
		recipeData.clear();
		notifyListenersOfChange();
	}

	public void changeNamespace(int delta) {
		int next = namespace + delta;
		if (next < 0) next = pages.size() - 1;
		if (next >= pages.size()) {
			if (page().bookmarks.isEmpty()) next = 0;
			else {
				pages.add(new BookmarkPage());
				next = pages.size() - 1;
			}
		}
		namespace = next;
		bookmarksList = page().bookmarks;
		notifyListenersOfChange();
		save();
	}

	private int identityIndex(IBookmark bookmark) {
		for (int i = 0; i < bookmarksList.size(); i++)
			if (bookmarksList.get(i) == bookmark) return i;
		return -1;
	}

	private IBookmark find(IBookmark value, int group) {
		return bookmarksList.stream()
				.filter(b -> state(b).group == group && b.equals(value))
				.findFirst()
				.orElse(null);
	}

	public void toggleInGroup(IBookmark bookmark, int group, long multiplier) {
		IBookmark existing = find(bookmark, group);
		if (existing != null) {
			remove(existing);
			return;
		}
		BookmarkState state = state(bookmark);
		state.group = group;
		state.multiplier = Math.max(0, multiplier);
		page().groups.computeIfAbsent(group, ignored -> new BookmarkGroup());
		if (clientConfig.bookmarkAddPosition().get().isFront()) bookmarksList.addFirst(bookmark);
		else bookmarksList.add(bookmark);
		notifyListenersOfChange();
		save();
	}

	public void changeQuantity(IBookmark bookmark, long delta) {
		BookmarkState state = state(bookmark);
		state.multiplier =
				delta >= 0
						? RecipeChain.add(state.multiplier, delta)
						: Math.max(0, state.multiplier + delta);
		changed();
	}

	public void cycleChoice(IBookmark bookmark, int slot, int delta) {
		BookmarkRecipeData data = recipeData.get(bookmark);
		if (data == null) return;
		BookmarkState state = state(bookmark);
		data.displaySlots(state, ingredientManager).stream()
				.filter(display -> display.slot().index() == slot)
				.findFirst()
				.ifPresent(display -> display.cycleChoice(state, delta));
		changed();
	}

	public boolean moveRecipeInput(IBookmark bookmark, int source, int target) {
		if (draggedBookmark != null && (bookmark != draggedBookmark || source != draggedInput))
			return false;
		BookmarkRecipeData data = recipeData.get(bookmark);
		if (data == null || !data.moveInput(state(bookmark), source, target, ingredientManager))
			return false;
		changed();
		return true;
	}


	public void groupBookmarks(List<IBookmark> selected, int target) {
		if (selected.isEmpty()) return;
		if (target < 0) {
			target = page().nextGroupId();
			page().groups.put(target, page().groups.get(0).copy());
		}
		for (IBookmark bookmark : selected) state(bookmark).group = target;
		changed();
	}

	public void moveGroup(int group, int target) {
		var targets = getGroupBookmarks(target);
		if (!targets.isEmpty()) moveGroupRelative(group, targets.getFirst(), false);
	}

	public void removeGroup(int group) {
		bookmarksList.removeIf(b -> state(b).group == group);
		if (group != 0) page().groups.remove(group);
		changed();
	}

	public void changed() {
		notifyListenersOfChange();
		save();
	}

	private void save() {
		if (draggedBookmark != null) return;
		bookmarkConfig.saveWorkspace(
				recipeManager,
				focusFactory,
				guiHelper,
				ingredientManager,
				registryAccess,
				codecHelper,
				getPages(),
				namespace,
				bookmarkCodec);
	}

	public Optional<RecipeChain.Result> calculation(int group) {
		getElements();
		return Optional.ofNullable(calculations.get(group));
	}

	public Map<Object, eakerzt.jiv.api.ingredients.ITypedIngredient<?>> materials(int group) {
		Map<Object, eakerzt.jiv.api.ingredients.ITypedIngredient<?>> values = new LinkedHashMap<>();
		for (IBookmark bookmark : bookmarksList)
			if (state(bookmark).group == group && recipeData.containsKey(bookmark))
				for (var slot : recipeData.get(bookmark).slots()) {
					var ingredient = slot.selected(state(bookmark));
					values.put(BookmarkRecipeData.key(ingredient, ingredientManager), ingredient);
				}
		return values;
	}

	public RecipeChain.Result calculateWithInventory(int group, Map<Object, Long> inventory) {
		getElements();
		var recipes = new ArrayList<RecipeChain.Recipe>();
		for (IBookmark bookmark : bookmarksList)
			if (state(bookmark).group == group && recipeData.containsKey(bookmark))
				recipes.add(
						recipeData
								.get(bookmark)
								.asRecipe(bookmark, state(bookmark), ingredientManager));
		return RecipeChain.calculate(recipes, true, inventory);
	}

	public List<IBookmark> getGroupBookmarks(int group) {
		return bookmarksList.stream().filter(b -> state(b).group == group).toList();
	}

	public Optional<BookmarkRecipeData> data(IBookmark bookmark) {
		getElements();
		return Optional.ofNullable(recipeData.get(bookmark));
	}

	public List<IElement<?>> getBookmarkElements() {
		// Sorting indices refer to owners, never to transient drag placeholders.
		return layoutGroups().stream()
				.flatMap(group -> group.members().stream())
				.<IElement<?>>map(IBookmark::getElement)
				.toList();
	}

	@Override
	public boolean containsElement(IElement<?> element) {
		return getElements().contains(element)
				|| bookmarksList.stream().anyMatch(bookmark -> bookmark.getElement() == element);
	}

	@Nullable
	public <R> RecipeBookmark<R, ?> getMatchingBookmark(IRecipeType<R> recipeType, R recipe) {
		for (IBookmark bookmark : bookmarksList) {
			if (bookmark instanceof RecipeBookmark<?, ?> recipeBookmark) {
				if (state(bookmark).group == 0 && recipeBookmark.isRecipe(recipeType, recipe)) {
					@SuppressWarnings("unchecked")
					RecipeBookmark<R, ?> castBookmark = (RecipeBookmark<R, ?>) recipeBookmark;
					return castBookmark;
				}
			}
		}
		return null;
	}

	public boolean isEmpty() {
		return pages.stream().allMatch(p -> p.bookmarks.isEmpty() && p.unresolved.isEmpty());
	}

	@Override
	public void addSourceListChangedListener(SourceListChangedListener listener) {
		listeners.add(listener);
	}

	private void notifyListenersOfChange() {
		if (draggedBookmark != null) {
			refreshDragLayout();
			return;
		}
		// A group can contain the same ingredient/recipe once; independent groups keep independent
		// counts.
		List<IBookmark> unique = new ArrayList<>();
		for (IBookmark bookmark : new ArrayList<>(bookmarksList)) {
			IBookmark duplicate =
					unique.stream()
							.filter(
									b ->
											state(b).group == state(bookmark).group
													&& b.equals(bookmark))
							.findFirst()
							.orElse(null);
			if (duplicate == null) unique.add(bookmark);
			else {
				state(duplicate).multiplier =
						RecipeChain.add(state(duplicate).multiplier, state(bookmark).multiplier);
				page().states.remove(bookmark);
				recipeData.remove(bookmark);
			}
		}
		bookmarksList.clear();
		bookmarksList.addAll(unique);
		layout = null;
		for (SourceListChangedListener listener : listeners) {
			listener.onSourceListChanged();
		}
	}
}
