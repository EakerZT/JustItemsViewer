package eakerzt.jiv.library.startup;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.library.config.JivConfigData;

import java.util.List;

public record StartData(
	List<IModPlugin> plugins,
	IConnectionToServer serverConnection,
	JivConfigData configData
) {
}
