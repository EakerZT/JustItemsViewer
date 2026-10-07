package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.inputs.IJivUserInput;
import eakerzt.jiv.api.runtime.IJivKeyMapping;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.api.gui.buttons.IButtonState;
import eakerzt.jiv.api.gui.buttons.IIconButtonController;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class BookmarkButtonController implements IIconButtonController {
	private final IDrawable offIcon;
	private final IDrawable onIcon;
	private final BookmarkOverlay bookmarkOverlay;
	private final BookmarkList bookmarkList;
	private final IClientToggleState toggleState;
	private final IInternalKeyMappings keyBindings;

	public BookmarkButtonController(BookmarkOverlay bookmarkOverlay, BookmarkList bookmarkList, IClientToggleState toggleState, IInternalKeyMappings keyBindings) {
		Textures textures = Internal.getTextures();
		this.offIcon = textures.getBookmarkButtonDisabledIcon();
		this.onIcon = textures.getBookmarkButtonEnabledIcon();
		this.bookmarkOverlay = bookmarkOverlay;
		this.bookmarkList = bookmarkList;
		this.toggleState = toggleState;
		this.keyBindings = keyBindings;
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
		if (toggleState.isBookmarkOverlayEnabled()) {
			tooltip.add(Component.translatable("jiv.tooltip.bookmarks.disable"));
		} else {
			tooltip.add(Component.translatable("jiv.tooltip.bookmarks.enable"));
		}
		IJivKeyMapping bookmarkKey = keyBindings.getBookmark();
		if (bookmarkKey.isUnbound()) {
			MutableComponent noKey = Component.translatable("jiv.tooltip.bookmarks.usage.nokey");
			tooltip.add(noKey.withStyle(ChatFormatting.RED));
		} else if (!bookmarkOverlay.hasRoom()) {
			MutableComponent notEnoughSpace = Component.translatable("jiv.tooltip.bookmarks.not.enough.space");
			tooltip.add(notEnoughSpace.withStyle(ChatFormatting.GOLD));
		} else {
			tooltip.addKeyUsageComponent(
				"jiv.tooltip.bookmarks.usage.key",
				bookmarkKey
			);
		}
	}

	@Override
	public void updateState(IButtonState state) {
		if (toggleState.isBookmarkOverlayEnabled()) {
			state.setIcon(onIcon);
			state.setForcePressed(true);
		} else {
			state.setIcon(offIcon);
			state.setForcePressed(false);
		}
	}

	@Override
	public boolean onPress(IJivUserInput input) {
		if (!bookmarkList.isEmpty() && bookmarkOverlay.hasRoom()) {
			if (!input.isSimulate()) {
				toggleState.toggleBookmarkEnabled();
			}
			return true;
		}
		return false;
	}
}
