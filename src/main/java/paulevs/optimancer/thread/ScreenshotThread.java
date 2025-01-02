package paulevs.optimancer.thread;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Environment(EnvType.CLIENT)
public class ScreenshotThread extends Thread {
	private static final Queue<ScreenshotData> SCREENSHOT_QUEUE = new ConcurrentLinkedQueue<>();
	private static final Queue<String> MESSAGES_QUEUE = new ConcurrentLinkedQueue<>();
	
	public ScreenshotThread() {
		setName("Screenshot Saver");
	}
	
	@Override
	public void run() {
		while (ThreadManager.canRun()) {
			ScreenshotData data = SCREENSHOT_QUEUE.poll();
			if (data == null) continue;
			
			BufferedImage image = new BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB);
			int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			
			for(int i = 0; i < pixels.length; i++) {
				int index = i * 3;
				int r = data.pixelData[index] & 255;
				int g = data.pixelData[index + 1] & 255;
				int b = data.pixelData[index + 2] & 255;
				int y = i / data.width;
				int x = i % data.width;
				int index2 = (data.height - y - 1) * data.width + x;
				pixels[index2] = 0xFF000000 | r << 16 | g << 8 | b;
			}
			
			File folder = new File(data.directory, "screenshots");
			//noinspection ResultOfMethodCallIgnored
			folder.mkdirs();
			
			File file = new File(folder, data.name + ".png");
			for(int index = 1; file.exists(); index++) {
				file = new File(folder, data.name + (index == 1 ? "" : "_" + index) + ".png");
			}
			
			try {
				ImageIO.write(image, "png", file);
				MESSAGES_QUEUE.add("Saved screenshot as " + file.getName());
			}
			catch (IOException e) {
				MESSAGES_QUEUE.add("Failed to save: " + file.getName());
				//noinspection CallToPrintStackTrace
				e.printStackTrace();
			}
		}
	}
	
	public static void addScreenshot(byte[] pixelData, int width, int height, File directory, String name) {
		ScreenshotData data = new ScreenshotData(pixelData, width, height, directory, name);
		SCREENSHOT_QUEUE.add(data);
	}
	
	public static String getMessage() {
		return MESSAGES_QUEUE.poll();
	}
	
	private record ScreenshotData (
		byte[] pixelData,
		int width,
		int height,
		File directory,
		String name
	) {}
}
