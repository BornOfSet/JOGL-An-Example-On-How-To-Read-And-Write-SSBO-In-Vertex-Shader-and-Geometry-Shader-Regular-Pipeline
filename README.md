"# OPENGLjogl" 
"# OPENGLjogl" 

运行程序后按大键盘上的数字1键可以做以下事情：
如果你保持默认状态：
		pvp = gl.glCreateProgram();
		universalshaderloader.SetPipeline(gl, pvp, "pvp_takesinnormal.vs",  "pvp_passnormal.gs",                 "grid_simplelambert.fs");
		pvpm = gl.glCreateProgram();
		universalshaderloader.SetPipeline(gl, pvpm, "pvp_vs.vs",                   "pvp_modify2normal.gs",      	  "grid_giveflattencolor_red.fs");
		shn = gl.glCreateProgram();
		universalshaderloader.SetPipeline(gl, shn, "pvp_takesinnormal.vs",  "pvp_shownormal.gs",                "grid_giveflattencolor_white.fs");
它能够计算法线，并且显示法线和兰伯特受光模型

如果你改为：
		pvp = gl.glCreateProgram();
		universalshaderloader.SetPipeline(gl, pvp, "pvp_vs.vs",   "pvp_render.gs",            "grid_giveflattencolor_white.fs");
		pvpm = gl.glCreateProgram();
		universalshaderloader.SetPipeline(gl, pvpm, "pvp_vs.vs","pvp_modify.gs",      	  "grid_giveflattencolor_red.fs");
		shn = gl.glCreateProgram();
		universalshaderloader.SetPipeline(gl, shn, "pvp_takesinnormal.vs",  "pvp_shownormal.gs",                "grid_giveflattencolor_white.fs");
并且禁用	public boolean shownormal = false;
那么你能看到顶点偏移

注意，默认我们使用的模型是强行合并所有位置相同的点当成同一个点的
这是因为导入ASSIMP的时候它并不会严格意义上合并相同顶点。假设有相同坐标法线uv的同一个点参与多个图元的组成，ASSIMP会为每个图元拷贝一份该顶点
https://www.bilibili.com/video/BV13eHneoEWq/?spm_id_from=333.999.0.0&vd_source=0b33afa454d0c1a1fd4a40955e2f2bad
不建议更改这部分设置，因为原数据并非根据法线或者UV拆的，而是遇到边就拆，这会造成极大量的浪费

如果想恢复ASSIMP的设置：
取消注释：
		/*
		// *不再沿用之前的变量. 已废弃的代码
		FloatBuffer  olyv = FloatBuffer.allocate(nvx*3);
		for(int i=0;i<nvx;i++) {
			Vec3 position = workmesh.getVertices().get(i);
			olyv.put(position.getArray());
		};
		olyv.clear();//do not forget
		*/
改以下代码：
		gl.glBufferData(GL3.GL_SHADER_STORAGE_BUFFER, remappedpos.capacity()*Float.BYTES, remappedpos, GL3.GL_DYNAMIC_COPY);
变成：
		gl.glBufferData(GL3.GL_SHADER_STORAGE_BUFFER, olyv.capacity()*Float.BYTES, olyv, GL3.GL_DYNAMIC_COPY);
然后再到display函数将：
		gl.glBindVertexArray(rmi);
改成gl.glBindVertexArray(mesh);
注意，存在多少就改多少。因为重新创建顶点后元素索引就对不上了，它会越界，因为顶点实际数量压缩了。所以rmi是remapped index重新映射过后的索引
