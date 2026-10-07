package eakerzt.jiv.test.lib;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.JivPlugin;
import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.api.registration.IModIngredientRegistration;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;

@JivPlugin
public class TestPlugin implements IModPlugin {
	public static final int BASE_INGREDIENT_COUNT = 2;

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(ModIds.JIV_ID, "test");
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		Collection<TestIngredient> baseTestIngredients = new ArrayList<>();
		for (int i = 0; i < BASE_INGREDIENT_COUNT; i++) {
			baseTestIngredients.add(new TestIngredient(i));
		}

		registration.register(TestIngredient.TYPE, baseTestIngredients, new TestIngredientHelper(), new TestIngredientRenderer(), TestIngredient.CODEC);
	}

}
