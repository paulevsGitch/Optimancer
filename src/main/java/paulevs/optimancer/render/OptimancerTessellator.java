package paulevs.optimancer.render;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

@Environment(EnvType.CLIENT)
public class OptimancerTessellator extends Tessellator {
	private final FloatList vertexData = new FloatArrayList(Short.MAX_VALUE << 1);
	private final float[] vertex = new float[9];
	private double offsetX;
	private double offsetY;
	private double offsetZ;
	
	protected OptimancerTessellator() {
		super(0);
	}
	
	@Override
	public void start() {
		vertexData.clear();
	}
	
	@Override
	public void setOffset(double x, double y, double z) {
		offsetX = x;
		offsetY = y;
		offsetZ = z;
	}
	
	@Override
	public void addVertex(double x, double y, double z) {
		vertex[0] = (float) (x + offsetX);
		vertex[1] = (float) (y + offsetY);
		vertex[2] = (float) (z + offsetZ);
		for (float value : vertex) vertexData.add(value);
	}
	
	@Override
	public void setUV(double u, double v) {
		vertex[7] = (float) u;
		vertex[8] = (float) v;
	}
	
	@Override
	public void setNormal(float x, float y, float z) {
		vertex[3] = x;
		vertex[4] = y;
		vertex[5] = z;
	}
	
	@Override
	public void color(int r, int g, int b, int a) {
		r = MathHelper.clamp(r, 0, 255);
		g = MathHelper.clamp(g, 0, 255);
		b = MathHelper.clamp(b, 0, 255);
		a = MathHelper.clamp(a, 0, 255);
		vertex[6] = Float.intBitsToFloat(a << 24 | b << 16 | g << 8 | r);
	}
	
	@Override
	public void color(float r, float g, float b, float a) {
		this.color((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F), (int) (a * 255.0F));
	}
	
	@Override
	public void color(float r, float g, float b) {
		this.color((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F), 255);
	}
	
	@Override
	public void color(int r, int g, int b) {
		this.color(r, g, b, 255);
	}
	
	@Override
	public void render() {}
	
	public void build(VBO vbo) {
		int size = vertexData.size();
		
		if (size == 0) {
			vbo.setEmpty();
			return;
		}
		
		FloatBuffer data = BufferUtils.createFloatBuffer(size);
		for (int i = 0; i < size; i++) {
			data.put(i, vertexData.getFloat(i));
		}
		data.position(0);
		
		vbo.setData(data);
	}
}
