package paulevs.optimancer.helper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public class GameHelper {
	@Environment(EnvType.CLIENT)
	@SuppressWarnings("deprecation")
	public static Minecraft getMinecraft() {
		return (Minecraft) FabricLoader.getInstance().getGameInstance();
	}
	
	@Environment(EnvType.SERVER)
	@SuppressWarnings("deprecation")
	public static MinecraftServer getServer() {
		return (MinecraftServer) FabricLoader.getInstance().getGameInstance();
	}
}
