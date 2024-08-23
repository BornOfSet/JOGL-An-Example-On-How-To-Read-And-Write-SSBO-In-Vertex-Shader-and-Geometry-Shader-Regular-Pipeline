package jogl8.sim;

import static assimp.AiPostProcessStep.Triangulate;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import assimp.AiMesh;
import assimp.AiScene;
import assimp.Importer;
import glm_.glm;
import glm_.mat4x4.Mat4;
import glm_.vec3.Vec3;
import glm_.vec4.Vec4;
import jogl8.control;
import jogl8.utils;

public class gridtrace implements GLEventListener{

	//open
	public String mpath = "C:/Users/User/Desktop/stdl.obj";
	public boolean ren_mesh_points = false;
	public boolean ren_grid = true;
	public boolean ren_mesh_faces = true;
	public float mmm = 0.002f;//mouse movement mult
	public buildgrid builder;
	
	//private
	private int mesh;
	private int grid;
	private int pp;
	private int sp;
	private int lp;
	private int rvc;//realvertexcount
	private int tpc;//totalpointscount
	private float yaw;
	private float pitch;
	
	
	//for convenience
	private void updateuniforms(GL3 gl , int program, FloatBuffer TRB, FloatBuffer SB) {
		int uv = gl.glGetUniformLocation(program, "view");
		int us = gl.glGetUniformLocation(program, "scale");
		gl.glUseProgram(program);
		gl.glUniformMatrix4fv(uv, 1, false, TRB);			
		gl.glUniformMatrix4fv(us, 1, false, SB);	
		gl.glUseProgram(0);
	}
	
	@Override
	public void display(GLAutoDrawable arg0) {
		//DrawModuels are fixed . Available are only enum-options
		GL3 gl = arg0.getGL().getGL3();
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT);			
		gl.glClear(GL3.GL_DEPTH_BUFFER_BIT);
		gl.glBindVertexArray(0);
		gl.glUseProgram(0);
				
		//transformation
		float xMoveDelta = (control.RealtimeLocation.x - control.StartLocation.x)*mmm;
		float yMoveDelta = (control.RealtimeLocation.y - control.StartLocation.y)*mmm;
		yaw += xMoveDelta;
		pitch += -yMoveDelta;
		pitch = Math.min(pitch, 1);
		pitch = Math.max(pitch, -1);
		Vec3 UP = new Vec3(0,1,0);
		Vec3 CameraVector = new Vec3(Math.sin(yaw),Math.sin(pitch),Math.cos(yaw));
		Vec3 CameraRight = UP.cross(CameraVector);
		Vec3 CameraUp = CameraVector.cross(CameraRight);
		Vec3 CameraMovement = new Vec3(0,0,0);
		Mat4 M = new Mat4(
				new Vec4(CameraRight,   CameraMovement.getX()),
				new Vec4(CameraUp,  	  CameraMovement.getY()),
				new Vec4(CameraVector, CameraMovement.getZ()),
				new Vec4(0,0,0,1)
		);
		M = M.transpose();
		Vec3 scale = new Vec3(1);
		Mat4 S = new Mat4(1);
		S = glm.INSTANCE.scale(S,  scale);
		FloatBuffer TRBuffer = utils.GetBuffer4x4(M);
		FloatBuffer SBuffer = utils.GetBuffer4x4(S);
		updateuniforms(gl, pp, TRBuffer, SBuffer);
		updateuniforms(gl, sp, TRBuffer, SBuffer);
		updateuniforms(gl, lp, TRBuffer, SBuffer);
				
