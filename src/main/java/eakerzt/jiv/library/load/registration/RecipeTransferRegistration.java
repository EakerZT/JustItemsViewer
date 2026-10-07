package eakerzt.jiv.library.load.registration;

import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.helpers.IStackHelper;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandler;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandlerHelper;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferInfo;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferListener;
import eakerzt.jiv.api.recipe.transfer.IUniversalRecipeTransferHandler;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.registration.IRecipeTransferRegistration;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.common.collect.Table;
import eakerzt.jiv.library.recipes.RecipeTransferManager;
import eakerzt.jiv.library.recipes.UniversalRecipeTransferHandlerAdapter;
import eakerzt.jiv.library.transfer.BasicRecipeTransferHandler;
import eakerzt.jiv.library.transfer.BasicRecipeTransferInfo;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipeTransferRegistration implements IRecipeTransferRegistration {
	private final Table<Class<? extends AbstractContainerMenu>, IRecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers = Table.hashBasedTable();
	private final List<IRecipeTransferListener> recipeTransferListeners = new ArrayList<>();
	private final IStackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IJivHelpers jivHelpers;
	private final IConnectionToServer serverConnection;

	public RecipeTransferRegistration(
		IStackHelper stackHelper,
		IRecipeTransferHandlerHelper handlerHelper,
		IJivHelpers jivHelpers,
		IConnectionToServer serverConnection
	) {
		this.stackHelper = stackHelper;
		this.handlerHelper = handlerHelper;
		this.jivHelpers = jivHelpers;
		this.serverConnection = serverConnection;
	}

	@Override
	public IJivHelpers getJivHelpers() {
		return jivHelpers;
	}

	@Override
	public IRecipeTransferHandlerHelper getTransferHelper() {
		return handlerHelper;
	}

	@Override
	public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(Class<? extends C> containerClass, @Nullable MenuType<C> menuType, IRecipeType<R> recipeType, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		ErrorUtil.checkNotNull(containerClass, "containerClass");
		ErrorUtil.checkNotNull(recipeType, "recipeType");

		IRecipeTransferInfo<C, R> recipeTransferInfo = new BasicRecipeTransferInfo<>(containerClass, menuType, recipeType, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
		addRecipeTransferHandler(recipeTransferInfo);
	}

	@Override
	public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferInfo<C, R> recipeTransferInfo) {
		ErrorUtil.checkNotNull(recipeTransferInfo, "recipeTransferInfo");

		IRecipeTransferHandler<C, R> recipeTransferHandler = new BasicRecipeTransferHandler<>(serverConnection, stackHelper, handlerHelper, recipeTransferInfo);
		addRecipeTransferHandler(recipeTransferHandler, recipeTransferInfo.getRecipeType());
	}

	@Override
	public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler, IRecipeType<R> recipeType) {
		ErrorUtil.checkNotNull(recipeTransferHandler, "recipeTransferHandler");
		ErrorUtil.checkNotNull(recipeType, "recipeType");

		Class<? extends C> containerClass = recipeTransferHandler.getContainerClass();
		this.recipeTransferHandlers.put(containerClass, recipeType, recipeTransferHandler);
	}

	@Override
	public <C extends AbstractContainerMenu> void addUniversalRecipeTransferHandler(IUniversalRecipeTransferHandler<C> universalRecipeTransferHandler) {
		ErrorUtil.checkNotNull(universalRecipeTransferHandler, "universalRecipeTransferHandler");

		Class<? extends C> containerClass = universalRecipeTransferHandler.getContainerClass();
		UniversalRecipeTransferHandlerAdapter<C, ?> adapter = new UniversalRecipeTransferHandlerAdapter<>(universalRecipeTransferHandler);
		this.recipeTransferHandlers.put(containerClass, adapter.getRecipeType(), adapter);
	}

	@Override
	public void addRecipeTransferListener(IRecipeTransferListener recipeTransferListener) {
		ErrorUtil.checkNotNull(recipeTransferListener, "recipeTransferListener");
		this.recipeTransferListeners.add(recipeTransferListener);
	}

	public RecipeTransferManager createRecipeTransferManager() {
		return new RecipeTransferManager(recipeTransferHandlers.toImmutable(), recipeTransferListeners);
	}
}
