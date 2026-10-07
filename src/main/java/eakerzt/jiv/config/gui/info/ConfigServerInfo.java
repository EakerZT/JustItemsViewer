package eakerzt.jiv.config.gui.info;

import eakerzt.jiv.config.api.schema.ConfigSchemaType;
import eakerzt.jiv.config.api.schema.IConfigSchema;
import eakerzt.jiv.config.gui.ConfigScreenSchema;
import eakerzt.jiv.config.gui.ConfigValueAccess;
import eakerzt.jiv.config.gui.api.ConfigInfo;
import eakerzt.jiv.config.gui.api.IConfigScreenValue;
import eakerzt.jiv.config.gui.remote.RemoteConfigEditor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Resolves server contexts once, then reads their live access state for the info panel.
 */
public final class ConfigServerInfo {
	private final ConfigScreenSchema schema;
	private final Map<IConfigSchema, Supplier<ServerConfigAccess>> schemaAccess = new IdentityHashMap<>();
	private final Map<Object, Optional<Supplier<ServerConfigAccess>>> valueAccess = new IdentityHashMap<>();

	public ConfigServerInfo(ConfigScreenSchema schema) {
		this.schema = schema;
	}

	public Supplier<List<Component>> forValues(Stream<? extends IConfigScreenValue<?>> values) {
		Set<Supplier<ServerConfigAccess>> providers = new LinkedHashSet<>();
		values.forEach(value -> valueAccess.computeIfAbsent(value.getIdentityKey(), ignored -> findAccess(value)).ifPresent(providers::add));
		if (providers.isEmpty()) {
			return List::of;
		}
		List<Supplier<ServerConfigAccess>> contexts = List.copyOf(providers);
		return () -> contexts.stream().map(Supplier::get).distinct().map(ServerConfigAccess::getDescription).toList();
	}

	private Optional<Supplier<ServerConfigAccess>> findAccess(IConfigScreenValue<?> value) {
		Optional<IConfigSchema> backingSchema = schema.findBackingSchema(value)
			.filter(schema -> schema.getType() == ConfigSchemaType.SERVER);
		if (backingSchema.isPresent()) {
			return Optional.of(schemaAccess.computeIfAbsent(backingSchema.get(),
				schema -> () -> RemoteConfigEditor.getInstance().getServerAccess(schema)));
		}
		if (value.getIdentityKey() instanceof ConfigValueAccess access) {
			return access.getServerAccess();
		}
		return Optional.empty();
	}

	public static ConfigInfo add(ConfigInfo info, List<Component> accessDescriptions) {
		if (accessDescriptions.isEmpty()) {
			return info;
		}
		List<Component> lines = new ArrayList<>(accessDescriptions);
		lines.addAll(info.lines());
		return new ConfigInfo(info.title(), lines);
	}
}
