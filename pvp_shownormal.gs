//pvp g
#version 450 core
layout(points) in;
layout(line_strip, max_vertices = 2) out;

uniform mat4 view;
uniform mat4 scale;
uniform mat4 move;

in vec3 Normal[];

void main() {
    float dist = .1;
    gl_Position = view * vec4(gl_in[0].gl_Position.xyz,1);
    EmitVertex(); 
    gl_Position = view * (vec4(gl_in[0].gl_Position.xyz,1) + dist * vec4(Normal[0],0));
    EmitVertex(); 
    EndPrimitive();
}