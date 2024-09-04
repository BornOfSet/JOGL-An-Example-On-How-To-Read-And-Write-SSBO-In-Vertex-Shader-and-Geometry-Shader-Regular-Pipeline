//pvp g
#version 450 core
layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

uniform mat4 view;
uniform mat4 scale;
uniform mat4 move;

in vec3 Normal[];
out vec3 normal_F;

void main() {

    vec3 xo = gl_in[0].gl_Position.xyz;
    vec3 xa = gl_in[1].gl_Position.xyz;
    vec3 xb = gl_in[2].gl_Position.xyz;

    gl_Position = view * vec4(xo,1);
    normal_F = (view * vec4(Normal[0],1)).xyz;
    EmitVertex();
    gl_Position = view * vec4(xa,1);
    normal_F = (view * vec4(Normal[1],1)).xyz;
    EmitVertex();
    gl_Position = view * vec4(xb,1);
    normal_F = (view * vec4(Normal[2],1)).xyz;
    EmitVertex();
    EndPrimitive();
}