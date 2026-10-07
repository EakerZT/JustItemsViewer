package eakerzt.jiv.neoforge.input;

import eakerzt.jiv.common.input.keys.IJivKeyMappingBuilder;
import eakerzt.jiv.common.input.keys.IJivKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;

public class ForgeJivKeyMappingCategoryBuilder implements IJivKeyMappingCategoryBuilder {
	private final KeyMapping.Category category;

	public ForgeJivKeyMappingCategoryBuilder(KeyMapping.Category category) {
		this.category = category;
	}

	@Override
	public IJivKeyMappingBuilder createMapping(String description) {
		return new ForgeJivKeyMappingBuilder(category, description);
	}
}
