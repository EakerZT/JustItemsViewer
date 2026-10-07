/**
 * Inspect and atomically update config schemas.
 * <p>
 * {@link IConfigSchema} is the main runtime interface. Declare schemas through {@link IConfigSchemaBuilder schema
 * builders}, and use the child packages for category discovery and batch updates.
 */
@NullMarked
package eakerzt.jiv.config.api.schema;

import eakerzt.jiv.config.api.schema.builder.IConfigSchemaBuilder;
import org.jspecify.annotations.NullMarked;
