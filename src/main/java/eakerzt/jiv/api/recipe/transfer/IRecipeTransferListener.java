package eakerzt.jiv.api.recipe.transfer;

import eakerzt.jiv.api.registration.IRecipeTransferRegistration;

/**
 * Observes recipe transfers requested through JIV.
 *
 * Register a listener with {@link IRecipeTransferRegistration#addRecipeTransferListener(IRecipeTransferListener)}.
 *
 * @since 29.35.0
 */
public interface IRecipeTransferListener {
	/**
	 * Called once when JIV attempts a recipe transfer, immediately before JIV invokes the selected transfer handler.
	 * This is not called while JIV is only checking whether a recipe can be transferred.
	 *
	 * @since 29.35.0
	 */
	default void beforeRecipeTransfer(IRecipeTransferContext<?, ?> context) {

	}

	/**
	 * Called after the selected recipe transfer handler reports that the transfer has finished.
	 * The context is the same one passed to {@link #beforeRecipeTransfer(IRecipeTransferContext)}.
	 *
	 * @since 29.35.0
	 */
	default void afterRecipeTransfer(IRecipeTransferContext<?, ?> context, RecipeTransferResult result) {

	}
}
