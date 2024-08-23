package jogl8;

import java.nio.FloatBuffer;
import java.util.List;

import com.jogamp.opengl.GL3;

import glm_.mat4x4.Mat4;

public class utils {
	public static void log(String s) {
		System.out.print(s+"\n");
	}
	public static int[] toarray(List<Integer> list) {
		int x[] = new int[list.size()];
		for(int i = 0;i<list.size();i++) {
			x[i]=list.get(i);
		}
		return x;
	}
	public static void logv(float[] x,String r) {
		System.out.print(r + ":  ");
		for(int i=0;i<x.length;i++) {
			System.out.print(String.valueOf(x[i]) + "  ");
		}
		log("Over");
	}
	public static void getErrors(GL3 gl,String additional) {
		int x;
		while(( x = gl.glGetError()) != GL3.GL_NO_ERROR) {
			System.out.println(x + "  " + additional);
		}
		System.out.println("Stack Cleared: " + x + "   " + additional + " Is Finished \n");
	}
	public static void LOG(Object s) {
		System.out.print(String.valueOf(s)+"\n");
	}
	public static FloatBuffer GetBuffer4x4(Mat4 matrix) {
		FloatBuffer fb = FloatBuffer.allocate(16);
		fb.put(matrix.v00());fb.put(matrix.v01());fb.put(matrix.v02());fb.put(matrix.v03());
		fb.put(matrix.v10());fb.put(matrix.v11());fb.put(matrix.v12());fb.put(matrix.v13());
		fb.put(matrix.v20());fb.put(matrix.v21());fb.put(matrix.v22());fb.put(matrix.v23());
		fb.put(matrix.v30());fb.put(matrix.v31());fb.put(matrix.v32());fb.put(matrix.v33());
		fb.flip();
		return fb;
	}
}
