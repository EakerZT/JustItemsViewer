package eakerzt.jiv.library.plugins.vanilla.cooking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eakerzt.jiv.library.recipes.RecipeSerializers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public class JivSmeltingRecipe extends SmeltingRecipe {
	public static final MapCodec<JivSmeltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Ingredient.CODEC.fieldOf("ingredient").forGetter(JivSmeltingRecipe::input),
			SlotDisplay.CODEC.optionalFieldOf("fuel", SlotDisplay.AnyFuel.INSTANCE).forGetter(JivSmeltingRecipe::fuel),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(JivSmeltingRecipe::result),
			Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(JivSmeltingRecipe::experience),
			Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(JivSmeltingRecipe::cookingTime)
		)
		.apply(instance, JivSmeltingRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, JivSmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC,
		JivSmeltingRecipe::input,
		SlotDisplay.STREAM_CODEC,
		JivSmeltingRecipe::fuel,
		ItemStackTemplate.STREAM_CODEC,
		JivSmeltingRecipe::result,
		ByteBufCodecs.FLOAT,
		JivSmeltingRecipe::experience,
		ByteBufCodecs.VAR_INT,
		JivSmeltingRecipe::cookingTime,
		JivSmeltingRecipe::new
	);
	public static final RecipeSerializer<JivSmeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	private final SlotDisplay fuel;

	public JivSmeltingRecipe(Ingredient input, SlotDisplay fuel, ItemStackTemplate result, float experience, int cookingTime) {
		super(
			new Recipe.CommonInfo(false),
			new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, ""),
			input,
			result,
			experience,
			cookingTime
		);
		this.fuel = fuel;
	}

	public SlotDisplay fuel() {
		return fuel;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(
			new FurnaceRecipeDisplay(
				input().display(),
				fuel,
				new SlotDisplay.ItemStackSlotDisplay(result()),
				new SlotDisplay.ItemSlotDisplay(Items.FURNACE),
				cookingTime(),
				experience()
			)
		);
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public RecipeSerializer<SmeltingRecipe> getSerializer() {
		return (RecipeSerializer) RecipeSerializers.getJivSmeltingRecipeSerializer();
	}
}
