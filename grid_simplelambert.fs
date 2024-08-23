//grid_simple_lambert
#version 330 core
in vec3 normal_F;
layout (location=0) out vec4 color;
void main(){
    vec3 sundir = vec3(0,0,1);
    color = vec4(normal_F,1);
}