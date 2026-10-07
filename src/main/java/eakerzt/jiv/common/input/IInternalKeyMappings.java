package eakerzt.jiv.common.input;

import eakerzt.jiv.api.runtime.IJivKeyMapping;
import eakerzt.jiv.api.runtime.IJivKeyMappings;
import eakerzt.jiv.common.input.keys.IJivKeyMappingInternal;
import eakerzt.jiv.common.input.keys.IJivKeyMappingWithExtraModifiers;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface IInternalKeyMappings extends IJivKeyMappings {
	IJivKeyMapping getToggleOverlay();
	IJivKeyMapping getFocusSearch();
	IJivKeyMapping getToggleCheatMode();
	IJivKeyMapping getToggleEditMode();

	IJivKeyMapping getToggleCheatModeConfigButton();

	IJivKeyMapping getRecipeBack();
	IJivKeyMapping getRecipeForward();
	IJivKeyMapping getPreviousCategory();
	IJivKeyMapping getNextCategory();
	IJivKeyMapping getPreviousRecipePage();
	IJivKeyMapping getNextRecipePage();
	IJivKeyMappingInternal getPauseRecipeCycling();

	IJivKeyMapping getPreviousPage();
	IJivKeyMapping getNextPage();

	IJivKeyMapping getCloseRecipeGui();

	@Override
	IJivKeyMappingWithExtraModifiers getBookmark();
	IJivKeyMapping getToggleBookmarkOverlay();

	@Override
	IJivKeyMappingWithExtraModifiers getShowRecipe();

	@Override
	IJivKeyMappingWithExtraModifiers getShowUses();

	IJivKeyMapping getTransferRecipeBookmark();
	IJivKeyMapping getMaxTransferRecipeBookmark();
	IJivKeyMapping getQuickMove();
	IJivKeyMapping getShareToChat();

	IJivKeyMapping getCheatOneItem();
	IJivKeyMapping getCheatItemStack();

	IJivKeyMapping getToggleHideIngredient();
	IJivKeyMapping getToggleWildcardHideIngredient();

	IJivKeyMapping getHoveredClearSearchBar();
	IJivKeyMapping getPreviousSearch();
	IJivKeyMapping getNextSearch();

	IJivKeyMapping getCopyRecipeId();

	@Unmodifiable
	List<KeyMapping> getConfigKeyMappings();

	// internal only, unregistered and can't be changed because they match vanilla Minecraft hard-coded keys:
	IJivKeyMapping getEscapeKey();
	IJivKeyMapping getLeftClick();
	IJivKeyMapping getRightClick();
	IJivKeyMapping getEnterKey();
}
