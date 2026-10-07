package eakerzt.jiv.common.util;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.helpers.IStackHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.ingredients.subtypes.ISubtypeManager;
import eakerzt.jiv.api.ingredients.subtypes.UidContext;
import eakerzt.jiv.common.ingredients.TypedIngredientUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class StackHelper implements IStackHelper {
	private final ISubtypeManager subtypeManager;

	public StackHelper(ISubtypeManager subtypeManager) {
		this.subtypeManager = subtypeManager;
	}

	@Override
	public boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs, UidContext context) {
		ErrorUtil.checkNotNull(context, "context");
		if (lhs == rhs) {
			return true;
		}

		if (lhs == null || rhs == null) {
			return false;
		}

		if (lhs.getItem() != rhs.getItem()) {
			return false;
		}

		Object keyLhs = subtypeManager.getSubtypeData(lhs, context);
		Object keyRhs = subtypeManager.getSubtypeData(rhs, context);
		return Objects.equals(keyLhs, keyRhs);
	}

	@Override
	public Object getUidForStack(ItemStack stack, UidContext context) {
		Item item = stack.getItem();
		Object subtypeData = subtypeManager.getSubtypeData(stack, context);
		if (subtypeData != null) {
			return List.of(item, subtypeData);
		}
		return item;
	}

	@Override
	public Object getUidForStack(ITypedIngredient<ItemStack> typedIngredient, UidContext context) {
		ITypedIngredient<ItemStack> checkedIngredient = TypedIngredientUtil.checkTypedIngredientFromApi(typedIngredient);
		Item item = checkedIngredient.getBaseIngredient(VanillaTypes.ITEM_STACK);
		Object subtypeData = subtypeManager.getSubtypeData(VanillaTypes.ITEM_STACK, checkedIngredient, context);
		if (subtypeData != null) {
			return List.of(item, subtypeData);
		}
		return item;
	}

	public boolean hasSubtypes(ItemStack stack) {
		return subtypeManager.hasSubtypes(stack);
	}
}
