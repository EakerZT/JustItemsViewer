package eakerzt.jiv.common.transfer;

import eakerzt.jiv.api.recipe.transfer.IRecipeTransferError;

public class RecipeTransferErrorInternal implements IRecipeTransferError {
	public static final RecipeTransferErrorInternal INSTANCE = new RecipeTransferErrorInternal();

	private RecipeTransferErrorInternal() {

	}

	@Override
	public Type getType() {
		return Type.INTERNAL;
	}
}
