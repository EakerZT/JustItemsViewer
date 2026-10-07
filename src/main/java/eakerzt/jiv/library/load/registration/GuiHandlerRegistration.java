package eakerzt.jiv.library.load.registration;

import com.google.common.base.Preconditions;
import eakerzt.jiv.api.gui.handlers.IGhostIngredientHandler;
import eakerzt.jiv.api.gui.handlers.IGlobalGuiHandler;
import eakerzt.jiv.api.gui.handlers.IGuiContainerHandler;
import eakerzt.jiv.api.gui.handlers.IScreenHandler;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.registration.IGuiHandlerRegistration;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.common.collect.ListMultiMap;
import eakerzt.jiv.common.ingredients.ITypedIngredientFactory;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.library.gui.GuiContainerHandlers;
import eakerzt.jiv.library.gui.helpers.ScreenHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiHandlerRegistration implements IGuiHandlerRegistration {
	private final GuiContainerHandlers guiContainerHandlers = new GuiContainerHandlers();
	private final List<IGlobalGuiHandler> globalGuiHandlers = new ArrayList<>();
	private final Map<Class<?>, IScreenHandler<?>> guiScreenHandlers = new HashMap<>();
	private final ListMultiMap<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers = new ListMultiMap<>();
	private final IJivHelpers jivHelpers;

	public GuiHandlerRegistration(IJivHelpers jivHelpers) {
		this.jivHelpers = jivHelpers;
	}

	@Override
	public <T extends AbstractContainerScreen<?>> void addGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<T> guiHandler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		ErrorUtil.checkNotNull(guiHandler, "guiHandler");
		this.guiContainerHandlers.add(guiClass, guiHandler);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractContainerScreen<?>> void addGenericGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<?> guiHandler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		ErrorUtil.checkNotNull(guiHandler, "guiHandler");
		this.guiContainerHandlers.add(guiClass, (IGuiContainerHandler<? super T>) guiHandler);
	}

	@Override
	public void addGlobalGuiHandler(IGlobalGuiHandler globalGuiHandler) {
		ErrorUtil.checkNotNull(globalGuiHandler, "globalGuiHandler");
		this.globalGuiHandlers.add(globalGuiHandler);
	}

	@Override
	public <T extends Screen> void addGuiScreenHandler(Class<T> guiClass, IScreenHandler<T> handler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		Preconditions.checkArgument(Screen.class.isAssignableFrom(guiClass), "guiClass must inherit from Screen");
		Preconditions.checkArgument(!Screen.class.equals(guiClass), "you cannot add a handler for Screen, only a subclass.");
		ErrorUtil.checkNotNull(handler, "guiScreenHandler");
		this.guiScreenHandlers.put(guiClass, handler);
	}

	@Override
	public <T extends Screen> void addGenericGuiScreenHandler(Class<T> guiClass, IScreenHandler<?> handler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		ErrorUtil.checkNotNull(handler, "handler");
		this.guiScreenHandlers.put(guiClass, handler);
	}

	@Unmodifiable
	private static final List<Class<? extends Screen>> ghostIngredientGuiBlacklist = List.of(
		Screen.class, InventoryScreen.class, CreativeModeInventoryScreen.class
	);

	@Override
	public <T extends Screen> void addGhostIngredientHandler(Class<T> guiClass, IGhostIngredientHandler<T> handler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		Preconditions.checkArgument(Screen.class.isAssignableFrom(guiClass), "guiClass must inherit from Screen");
		Preconditions.checkArgument(!ghostIngredientGuiBlacklist.contains(guiClass), "you cannot add a ghost ingredient handler for the following Guis, it would interfere with using JIV: %s", ghostIngredientGuiBlacklist);
		ErrorUtil.checkNotNull(handler, "handler");
		this.ghostIngredientHandlers.put(guiClass, handler);
	}

	public IScreenHelper createGuiScreenHelper(ITypedIngredientFactory typedIngredientFactory) {
		return new ScreenHelper(typedIngredientFactory, globalGuiHandlers, guiContainerHandlers, ghostIngredientHandlers, guiScreenHandlers);
	}

	@Override
	public IJivHelpers getJivHelpers() {
		return jivHelpers;
	}
}
