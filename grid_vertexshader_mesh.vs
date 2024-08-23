//grid_vertex_shader_mesh
#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNorm;
uniform mat4 view;
uniform mat4 scale;
out vec3 normal;

void main(){
    gl_Position = view * scale * vec4(aPos,1);
    normal = aNorm;
}