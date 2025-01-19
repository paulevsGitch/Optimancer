package paulevs.optimancer.mixin.common;

import net.minecraft.level.dimension.DimensionData;
import net.minecraft.level.dimension.DimensionDataHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionDataHandler.class)
public interface DimensionDataHandlerAccessor {
	@Accessor("dimensionData")
	DimensionData optimancer_getDimensionData();
}
