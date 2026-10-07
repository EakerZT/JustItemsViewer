package eakerzt.jiv.common.gui;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.helpers.IModIdHelper;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.ingredients.subtypes.UidContext;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IJivKeyMapping;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.DebugConfig;
import eakerzt.jiv.common.ingredients.TypedIngredientUtil;
import eakerzt.jiv.common.platform.IPlatformRenderHelper;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.common.util.ErrorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JivTooltip implements ITooltipBuilder {
	private final List<Either<FormattedText, TooltipComponent>> lines = new ArrayList<>();
	private final List<Either<FormattedText, TooltipComponent>> ingredientTooltipFooter = new ArrayList<>();
	private @Nullable ITypedIngredient<?> typedIngredient;

	public record TooltipRenderData(Font font, ItemStack itemStack) {
	}

	@Override
	public void add(@Nullable FormattedText formattedText) {
		if (formattedText == null) {
			if (Services.PLATFORM.getModHelper().isInDev()) {
				throw new NullPointerException("Tried to add null tooltip text");
			}
			return;
		}
		lines.add(Either.left(formattedText));
	}

	@Override
	public void add(@Nullable TooltipComponent component) {
		if (component == null) {
			if (Services.PLATFORM.getModHelper().isInDev()) {
				throw new NullPointerException("Tried to add null tooltip component");
			}
			return;
		}
		lines.add(Either.right(component));
	}

	@Override
	public void setIngredient(ITypedIngredient<?> typedIngredient) {
		this.typedIngredient = TypedIngredientUtil.checkTypedIngredientFromApi(typedIngredient);
	}

	@Override
	public void addKeyUsageComponent(String translationKey, IJivKeyMapping keyMapping) {
		add(createKeyUsageComponent(translationKey, keyMapping));
	}

	public void addKeyUsageComponent(String translationKey, MutableComponent keyMapping) {
		add(createKeyUsageComponent(translationKey, keyMapping));
	}

	private static MutableComponent createKeyUsageComponent(String translationKey, IJivKeyMapping keyMapping) {
		return createKeyUsageComponent(translationKey, keyMapping.getTranslatedKeyMessage().copy());
	}

	private static MutableComponent createKeyUsageComponent(String translationKey, MutableComponent keyMapping) {
		Component boldKeyMapping = keyMapping.withStyle(ChatFormatting.BOLD);
		return Component.translatable(translationKey, boldKeyMapping)
			.withStyle(ChatFormatting.ITALIC)
			.withStyle(ChatFormatting.GRAY);
	}

	@Override
	public void addAll(Collection<? extends FormattedText> components) {
		for (FormattedText component : components) {
			add(component);
		}
	}

	@Override
	public void clearIngredient() {
		this.typedIngredient = null;
	}

	@Override
	public List<Either<FormattedText, TooltipComponent>> getLines() {
		return lines;
	}

	public void addAll(JivTooltip tooltip) {
		lines.addAll(tooltip.lines);
		ingredientTooltipFooter.addAll(tooltip.ingredientTooltipFooter);
	}

	public void addIngredientTooltipFooter(JivTooltip tooltip) {
		ingredientTooltipFooter.addAll(tooltip.lines);
		ingredientTooltipFooter.addAll(tooltip.ingredientTooltipFooter);
	}

	public boolean isEmpty() {
		return lines.isEmpty() && ingredientTooltipFooter.isEmpty() && typedIngredient == null;
	}

	@Override
	public String toString() {
		return lines.stream()
			.map(e -> e.map(
				FormattedText::getString,
				Object::toString
			))
			.collect(Collectors.joining("\n", "[\n", "\n]"));
	}

	public void draw(GuiGraphicsExtractor guiGraphics, int x, int y) {
		draw(guiGraphics, x, y, null);
	}

	public void draw(GuiGraphicsExtractor guiGraphics, int x, int y, @Nullable ClientTooltipPositioner positioner) {
		if (typedIngredient != null) {
			draw(guiGraphics, x, y, typedIngredient, positioner);
			return;
		}
		if (isEmpty()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		try {
			renderTooltip(renderHelper, guiGraphics, x, y, font, ItemStack.EMPTY, positioner);
		} catch (RuntimeException e) {
			throw new RuntimeException("Crashed when rendering tooltip:\n" + this, e);
		}
	}

	private <T> void draw(
		GuiGraphicsExtractor guiGraphics,
		int x,
		int y,
		ITypedIngredient<T> typedIngredient,
		@Nullable ClientTooltipPositioner positioner
	) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientManager ingredientManager = Internal.getJivRuntime().getIngredientManager();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		draw(guiGraphics, x, y, typedIngredient, ingredientRenderer, ingredientManager, positioner);
	}

	public <T> void draw(
		GuiGraphicsExtractor guiGraphics,
		int x,
		int y,
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientManager ingredientManager
	) {
		draw(guiGraphics, x, y, typedIngredient, ingredientRenderer, ingredientManager, null);
	}

	private <T> void draw(
		GuiGraphicsExtractor guiGraphics,
		int x,
		int y,
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientManager ingredientManager,
		@Nullable ClientTooltipPositioner positioner
	) {
		TooltipRenderData renderData = prepareForIngredientTooltip(typedIngredient, ingredientRenderer, ingredientManager);
		if (isEmpty()) {
			return;
		}
		try {
			IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
			renderTooltip(renderHelper, guiGraphics, x, y, renderData.font(), renderData.itemStack(), positioner);
		} catch (RuntimeException e) {
			CrashReport crashReport = ErrorUtil.createIngredientCrashReport(e, "Rendering ingredient tooltip", ingredientManager, typedIngredient);
			crashReport.addCategory("tooltip")
				.setDetail("value", this);
			throw new ReportedException(crashReport);
		}
	}

	private void renderTooltip(
		IPlatformRenderHelper renderHelper,
		GuiGraphicsExtractor guiGraphics,
		int x,
		int y,
		Font font,
		ItemStack itemStack,
		@Nullable ClientTooltipPositioner positioner
	) {
		if (positioner == null) {
			renderHelper.renderTooltip(guiGraphics, lines, x, y, font, itemStack);
		} else {
			renderHelper.renderTooltip(guiGraphics, lines, x, y, font, itemStack, positioner);
		}
	}

	public <T> TooltipRenderData prepareForIngredientTooltip(
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientManager ingredientManager
	) {
		Minecraft minecraft = Minecraft.getInstance();
		T ingredient = typedIngredient.getIngredient();
		Font font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = typedIngredient.getItemStack().orElse(ItemStack.EMPTY);

		itemStack.getTooltipImage()
			.ifPresent((c) -> {
				int index = Math.min(1, lines.size());
				lines.add(index, Either.right(c));
			});

		addDebugInfo(ingredientManager, typedIngredient);
		lines.addAll(ingredientTooltipFooter);
		ingredientTooltipFooter.clear();

		IJivHelpers jivHelpers = Internal.getJivRuntime().getJivHelpers();
		IModIdHelper modIdHelper = jivHelpers.getModIdHelper();
		modIdHelper.getModNameForTooltip(typedIngredient)
			.ifPresent(this::add);

		return new TooltipRenderData(font, itemStack);
	}

	private <T> void addDebugInfo(IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		if (!DebugConfig.isDebugInfoTooltipsEnabled() || !Minecraft.getInstance().options.advancedItemTooltips) {
			return;
		}
		T ingredient = typedIngredient.getIngredient();
		IIngredientType<T> type = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);
		Codec<T> ingredientCodec = ingredientManager.getIngredientCodec(type);

		add(Component.empty());
		add(
			Component.literal("JIV Debug:")
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		add(
			Component.literal("• type: " + ingredientHelper.getIngredientType().getUid())
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		String hasSubtypes = Boolean.toString(ingredientHelper.hasSubtypes(ingredient));
		add(
			Component.literal("• has subtypes: " + hasSubtypes)
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		add(
			Component.literal("• uid: " + ingredientHelper.getUid(ingredient, UidContext.Ingredient))
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		try {
			Minecraft minecraft = Minecraft.getInstance();
			ClientLevel level = minecraft.level;
			assert level != null;
			RegistryAccess registryAccess = level.registryAccess();
			RegistryOps<JsonElement> registryOps = registryAccess.createSerializationContext(JsonOps.INSTANCE);
			String jsonResult = ingredientCodec.encodeStart(registryOps, ingredient)
				.mapOrElse(
					JsonElement::toString,
					DataResult.Error::message
				);
			add(
				Component.literal("• json: " + jsonResult)
					.withStyle(ChatFormatting.DARK_GRAY)
			);
		} catch (RuntimeException e) {
			add(
				Component.literal("• json crashed: " + e.getMessage())
					.withStyle(ChatFormatting.DARK_RED)
			);
		}
		add(
			Component.literal("• extra info: " + ingredientHelper.getErrorInfo(ingredient))
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		add(Component.empty());
	}
}
