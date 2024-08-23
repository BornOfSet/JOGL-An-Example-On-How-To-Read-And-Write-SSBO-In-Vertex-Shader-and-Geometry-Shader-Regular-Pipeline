package jogl8.sim;

import assimp.AiMesh;
import glm_.vec3.Vec3;
import jogl8.utils;

public class buildgrid {
	
	AiMesh self;
	float minmove = -0.02f;
	float maxmove = 0.02f;
	float unitsize = 0.05f;
	
	public buildgrid(AiMesh x) {
		self = x;
	}
	
	//per component lesser
	private void percless(Vec3 receive, Vec3 p) {
		float a = p.getX();
		float b = p.getY();
		float c = p.getZ();
		float u = receive.getX();
		float v = receive.getY();
		float w = receive.getZ();
		if(a<=u) receive.setX(a);
		if(b<=v) receive.setY(b);
		if(c<=w) receive.setZ(c);
	}
	
	//per component greater
	private void percgreat(Vec3 receive, Vec3 p) {
		float a = p.getX();
		float b = p.getY();
		float c = p.getZ();
		float u = receive.getX();
		float v = receive.getY();
		float w = receive.getZ();
		if(a>=u) receive.setX(a);
		if(b>=v) receive.setY(b);
		if(c>=w) receive.setZ(c);
	}
	
	public Vec3[] func() {
		
		Vec3 max = new Vec3(Float.MIN_VALUE);
		Vec3 min = new Vec3(Float.MAX_VALUE);
		for(int i=0;i<self.getNumVertices();i++) {
			Vec3 p = self.getVertices().get(i);
			percless(min,p);
			percgreat(max,p);
		}
		min = min.plus(minmove);
		max = max.plus(maxmove);
		utils.logv(min.getArray(),"MIN");
		utils.logv(max.getArray(),"MAX");
		Vec3 range = max.minus(min);
		int xcount = (int) Math.ceil(range.getX()/unitsize);//计算的就是盒子的数量，不是挡板的数量
		int ycount = (int) Math.ceil(range.getY()/unitsize);
		int zcount = (int) Math.ceil(range.getZ()/unitsize);
		Vec3 H[] = new Vec3[xcount*ycount*zcount];
		Vec3 start = min.plus(unitsize/2);
		int count = 0;
		max = new Vec3(Float.MIN_VALUE);
		min = new Vec3(Float.MAX_VALUE);
		for(int x=0;x<xcount;x++) {
			for(int y=0;y<ycount;y++) {
				for(int z=0;z<zcount;z++) {
					Vec3 delta = new Vec3(x*unitsize,y*unitsize,z*unitsize);
					H[count] = start.plus(delta);
					percless(min,H[count]);
					percgreat(max,H[count]);
					count++;
				}
			}
		}
		utils.logv(min.getArray(),"FloatingPoints");
		utils.logv(max.getArray(),"FloatingPoints");
		//公式1：(最远中心坐标新max值+一半unitsize-边界框左下值旧min值)/unitsize=ceil(....)
		//公式2：(最远中心坐标新max值-最近中心坐标值新min值)/unitsize=ceil-1
		return H;
	}
}
