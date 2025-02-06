package paulevs.optimancer.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.level.Level;
import net.minecraft.level.LevelMonsterSpawner;
import net.minecraft.util.maths.Vec2I;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import paulevs.optimancer.helper.GameHelper;

import java.util.Set;

@Mixin(LevelMonsterSpawner.class)
public class LevelMonsterSpawnerMixin {
	@SuppressWarnings("rawtypes")
	@WrapOperation(method = "spawnEntities", at = @At(
		value = "INVOKE",
		target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"
	))
	private static boolean optimancer_removeUnloaded(
		Set loadedChunkPositions, Object objVector, Operation<Boolean> original, @Local(argsOnly = true) Level level
	) {
		Vec2I pos = (Vec2I) objVector;
		if (GameHelper.isInvalidChunk(level, pos.x, pos.z)) return false;
		return original.call(loadedChunkPositions, objVector);
	}
}
