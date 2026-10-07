package eakerzt.jiv.library.recipes;

import eakerzt.jiv.api.recipe.transfer.IRecipeTransferError;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferContext;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandler;
import eakerzt.jiv.api.recipe.transfer.IUniversalRecipeTransferHandler;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.common.Constants;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class UniversalRecipeTransferHandlerAdapter<C extends AbstractContainerMenu, R> implements IRecipeTransferHandler<C, R> {
	private final IUniversalRecipeTransferHandler<C> universalRecipeTransferHandler;

	public UniversalRecipeTransferHandlerAdapter(IUniversalRecipeTransferHandler<C> universalRecipeTransferHandler) {
		this.universalRecipeTransferHandler = universalRecipeTransferHandler;
	}

	@Override
	public Class<? extends C> getContainerClass() {
		return universalRecipeTransferHandler.getContainerClass();
	}

	@Override
	public Optional<MenuType<C>> getMenuType() {
		return universalRecipeTransferHandler.getMenuType();
	}

	@Override
	public IRecipeType<R> getRecipeType() {
		@SuppressWarnings("unchecked")
		IRecipeType<R> cast = (IRecipeType<R>) Constants.UNIVERSAL_RECIPE_TRANSFER_TYPE;
		return cast;
	}

	@Override
	public @Nullable IRecipeTransferError transferRecipe(IRecipeTransferContext<R, C> context, boolean doTransfer) {
		return universalRecipeTransferHandler.transferRecipe(
			context.getContainer(),
			context.getRecipe(),
			context.getRecipeSlots(),
			context.getPlayer(),
			context.isMaxTransfer(),
			doTransfer
		);
	}
}
