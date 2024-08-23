//grid_vertex_shader_grid
#version 330 core
layout (location = 2) in vec3 pos;
uniform mat4 view;
uniform mat4 scale;

void main(){
    gl_Position = view * scale * vec4(pos, 1);
}