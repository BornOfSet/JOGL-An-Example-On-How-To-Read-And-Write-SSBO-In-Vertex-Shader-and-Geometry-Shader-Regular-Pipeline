package jogl8.sim;

import static assimp.AiPostProcessStep.Triangulate;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.jogamp.opengl.GL2;
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
	public String Lpath =  "C:/Users/User/Desktop/stdl.obj";
	public boolean ren_mesh_points =true;
	public boolean ren_grid = false;
	public boolean ren_mesh_faces = true;
	public boolean ren_capsule = true;
	public float mmm = 0.002f;//mouse movement mult
	public buildgrid builder;
	public int linewidth = 1;
	public int pointsize = 4;
	public boolean joinidentical = false;//已经废弃的标识符，用于限定输入编程顶点着色器的数据是否需要融合顶点
	public boolean shownormal = true;
	
	//private
	private int mesh;
	private int grid;
	private int backwards;
	private int pp;
	private int sp;
	private int lp;
	private int tp;
	private int rvc;//realvertexcount
	private int tpc;//totalpointscount
	private float yaw;
	private float pitch;
	private int framebuffer;
	private int[] Dimension;
	private int pvp;
	private int pvpm;
	private int rmi;
	private int shn;
	private int pointcount;
	
	
	//for convenience
	private void updateuniforms(GL3 gl , int program, FloatBuffer RB, FloatBuffer TB, FloatBuffer SB) {
		int uv = gl.glGetUniformLocation(program, "view");
		int um = gl.glGetUniformLocation(program, "move");
		int us = gl.glGetUniformLocation(program, "scale");
		gl.glUseProgram(program);
		gl.glUniformMatrix4fv(uv, 1, false, RB);		
		gl.glUniformMatrix4fv(um, 1, false, TB);			
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

		gl.glViewport(Dimension[0], Dimension[1], Dimension[2], Dimension[3]);
		GL2 degenerate = arg0.getGL().getGL2();//如果任何一个具有core标题的着色器被使用，那么旧固定管线就无法继续使用
		degenerate.glColor3f(1, 0, 0);
		degenerate.glBegin(GL2.GL_LINES);
		degenerate.glVertex2f(0.0f, 1.0f);
		degenerate.glVertex2f(0.0f, -1.0f);
		degenerate.glEnd();
		//xoffset , yoffset , width , adaptiveheight
		gl.glViewport(Dimension[2]/2, Dimension[1], Dimension[2]/2, Dimension[3]);
		
		
		//transformation
		float xMoveDelta = (control.RealtimeLocation.x - control.StartLocation.x)*mmm;
		float yMoveDelta = (control.RealtimeLocation.y - control.StartLocation.y)*mmm;
		yaw += xMoveDelta;
		pitch += -yMoveDelta;
		pitch = Math.min(pitch,   1);
		pitch = Math.max(pitch, -1);
		//默认状态下是单位矩阵
		Vec3 UP = new Vec3(0,1,0);
		Vec3 CameraVector = new Vec3(Math.sin(yaw),Math.sin(pitch),Math.cos(yaw));
		Vec3 CameraRight = UP.cross(CameraVector);
		Vec3 CameraUp = CameraVector.cross(CameraRight);
		Vec3 CameraMovement = control.Translation;
		Mat4 M = new Mat4(
				new Vec4(CameraRight,  0),
				new Vec4(CameraUp,  	  0),
				new Vec4(CameraVector, 0),
				new Vec4(0,0,0,1)
		);
		float l = control.HardScale;
		Mat4 T = new Mat4(
				new Vec4(l,0,0,0),
				new Vec4(0,l,0,0),
				new Vec4(0,0,l,0),
				new Vec4(CameraMovement,1)
		);
		M = M.transpose();
		Vec3 scale = control.Resize;
		Mat4 S = new Mat4(1);
		S = glm.INSTANCE.scale(S,  scale);
		FloatBuffer RBuffer = utils.GetBuffer4x4(M);
		FloatBuffer SBuffer = utils.GetBuffer4x4(S);
		FloatBuffer TBuffer = utils.GetBuffer4x4(T);
		updateuniforms(gl, pp, RBuffer, TBuffer, SBuffer);
		updateuniforms(gl, sp, RBuffer, TBuffer, SBuffer);
		updateuniforms(gl, lp, RBuffer, TBuffer, SBuffer);
		updateuniforms(gl, tp, RBuffer, TBuffer, SBuffer);
		updateuniforms(gl, pvp, RBuffer, TBuffer, SBuffer);
		updateuniforms(gl, pvpm, RBuffer, TBuffer, SBuffer);
		updateuniforms(gl, shn, RBuffer, TBuffer, SBuffer);
		
		
		//render in progress
		if(ren_mesh_points) {
			gl.glPointSize(pointsize);
			gl.glBindVertexArray(mesh);
			gl.glUseProgram(pp);
			gl.glDrawElements(GL3.GL_POINTS, rvc , GL3.GL_UNSIGNED_INT, 0);
		}
		if(ren_grid) {
			gl.glLineWidth(linewidth);
			gl.glBindVertexArray(grid);
			gl.glUseProgram(lp);
			//grid geometry
			int ps = gl.glGetUniformLocation(lp, "pointsize");
			gl.glUniform1f(ps, this.builder.unitsize/2);
			gl.glDrawArrays(GL3.GL_POINTS, 0, tpc);
		}
		if(ren_mesh_faces) {
			gl.glLineWidth(linewidth);
			gl.glBindVertexArray(mesh);
			gl.glUseProgram(sp);
			gl.glDrawElements(GL3.GL_TRIANGLES, rvc, GL3.GL_UNSIGNED_INT, 0);
		}
		if(ren_capsule) {
			gl.glLineWidth(linewidth);
			gl.glBindVertexArray(mesh);
			gl.glUseProgram(tp);
			gl.glDrawElements(GL3.GL_TRIANGLES, rvc, GL3.GL_UNSIGNED_INT, 0);		
		}
		//截止到完成鼠标交互，需要修深度，加兰伯特，改线框，增加键盘移动操作，线框跟随旋转
		//截止到完成无极缩放，完成线框，需要边缘检测，投影线框
		//最终添加了一个线框包裹
		
		gl.glViewport(Dimension[0], Dimension[1], Dimension[2]/2, Dimension[3]);
		//低性能负担光追，显示高模点云
		//参见《实验性模块》部分
		//这里应该每帧基于修改后的vertex buffer给出渲染数据
		//在每次调用主动SSBO计算的时候，选择更新该渲染数据，它不是每帧更新的。
		//每帧仅仅应该绘制结果
		//先实现pvp
		
		//在GS中分配顶点ID，但网格应该在外部构建。因为顶点数量太多了
		gl.glBindVertexArray(rmi);
		gl.glUseProgram(pvp);
		gl.glDrawElements(GL3.GL_TRIANGLES, rvc, GL3.GL_UNSIGNED_INT, 0);
		
		if(this.shownormal) {
			gl.glUseProgram(shn);
			gl.glDrawArrays(GL3.GL_POINTS, 0, pointcount);
		}
		//renew
		//由于我们在一次display调用中只在开始清空画布
		//特殊几何着色器将会叠加在上一个着色器输出的结果上面绘画
		if(control.RenewGeometryShader == true) {
			utils.LOG("运行特殊几何着色器");
			gl.glBindVertexArray(rmi);
			gl.glUseProgram(pvpm);
			gl.glDrawElements(GL3.GL_TRIANGLES, rvc, GL3.GL_UNSIGNED_INT, 0);
			control.RenewGeometryShader = false;
		}
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		
	}

	@Override
	public void init(GLAutoDrawable arg0) {
		GL3 gl = arg0.getGL().getGL3();
		//gl.glDepthRange (1,0);
        gl.glEnable(GL3.GL_DEPTH_TEST);
        gl.glDepthFunc(GL3.GL_LESS);
		AiScene rootscene = new Importer().readFile(mpath , Triangulate.i);//PATH
		//参见《关于元素绘制....的规范》
		//多余的顶点缺少面来承载，如果以默认所有面都是三角面的标准创建顶点缓冲，终会导致溢出
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
		//关于元素绘制时VS 总调用次数的规范：等于图元数量*3，代表每个图元是含三个点的三角形
		
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
		tp = gl.glCreateProgram();//triangulate wire frame
		//可能允许用户自己配置着色器
		universalshaderloader.SetPipeline(gl, pp, "grid_vertexshader_mesh.vs",  "grid_pointprimitives.gs",          "grid_giveflattencolor_red.fs");
		universalshaderloader.SetPipeline(gl, sp, "grid_vertexshader_mesh.vs",  "grid_surfaceprimitives.gs",       "grid_simplelambert.fs");
		universalshaderloader.SetPipeline(gl, lp, "grid_vertexshader_grid.vs",    "grid_lineprimitives.gs",             "grid_giveflattencolor_white.fs");
		universalshaderloader.SetPipeline(gl, tp, "grid_vertexshader_mesh.vs",  "grid_triangulateprimitives.gs", "grid_giveflattencolor_red.fs");

		utils.getErrors(gl, "Initiation-I");
				
		
		
		
		//RayTracing
		//1.加载低模，导入arraybuffer。这是我们之后的射线平面
		//2.高模在此之前已经载入了。转写高模成纹理缓冲对象
		//3.加载shader files
		//4.设置offscreen frambuffer
		//5.添加side viewport
		//6.ui控制。ui控制将中断animator的行为，然后手动以render参数调用烘焙分支
		
		//Offscreen Framebuffer Configuration
		//FragColor Outputs to Channel 0
		//仅在明确提出使用offscreenFBO的时候绑定它
		//不是分屏		
		
		
		//Load Mesh
		utils.LOG("加载低模");
		AiScene lowmeshscene  = new Importer().readFile(this.Lpath , Triangulate.i);
		AiMesh Mesh = lowmeshscene.getMeshes().get(0);
		utils.log(lowmeshscene.getMeshes().size() + " 个网格存在于 " + this.Lpath);
		
		List<float[]> UVs = Mesh.getTextureCoords().get(0);
		utils.log("Mesh.hasTextureCoords(0) = " + Mesh.hasTextureCoords(0));
		
		int VertexNum = Mesh.getNumVertices();
		int FaceNum = Mesh.getNumFaces();
		List<Vec3> Vertices = Mesh.getVertices();
		List<Vec3> Normals = Mesh.getNormals();
		
		List<List<Integer>> Faces = Mesh.getFaces();

		//顶点数据
		FloatBuffer output = FloatBuffer.allocate(VertexNum*8); 
		for(int i = 0;i<VertexNum;i++) {
			Vec3 v1 = Vertices.get(i);
			Vec3 v2 = Normals.get(i);
			output.put(v1.toFloatArray());
			output.put(v2.toFloatArray());
			
			float[] v3 = UVs.get(i);
			output.put(v3[0]);
			output.put(v3[1]);
		}
		output.flip();
		
		//索引（图元顶点!=实际顶点，实际顶点可以被压缩到少于图元所要求的顶点的数量）
		IntBuffer indices = IntBuffer.allocate(FaceNum*3); 
		for(int i = 0;i<FaceNum;i++) {
			List<Integer> face = Faces.get(i);
			for(int j = 0;j<3;j++) {
				indices.put(face.get(j));
			}
		}
		indices.flip();

		
		//绑定
		IntBuffer backwards_VAO = IntBuffer.allocate(1);
		gl.glGenVertexArrays(1, backwards_VAO);
		this.backwards = backwards_VAO.get();
		gl.glBindVertexArray(this.backwards);
		
		IntBuffer backwards_VBO = IntBuffer.allocate(1);
		gl.glGenBuffers(1, backwards_VBO);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, backwards_VBO.get());
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, 	 VertexNum * 8 * Float.BYTES, 	 output, 	GL3.GL_STATIC_DRAW);
		
		IntBuffer backwards_EBO = IntBuffer.allocate(1);
		gl.glGenBuffers(1, backwards_EBO);
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, backwards_EBO.get());
		gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER,    FaceNum  *  3 * Integer.BYTES,		indices, 		GL3.GL_STATIC_DRAW);

		
		gl.glEnableVertexAttribArray(3);
		gl.glVertexAttribPointer(3, 3, GL3.GL_FLOAT, false, 8*Float.BYTES, 0);
		gl.glEnableVertexAttribArray(4);
		gl.glVertexAttribPointer(4, 3, GL3.GL_FLOAT, false, 8*Float.BYTES, 3*Float.BYTES);
		gl.glEnableVertexAttribArray(5);
		gl.glVertexAttribPointer(5, 2, GL3.GL_FLOAT, false, 8*Float.BYTES, 6*Float.BYTES);
		
		gl.glBindVertexArray(0);
		
		
		//高模
		
		//1.逐图元解析顶点数据，为每个顶点创建平面法线，就像GS；然后遍历所有顶点，将法线平滑
		//2.以相同的参数创建网格，在每个网格生成的过程中检查所有顶点，将它的位置值的W变量设为该网格的索引（因为网格是一次性创建的，我们不需要在FS里面对每个像素执行该过程）
		//
		
		//我们需要element array buffer，因为顶点本身是压缩的，我们在不同的面上需要反复索引同一个顶点的位置
		//这个位置被称作gl_VertexID
		//在GS中处理法线的时候，我们可能在不同的图元上屡次遇到同一个顶点，只需要执行加法就行了
		//与shader的类型同步....
		//在shader中，我们期望用结构体来储存ssbo。ssbo转录成一个数组，数组的长度应该等于顶点总数
		//注释：高模没有uv；法线亟待处理
		
		//参见《实验性模块》
		//先导入SSBO
		
		//实验性模块
		//通过SSBO传入不重复的顶点数据和对应的顶点ID
		//通过elementdraw确定顶点所在的图元
		//通过传入gs的顶点ID修改SSBO对应的数据
		//将顶点放在SSBO中传入和修改，得到符合我们预期的修改后顶点
		//https://ktstephano.github.io/rendering/opengl/prog_vtx_pulling
		//需要一个正常的pvp着色器读取修改后的数据然后显示
		//pvp....
		
		utils.LOG("解析高模");
		
		//补充TODO：由于我只考虑坐标，而法线可以计算，从这里开始我要打算优化高模，合并独一无二的顶点
		//使用哈希表
		//可以简单地使用重复的哈希值赋值，然后循环索引数组用得到的坐标获知新坐标，将索引的目标改变
		//该过程不应该放在最开始，因为我希望计算法线。这里完全没有考虑法线，只压缩了顶点位置
		//压缩造成索引顺序改变，就需要重新映射
		//Pos-Index
		HashMap<Vec3, Integer> Remapping = new HashMap<Vec3, Integer>();
				
		//创造一个可以通过Vec3来检索重新映射之后的索引的哈希
		//这将暗地里消灭重复值，使得值独一无二
		//我操，有问题。你如果在某个size上遇到了一个以前见到的值，你会将它重新赋值成当前size。如果你在最后遍历了一遍已经赋予过的哈希
		//你会强行把所有值设为一个常量，因为你已经到了最大的极限，你用这个极限数据反复重置旧数据
		//解决方法：把integer改成integer[]或者List<Integer>
		//不需要那么麻烦，只要一个size==lastsize的判定就行了。
		//呃，那你为什么不用containkey
		for(int i=0;i<nvx;i++) {
			Vec3 position = workmesh.getVertices().get(i);
			Integer index = Remapping.size();
			if(!Remapping.containsKey(position)) {
				Remapping.put(position, index);
			}
		};
		
		//独一无二的值：Assimp的愚蠢分配：实际上要输入的顶点数量
		this.pointcount = Remapping.size();
		utils.LOG(Remapping.size()+"hash size");
		utils.LOG(nvx+"vertex");
		utils.LOG(rvc+"real");
		
		
		//遍历所有key-value对，输出key值
		//要注意，keySet() method in HashMap returns keys in random order
		//所以需要用到双参数put方法
		FloatBuffer remappedpos = FloatBuffer.allocate(Remapping.size()*3);
		Set<Vec3> Pairs = Remapping.keySet();
		Iterator<Vec3> pairIterator = Pairs.iterator();
		while(pairIterator.hasNext()) {
			Vec3 thispos = pairIterator.next();
			Integer index = Remapping.get(thispos);
			float X = thispos.getX();
			float Y = thispos.getY();
			float Z = thispos.getZ();
			remappedpos.put(index*3+0, X);
			remappedpos.put(index*3+1, Y);
			remappedpos.put(index*3+2, Z);
		}
		remappedpos.clear();
		//int nE = workmesh.getNumFaces();
		//在重映射后，这个值依然不变，因为我们确实有如此数量的图元
		IntBuffer remappedindex = IntBuffer.allocate(nE*3); 
		for(int i=0;i<nE;i++) {
			List<List<Integer>> faces = workmesh.getFaces();
			for(int j=0;j<3;j++) {
				int realVertexIndex = faces.get(i).get(j);
				Vec3 retrievedPos = workmesh.getVertices().get(realVertexIndex);
				remappedindex.put(Remapping.get(retrievedPos));
			}
		}
		remappedindex.clear();
		
		
		/*
		// *不再沿用之前的变量. 已废弃的代码
		FloatBuffer  olyv = FloatBuffer.allocate(nvx*3);
		for(int i=0;i<nvx;i++) {
			Vec3 position = workmesh.getVertices().get(i);
			olyv.put(position.getArray());
		};
		olyv.clear();//do not forget
		*/

		//VAO不需要储存VBO状态
		IntBuffer ssbo = IntBuffer.allocate(2);
		gl.glGenBuffers(2, ssbo);
		gl.glBindBuffer(GL3.GL_SHADER_STORAGE_BUFFER, ssbo.get(0));
		
		//参见《关于元素绘制....的规范》
		//缓冲对象总尺寸的规范：原始A类图元数量*A类图元含有顶点数+原始B类图元数量*B类图元含有顶点数+....
		//特此区分原始图元为未经过三角处理的写于原obj文件的，含有数量不受限制的顶点来构成自身的一组数据
		//也就是说，即使完全相同的数据，由于组成不同的图元，而会被强制拷贝
		gl.glBufferData(GL3.GL_SHADER_STORAGE_BUFFER, remappedpos.capacity()*Float.BYTES, remappedpos, GL3.GL_DYNAMIC_COPY);
		//SSBO 索引：0
		gl.glBindBufferBase(GL3.GL_SHADER_STORAGE_BUFFER, 0, ssbo.get(0));
		
		//绑定一个法线集
		gl.glBindBuffer(GL3.GL_SHADER_STORAGE_BUFFER, ssbo.get(1));
		FloatBuffer emptynormal = FloatBuffer.allocate(Remapping.size()*3);
		gl.glBufferData(GL3.GL_SHADER_STORAGE_BUFFER, emptynormal.capacity()*Float.BYTES, emptynormal, GL3.GL_DYNAMIC_COPY);
		gl.glBindBufferBase(GL3.GL_SHADER_STORAGE_BUFFER, 1, ssbo.get(1));

		
		//没有必要继续绑定操作对象（操作目标）了，因为我们已经将实际的缓冲块绑定到索引上了
		gl.glBindBuffer(GL3.GL_SHADER_STORAGE_BUFFER, 0);
		//重新绑定element buffer 
		//VS调用次数不变
		IntBuffer ANew = IntBuffer.allocate(1);
		gl.glGenVertexArrays(1, ANew);
		gl.glBindVertexArray(ANew.get(0));
		this.rmi = ANew.get(0);
		IntBuffer ENew = IntBuffer.allocate(1);
		gl.glGenBuffers(1, ENew);
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, ENew.get(0));
		gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, remappedindex.capacity() * Integer.BYTES, remappedindex, GL3.GL_STATIC_DRAW);
		gl.glBindVertexArray(0);
		//预料的结果：
		//由于没有考虑法线，仅仅考虑坐标，因此相同的坐标就会融合。原模型有没有平滑法线，不影响最后效果

		//一个负责显示，一个负责实际写入SSBO
		pvp = gl.glCreateProgram();
		universalshaderloader.SetPipeline(gl, pvp, "pvp_takesinnormal.vs",  "pvp_passnormal.gs",                 "grid_simplelambert.fs");
		pvpm = gl.glCreateProgram();
		universalshaderloader.SetPipeline(gl, pvpm, "pvp_vs.vs",                   "pvp_modify2normal.gs",      	  "grid_giveflattencolor_red.fs");
		shn = gl.glCreateProgram();
		universalshaderloader.SetPipeline(gl, shn, "pvp_takesinnormal.vs",  "pvp_shownormal.gs",                "grid_giveflattencolor_white.fs");

		
		
		
		
		utils.getErrors(gl, "programmable vertices pulling"); 
	}

	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
		this.Dimension = new int[] {arg1, arg2, arg3, arg4} ;
		//bind frame buffer 1 (custom)
		//gl viewport
		//bind frame buffer 0
	}

}
