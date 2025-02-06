package paulevs.optimancer.helper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.source.LevelSource;
import net.minecraft.server.MinecraftServer;
import paulevs.optimancer.world.NullChunk;
import paulevs.optimancer.world.PromiseChunk;

public class GameHelper {
	@Environment(EnvType.CLIENT)
	@SuppressWarnings("deprecation")
	public static Minecraft getClient() {
		return (Minecraft) FabricLoader.getInstance().getGameInstance();
	}
	
	@Environment(EnvType.SERVER)
	@SuppressWarnings("deprecation")
	public static MinecraftServer getServer() {
		return (MinecraftServer) FabricLoader.getInstance().getGameInstance();
	}
	
	public static boolean isInvalidChunk(Level level, int x, int z) {
		LevelSource source = level.getCache();
		if (!source.isChunkLoaded(x, z)) return true;
		Chunk chunk = source.getChunk(x, z);
		return chunk == null || isInvalidChunk(chunk);
	}
	
	public static boolean isInvalidChunk(Chunk chunk) {
		return chunk instanceof PromiseChunk || chunk instanceof NullChunk;
	}
}
