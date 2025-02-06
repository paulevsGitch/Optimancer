package paulevs.optimancer.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

@Environment(EnvType.CLIENT)
public class VBO {
	//private static long startRenderTime;
	private volatile FloatBuffer data;
	private static int updatedCount;
	protected int vaoTarget;
	protected int vboTarget;
	protected int size;
	
	public VBO() {}
	
	public void setData(FloatBuffer data) {
		this.data = data;
	}
	
	public void setEmpty() {
		this.data = null;
		this.size = 0;
	}
	
	public boolean isEmpty() {
		return data == null && size == 0;
	}
	
	public void render() {
		init();
		bind();
		update();
		GL11.glDrawArrays(GL11.GL_QUADS, 0, size);
	}
	
	protected void bind() {
		GL30.glBindVertexArray(vaoTarget);
	}
	
	protected void init() {
		if (vaoTarget != 0) return;
		vaoTarget = GL30.glGenVertexArrays();
		vboTarget = GL15.glGenBuffers();
	}
	
	protected void update() {
		//if (updatedCount == 8) return;
		//updatedCount++;
		
		FloatBuffer data = this.data;
		if (data == null) return;
		
		//int timeSinceStart = (int) (System.currentTimeMillis() - startRenderTime);
		//if (timeSinceStart > 10) return;
		
		this.data = null;
		this.size = data.capacity() / 9;
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTarget);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
		
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 36, 0L);
		
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		GL11.glNormalPointer(GL11.GL_FLOAT, 36, 12L);
		
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 36, 24L);
		
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 36, 28L);
	}
	
	public static void unbind() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}
	
	public void dispose() {
		GL15.glDeleteBuffers(vaoTarget);
		GL15.glDeleteBuffers(vboTarget);
	}
	
	public static void updateTime() {
		updatedCount = 0;
		//startRenderTime = System.currentTimeMillis();
	}
}