		//render in progress
		if(ren_mesh_points) {
			gl.glPointSize(4);
			gl.glBindVertexArray(mesh);
			gl.glUseProgram(pp);
			gl.glDrawElements(GL3.GL_POINTS, rvc , GL3.GL_UNSIGNED_INT, 0);
		}
		if(ren_grid) {
			gl.glBindVertexArray(grid);
			gl.glUseProgram(lp);
			//grid geometry
			int ps = gl.glGetUniformLocation(lp, "pointsize");
			gl.glUniform1f(ps, this.builder.unitsize);
			gl.glDrawArrays(GL3.GL_POINTS, 0, tpc);
		}
		if(ren_mesh_faces) {
			gl.glBindVertexArray(mesh);
			gl.glUseProgram(sp);
			gl.glDrawElements(GL3.GL_TRIANGLES, rvc, GL3.GL_UNSIGNED_INT, 0);
		}
		//截止到完成鼠标交互，需要修深度，加兰伯特，改线框，增加键盘移动操作，旋转线框

	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		
	}

	@Override
	public void init(GLAutoDrawable arg0) {
		GL3 gl = arg0.getGL().getGL3();
		//gl.glDepthRange (1,0);
        gl.glEnable(GL3.GL_DEPTH_TEST);
        gl.glDepthFunc(GL3.GL_LEQUAL);
		AiScene rootscene = new Importer().readFile(mpath , Triangulate.i);//PATH
		Integer x = rootscene.getNumMeshes();
		utils.log("LOG_MeshCount:"+x.toString());
		AiMesh workmesh = rootscene.getMeshes().get(0);
		int nvx = workmesh.getNumVertices();
		int nE = workmesh.getNumFaces();
		IntBuffer evx = IntBuffer.allocate(nE*3);
		FloatBuffer vtx = FloatBuffer.allocate(nvx*6);
		for(int i=0;i<nvx;i++) {
			Vec3 position = workmesh.getVertices().get(i);
			Vec3 normal = workmesh.getNormals().get(i);
			vtx.put(position.getArray());
			vtx.put(normal.getArray());
		};
		vtx.clear();//do not forget
		for(int i=0;i<nE;i++) {
			List<List<Integer>> faces = workmesh.getFaces();
			evx.put(utils.toarray(faces.get(i)));
		}
		evx.clear();
		
		//preparations
		IntBuffer loc = IntBuffer.allocate(2);
		IntBuffer aloc = IntBuffer.allocate(2);
		IntBuffer eloc = IntBuffer.allocate(1);
		gl.glGenBuffers(2, loc);
		gl.glGenVertexArrays(2, aloc);
		gl.glGenBuffers(1, eloc);
		
		//mesh
		this.mesh = aloc.get();
		gl.glBindVertexArray(this.mesh);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, loc.get());//vao stores binding . so bind vao before all bindings
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, vtx.capacity()*Float.BYTES, vtx, GL3.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);//position/ vertexattrib reads from arraybuffer , so bind arraybuffer before creating attribute for it
		gl.glVertexAttribPointer(0, 3, GL3.GL_FLOAT, true, 6*Float.BYTES, 0);
		gl.glEnableVertexAttribArray(1);//normal
		gl.glVertexAttribPointer(1, 3, GL3.GL_FLOAT, true, 6*Float.BYTES, 3*Float.BYTES);
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, eloc.get());
		gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, evx.capacity()*Integer.BYTES, evx, GL3.GL_STATIC_DRAW);
		this.rvc = evx.capacity();
		
		//grid
		buildgrid bg = new buildgrid(workmesh);
		Vec3[] points = bg.func();
		this.builder = bg;
		FloatBuffer geometry = FloatBuffer.allocate(points.length*3);
		for(int i=0;i<points.length;i++) {
			geometry.put(points[i].getArray());
		}
		geometry.clear();
		this.grid = aloc.get();
		gl.glBindVertexArray(this.grid);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, loc.get());
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, geometry.capacity()*Float.BYTES, geometry, GL3.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(2);
		gl.glVertexAttribPointer(2, 3, GL3.GL_FLOAT, true, 3*Float.BYTES, 0);
		gl.glBindVertexArray(0);
		this.tpc = points.length;
		
		//shader
		pp = gl.glCreateProgram();//draw module : DensedPointsCluster  CODE: dm.dpc
		sp = gl.glCreateProgram();//draw module : StandardLitSurface    CODE: dm.sls
		lp = gl.glCreateProgram();//draw module : CleanWireFrame         CODE: dm.cwf
		//可能允许用户自己配置着色器
		universalshaderloader.SetPipeline(gl, pp, "grid_vertexshader_mesh.vs",  "grid_pointprimitives.gs",    "grid_giveflattencolor_red.fs");
		universalshaderloader.SetPipeline(gl, sp, "grid_vertexshader_mesh.vs",  "grid_surfaceprimitives.gs", "grid_simplelambert.fs");
		universalshaderloader.SetPipeline(gl, lp, "grid_vertexshader_grid.vs",  "grid_lineprimitives.gs",       "grid_giveflattencolor_white.fs");
		
		utils.getErrors(gl, "Initiation");
		
	}

	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
		
	}

}
