package eakerzt.jiv.common.input.keys;

public interface IJivKeyMappingBuilder {
	IJivKeyMappingBuilder setContext(JivKeyConflictContext context);
	IJivKeyMappingBuilder setModifier(JivKeyModifier modifier);

	IJivKeyMappingInternal buildMouseLeft();
	IJivKeyMappingInternal buildMouseRight();
	IJivKeyMappingInternal buildMouseMiddle();
	IJivKeyMappingInternal buildKeyboardKey(int key);
	IJivKeyMappingInternal buildUnbound();
}
