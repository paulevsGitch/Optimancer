package paulevs.optimancer.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.InGame;
import net.minecraft.client.util.ScreenshotManager;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.optimancer.thread.ScreenshotThread;
import paulevs.optimancer.thread.ThreadManager;

import java.io.File;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow private static File gameDirectory;
	@Shadow boolean isTakingScreenshot;
	@Shadow public int actualHeight;
	@Shadow public int actualWidth;
	@Shadow public InGame overlay;
	
	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 10485760))
	private static int optimancer_removeUnusedData(int original) {
		return 0;
	}
	
	@Inject(method = "init", at = @At("HEAD"))
	private void optimancer_onMinecraftInit(CallbackInfo info) {
		ThreadManager.init();
	}
	
	@Inject(method = "scheduleStop", at = @At("HEAD"))
	private void optimancer_onMinecraftStop(CallbackInfo info) {
		ThreadManager.stop();
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void optimancer_tickClient(CallbackInfo info) {
		ThreadManager.tickClient(Minecraft.class.cast(this));
	}
	
	@Inject(method = "checkTakingScreenshot", at = @At("HEAD"), cancellable = true)
	private void optimancer_checkTakingScreenshot(CallbackInfo info) {
		info.cancel();
		String message = ScreenshotThread.getMessage();
		if (message != null) overlay.addChatMessage(message);
		
		if (Keyboard.isKeyDown(60)) {
			if (isTakingScreenshot) return;
			isTakingScreenshot = true;
			ScreenshotManager.takeScreenshot(gameDirectory, this.actualWidth, this.actualHeight);
		}
		else isTakingScreenshot = false;
	}
}
