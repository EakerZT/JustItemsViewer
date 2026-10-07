package example.jiv;

import eakerzt.jiv.config.api.Configs;
import eakerzt.jiv.config.api.schema.IConfigSchema;
import eakerzt.jiv.config.api.value.IConfigValue;

/** Construct once during mod initialization, while the JIV runtime is installed. */
public final class DemoConfig {
    public final IConfigValue<Boolean> enabled;
    public final IConfigValue<Integer> rows;
    public final IConfigSchema schema;

    public DemoConfig() {
        var builder = Configs.forMod("examplemod")
            .createClientSchemaBuilder("client.ini", "examplemod.config.client");
        var category = builder.addCategory("display");
        enabled = category.addBoolean("enabled", true).build();
        rows = category.addInteger("rows", 8, 1, 16).build();
        schema = builder.build();
    }
}
