//pvp g
#version 450 core
layout(triangles) in;
layout(line_strip, max_vertices = 4) out;

struct VertexData {
    float position[3];
};

layout(std430, binding = 0) writeonly buffer ssbo {
    VertexData data[];
};

void setPosition(int index, vec3 pos) {
    VertexData postruct = VertexData(float[3](pos.x,pos.y,pos.z));
    data[index] = postruct;
}

uniform mat4 view;
uniform mat4 scale;
uniform mat4 move;

void main(){

    //for ssbo
    vec3 flatnormal = normalize(cross(gl_in[1].gl_Position.xyz-gl_in[0].gl_Position.xyz, gl_in[2].gl_Position.xyz-gl_in[0].gl_Position.xyz));

    float movedistance = 0.1;
    
    vec4 pos0 = gl_in[0].gl_Position;
    vec4 pos1 = gl_in[1].gl_Position;
    vec4 pos2 = gl_in[2].gl_Position;

    setPosition(int(pos0.w), pos0.xyz + movedistance * flatnormal);
    setPosition(int(pos1.w), pos1.xyz + movedistance * flatnormal);
    setPosition(int(pos2.w), pos2.xyz + movedistance * flatnormal);


    //for rendering
    gl_Position = view * vec4(gl_in[0].gl_Position.xyz,1.0);
    EmitVertex();
    gl_Position = view * vec4(gl_in[1].gl_Position.xyz,1.0);
    EmitVertex();
    gl_Position = view * vec4(gl_in[2].gl_Position.xyz,1.0);
    EmitVertex();
    gl_Position = view * vec4(gl_in[0].gl_Position.xyz,1.0);
    EmitVertex();
    EndPrimitive();
}