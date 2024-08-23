//grid_surface_primitives
#version 330 core
layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;
in vec3 normal[];
out vec3 normal_F;

void main() {
    gl_Position = gl_in[0].gl_Position;
    normal_F = normal[0];
    EmitVertex();
    gl_Position = gl_in[1].gl_Position;
    normal_F = normal[1];
    EmitVertex();
    gl_Position = gl_in[2].gl_Position;
    normal_F = normal[2];
    EmitVertex();
    EndPrimitive();
}