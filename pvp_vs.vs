//pvp v
#version 450 core


struct VertexData {
    float position[3];
};

layout(binding = 0, std430) readonly buffer ssbo {
    VertexData data[];
};

vec3 getPosition(int index) {
    return vec3(
        data[index].position[0], 
        data[index].position[1], 
        data[index].position[2]
    );
}



void main()
{
    gl_Position = vec4(getPosition(gl_VertexID), gl_VertexID);

}