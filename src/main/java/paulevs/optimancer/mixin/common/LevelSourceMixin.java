package paulevs.optimancer.mixin.common;

import net.minecraft.level.source.LevelSource;
import org.spongepowered.asm.mixin.Mixin;
import paulevs.optimancer.world.OptimancerLevelSource;

@Mixin(LevelSource.class)
public interface LevelSourceMixin extends OptimancerLevelSource {}
