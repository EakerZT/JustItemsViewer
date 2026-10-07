package eakerzt.jiv.library.plugins.vanilla.brewing;

import eakerzt.jiv.api.recipe.vanilla.IJivBrewingRecipe;
import eakerzt.jiv.library.plugins.vanilla.ingredients.subtypes.PotionSubtypeInterpreter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class JivBrewingRecipe implements IJivBrewingRecipe {
	private final List<ItemStack> ingredients;
	private final List<ItemStack> potionInputs;
	private final ItemStack potionOutput;
	private final Identifier uid;
	private final BrewingRecipeUtil brewingRecipeUtil;

	public JivBrewingRecipe(
		List<ItemStack> ingredients,
		List<ItemStack> potionInputs,
		ItemStack potionOutput,
		Identifier uid,
		BrewingRecipeUtil brewingRecipeUtil
	) {
		this.ingredients = List.copyOf(ingredients);
		this.potionInputs = List.copyOf(potionInputs);
		this.potionOutput = potionOutput;
		this.uid = uid;
		this.brewingRecipeUtil = brewingRecipeUtil;

		brewingRecipeUtil.addRecipe(potionInputs, potionOutput);
	}

	@Override
	public List<ItemStack> getPotionInputs() {
		return potionInputs;
	}

	@Override
	public List<ItemStack> getIngredients() {
		return ingredients;
	}

	@Override
	public ItemStack getPotionOutput() {
		return potionOutput;
	}

	@Override
	public Identifier getUid() {
		return uid;
	}

	@Override
	public int getBrewingSteps() {
		return brewingRecipeUtil.getBrewingSteps(potionOutput);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JivBrewingRecipe other)) {
			return false;
		}

		return uid.equals(other.uid);
	}

	@Override
	public int hashCode() {
		return uid.hashCode();
	}

	@Override
	public String toString() {
		ItemStack input = potionInputs.getFirst();
		String inputName = PotionSubtypeInterpreter.INSTANCE.getStringName(input);
		String outputName = PotionSubtypeInterpreter.INSTANCE.getStringName(potionOutput);
		return ingredients + " + [" + input.getItem() + " " + inputName + "] = [" + potionOutput + " " + outputName + "]";
	}
}
