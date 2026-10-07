package eakerzt.jiv.test;

import com.mojang.serialization.Codec;
import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.subtypes.UidContext;
import eakerzt.jiv.api.registration.IModIngredientRegistration;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.library.ingredients.subtypes.SubtypeInterpreters;
import eakerzt.jiv.library.ingredients.subtypes.SubtypeManager;
import eakerzt.jiv.library.load.registration.IngredientManagerBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IngredientTypeRegistrationTest {
	private static final IIngredientType<String> STRING_TYPE = () -> String.class;
	private static final IIngredientType<Integer> INTEGER_TYPE = () -> Integer.class;

	@Test
	void rejectedDuplicateDoesNotReplaceTheRegisteringPlugin() {
		IngredientManagerBuilder builder = createBuilder();
		IModPlugin firstPlugin = plugin("first", registration -> registerStrings(registration, List.of()));
		builder.registerIngredients(firstPlugin);

		assertThrows(IllegalArgumentException.class, () -> builder.registerIngredients(
			plugin("duplicate", registration -> registerStrings(registration, List.of()))
		));

		assertEquals(Optional.of(firstPlugin.getPluginUid()), builder.build().getRegisteringPluginUid(STRING_TYPE));
	}

	@Test
	void failedPluginDoesNotLeakItsIdentityIntoLaterRegistrations() {
		IngredientManagerBuilder builder = createBuilder();
		IModPlugin failedPlugin = plugin("failed", registration -> {
			registerStrings(registration, List.of());
			throw new IllegalStateException("plugin failed after registration");
		});
		assertThrows(IllegalStateException.class, () -> builder.registerIngredients(failedPlugin));
		registerIntegers(builder);

		IIngredientManager manager = builder.build();
		assertEquals(Optional.of(failedPlugin.getPluginUid()), manager.getRegisteringPluginUid(STRING_TYPE));
		assertEquals(Optional.empty(), manager.getRegisteringPluginUid(INTEGER_TYPE));
	}

	private static IngredientManagerBuilder createBuilder() {
		return new IngredientManagerBuilder(
			new SubtypeManager(new SubtypeInterpreters()),
			DummyColorHelper.INSTANCE,
			new ContextMap.Builder().create(new ContextKeySet.Builder().build())
		);
	}

	private static IModPlugin plugin(String modId, Consumer<IModIngredientRegistration> register) {
		return new IModPlugin() {
			@Override
			public Identifier getPluginUid() {
				return Identifier.fromNamespaceAndPath(modId, "jiv");
			}

			@Override
			public void registerIngredients(IModIngredientRegistration registration) {
				register.accept(registration);
			}
		};
	}

	private static void registerStrings(IModIngredientRegistration registration, List<String> ingredients) {
		registration.register(STRING_TYPE, ingredients, new TestHelper<>(STRING_TYPE), new TestRenderer<>(), Codec.STRING);
	}

	private static void registerIntegers(IModIngredientRegistration registration) {
		registration.register(INTEGER_TYPE, List.of(), new TestHelper<>(INTEGER_TYPE), new TestRenderer<>(), Codec.INT);
	}

	private record TestHelper<T>(IIngredientType<T> type) implements IIngredientHelper<T> {
		@Override
		public IIngredientType<T> getIngredientType() {
			return type;
		}

		@Override
		public String getDisplayName(T ingredient) {
			return ingredient.toString();
		}

		@Override
		public Object getUid(T ingredient, UidContext context) {
			return ingredient.toString();
		}

		@Override
		public Identifier getIdentifier(T ingredient) {
			return Identifier.fromNamespaceAndPath("ingredient_content_mod", ingredient.toString());
		}

		@Override
		public T copyIngredient(T ingredient) {
			return ingredient;
		}

		@Override
		public String getErrorInfo(@Nullable T ingredient) {
			return String.valueOf(ingredient);
		}
	}

	private static class TestRenderer<T> implements IIngredientRenderer<T> {
		@Override
		public void render(GuiGraphicsExtractor guiGraphics, T ingredient) {
		}

		@Override
		public List<Component> getTooltip(T ingredient, net.minecraft.world.item.Item.TooltipContext tooltipContext, net.minecraft.world.entity.player.Player player, TooltipFlag tooltipFlag) {
			return List.of();
		}
	}
}
