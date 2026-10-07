package eakerzt.jiv.neoforge.input;

import com.mojang.blaze3d.platform.InputConstants;
import eakerzt.jiv.common.input.keys.AbstractJivKeyMappingBuilder;
import eakerzt.jiv.common.input.keys.IJivKeyMappingBuilder;
import eakerzt.jiv.common.input.keys.IJivKeyMappingInternal;
import eakerzt.jiv.common.input.keys.JivKeyConflictContext;
import eakerzt.jiv.common.input.keys.JivKeyModifier;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class ForgeJivKeyMappingBuilder extends AbstractJivKeyMappingBuilder {
	private final KeyMapping.Category category;
	private final String description;
	private IKeyConflictContext keyConflictContext = KeyConflictContext.UNIVERSAL;
	private KeyModifier keyModifier = KeyModifier.NONE;

	public ForgeJivKeyMappingBuilder(KeyMapping.Category category, String description) {
		this.category = category;
		this.description = description;
	}

	@Override
	public IJivKeyMappingBuilder setContext(JivKeyConflictContext context) {
		this.keyConflictContext = switch (context) {
			case UNIVERSAL -> KeyConflictContext.UNIVERSAL;
			case GUI -> KeyConflictContext.GUI;
			case IN_GAME -> KeyConflictContext.IN_GAME;
			case JIV_GUI_HOVER -> JivForgeKeyConflictContexts.JIV_GUI_HOVER;
			case JIV_GUI_HOVER_BOOKMARK -> JivForgeKeyConflictContexts.JIV_GUI_HOVER_BOOKMARK;
			case JIV_GUI_HOVER_CHEAT_MODE -> JivForgeKeyConflictContexts.JIV_GUI_HOVER_CHEAT_MODE;
			case JIV_GUI_HOVER_CONFIG_BUTTON -> JivForgeKeyConflictContexts.JIV_GUI_HOVER_CONFIG_BUTTON;
			case JIV_GUI_HOVER_INGREDIENT -> JivForgeKeyConflictContexts.JIV_GUI_HOVER_INGREDIENT;
			case JIV_GUI_HOVER_SEARCH -> JivForgeKeyConflictContexts.JIV_GUI_HOVER_SEARCH;
			case JIV_GUI_FOCUSED_SEARCH -> JivForgeKeyConflictContexts.JIV_GUI_FOCUSED_SEARCH;
		};
		return this;
	}

	@Override
	public IJivKeyMappingBuilder setModifier(JivKeyModifier modifier) {
		this.keyModifier = switch (modifier) {
			case CONTROL -> KeyModifier.CONTROL;
			case CONTROL_OR_COMMAND -> KeyModifier.CONTROL_OR_COMMAND;
			case SHIFT -> KeyModifier.SHIFT;
			case ALT -> KeyModifier.ALT;
			case NONE -> KeyModifier.NONE;
		};
		return this;
	}

	@Override
	protected IJivKeyMappingInternal buildMouse(int mouseButton) {
		KeyMapping keyMapping = new KeyMapping(
			description,
			keyConflictContext,
			keyModifier,
			InputConstants.Type.MOUSE,
			mouseButton,
			category
		);
		return new NeoForgeJivKeyMapping(keyMapping);
	}

	@Override
	public IJivKeyMappingInternal buildKeyboardKey(int key) {
		KeyMapping keyMapping = new KeyMapping(
			description,
			keyConflictContext,
			keyModifier,
			InputConstants.Type.KEYSYM,
			key,
			category
		);
		return new NeoForgeJivKeyMapping(keyMapping);
	}
}
