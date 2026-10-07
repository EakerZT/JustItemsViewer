package eakerzt.jiv.api.gui.widgets;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.inputs.IJivInputHandler;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * A smooth-scrolling area with a scrollbar.
 *
 * Create one with {@link IRecipeExtrasBuilder#addScrollBoxWidget}.
 *
 * @since 19.8.0
 */
@ApiStatus.NonExtendable
public interface IScrollBoxWidget extends IRecipeWidget, IJivInputHandler {
	/**
	 * Get the width available for displaying contents in the scroll box.
	 * The scroll bar takes up some of the space, so this can be used in order to create accurately-sized contents.
	 *
	 * @since 19.18.9
	 */
	int getContentAreaWidth();

	/**
	 * Get the visible height for displaying contents in the scroll box.
	 * The actual height of the contents can be taller, because the box can scroll to show more.
	 *
	 * @since 19.18.9
	 */
	int getContentAreaHeight();

	/**
	 * Set the contents to display inside the scroll box.
	 * The drawable width should match {@link #getContentAreaWidth()}, and the height can be any height.
	 *
	 * @since 19.18.9
	 */
	IScrollBoxWidget setContents(IDrawable contents);

	/**
	 * Display text in the scroll box.
	 * Text will be automatically wrapped in order to fit inside of {@link #getContentAreaWidth()}.
	 *
	 * @since 19.18.9
	 */
	IScrollBoxWidget setContents(List<FormattedText> text);
}
