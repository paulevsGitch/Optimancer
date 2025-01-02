package paulevs.optimancer.helper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class ClientHelper {
	public static Minecraft getMinecraft() {
		//noinspection deprecation
		return (Minecraft) FabricLoader.getInstance().getGameInstance();
	}
}
