package eakerzt.jiv.library.ingredients;

import eakerzt.jiv.api.ingredients.ISlotDisplayInterpretationBuilder;
import eakerzt.jiv.common.util.ErrorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

final class SlotDisplayInterpretationBuilder<T> implements ISlotDisplayInterpretationBuilder<T> {
	private SlotDisplayInfo.Value<Boolean> wildcardForSubtypes = SlotDisplayInfo.Value.unspecified();
	private SlotDisplayInfo.Value<TagKey<?>> tagKey = SlotDisplayInfo.Value.unspecified();
	private SlotDisplayInfo.Value<Component> tooltipHeader = SlotDisplayInfo.Value.unspecified();
	private final List<ChildDisplay<T>> childDisplays = new ArrayList<>();
	private boolean childDisplaysSet;

	@Override
	public ISlotDisplayInterpretationBuilder<T> addChildDisplay(SlotDisplay childDisplay) {
		return addChildDisplay(childDisplay, UnaryOperator.identity());
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> addChildDisplay(
		SlotDisplay childDisplay,
		UnaryOperator<T> ingredientTransformer
	) {
		ErrorUtil.checkNotNull(childDisplay, "childDisplay");
		ErrorUtil.checkNotNull(ingredientTransformer, "ingredientTransformer");
		this.childDisplays.add(new ChildDisplay<>(childDisplay, ingredientTransformer));
		this.childDisplaysSet = true;
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> setWildcardForSubtypes(boolean wildcardForSubtypes) {
		this.wildcardForSubtypes = SlotDisplayInfo.Value.of(wildcardForSubtypes);
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> setTagKey(TagKey<?> tagKey) {
		ErrorUtil.checkNotNull(tagKey, "tagKey");
		this.tagKey = SlotDisplayInfo.Value.of(tagKey);
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> clearTagKey() {
		this.tagKey = SlotDisplayInfo.Value.empty();
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> setTooltipHeader(Component tooltipHeader) {
		ErrorUtil.checkNotNull(tooltipHeader, "tooltipHeader");
		this.tooltipHeader = SlotDisplayInfo.Value.of(tooltipHeader);
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> clearTooltipHeader() {
		this.tooltipHeader = SlotDisplayInfo.Value.empty();
		return this;
	}

	SlotDisplayInfo buildInfo() {
		return new SlotDisplayInfo(
			SlotDisplayInfo.Value.unspecified(),
			wildcardForSubtypes,
			tagKey,
			tooltipHeader
		);
	}

	List<ChildDisplay<T>> getChildDisplays() {
		return childDisplays;
	}

	boolean isChildDisplaysSet() {
		return childDisplaysSet;
	}

	record ChildDisplay<T>(SlotDisplay slotDisplay, UnaryOperator<T> ingredientTransformer) {
		ChildDisplay {
			ErrorUtil.checkNotNull(slotDisplay, "slotDisplay");
			ErrorUtil.checkNotNull(ingredientTransformer, "ingredientTransformer");
		}
	}
}
