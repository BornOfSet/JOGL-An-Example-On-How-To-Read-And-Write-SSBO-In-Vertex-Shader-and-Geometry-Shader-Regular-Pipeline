//pvp v
#version 450 core


struct VertexData {
    float position[3];
};

layout(binding = 0, std430) readonly buffer ssbo {
    VertexData data[];
};
layout(binding = 1, std430) readonly buffer ssbo_normal {
    VertexData auxi[];
};

vec3 getPosition(int index) {
    return vec3(
        data[index].position[0], 
        data[index].position[1], 
        data[index].position[2]
    );
}

vec3 getNormal(int index) {
    return vec3(
        auxi[index].position[0], 
        auxi[index].position[1], 
        auxi[index].position[2]
    );
}

out vec3 Normal;

void main()
{
    gl_Position = vec4(getPosition(gl_VertexID), gl_VertexID);
    Normal = normalize(getNormal(gl_VertexID));
}