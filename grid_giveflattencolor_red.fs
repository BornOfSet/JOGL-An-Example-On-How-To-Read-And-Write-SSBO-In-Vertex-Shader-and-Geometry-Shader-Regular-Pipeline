//grid_give_flatten_color_red
#version 330 core
layout (location=0) out vec4 color;
void main(){
    gl_FragDepth = 0;
    color = vec4(1,0,0,1);
}