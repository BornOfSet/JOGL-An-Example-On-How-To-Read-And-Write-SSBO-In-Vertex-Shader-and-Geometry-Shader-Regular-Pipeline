//pvp g
#version 450 core
layout(triangles) in;
layout(line_strip, max_vertices = 4) out;

uniform mat4 view;
uniform mat4 scale;
uniform mat4 move;

void main() {

    vec3 xo = gl_in[0].gl_Position.xyz;
    vec3 xa = gl_in[1].gl_Position.xyz;
    vec3 xb = gl_in[2].gl_Position.xyz;

    gl_Position = view * vec4(xo,1);
    EmitVertex();
    gl_Position = view * vec4(xa,1);
    EmitVertex();
    gl_Position = view * vec4(xb,1);
    EmitVertex();
    gl_Position = view * vec4(xo,1);
    EmitVertex();
    EndPrimitive();
}