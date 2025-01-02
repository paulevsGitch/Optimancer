package paulevs.optimancer.mixin.client;

import net.minecraft.client.util.ScreenshotManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import paulevs.optimancer.thread.ScreenshotThread;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;

@Mixin(ScreenshotManager.class)
public class ScreenshotManagerMixin {
	@Shadow private static ByteBuffer screenshotBuffer;
	@Shadow private static DateFormat dateFormat;
	
	/**
	 * @author paulevs
	 * @reason Remove try/catch block and optimize method.
	 */
	@Overwrite
	public static String takeScreenshot(File directory, int width, int height) {
		int capacity = width * height * 3;
		
		if (screenshotBuffer == null || screenshotBuffer.capacity() != capacity) {
			screenshotBuffer = BufferUtils.createByteBuffer(capacity);
		}
		
		GL11.glPixelStorei(3333, 1);
		GL11.glPixelStorei(3317, 1);
		screenshotBuffer.position(0);
		GL11.glReadPixels(0, 0, width, height, 6407, 5121, screenshotBuffer);
		
		String name = dateFormat.format(new Date());
		byte[] pixelData = new byte[capacity];
		screenshotBuffer.position(0);
		screenshotBuffer.get(pixelData);
		
		ScreenshotThread.addScreenshot(pixelData, width, height, directory, name);
		
		return null;
	}
}
