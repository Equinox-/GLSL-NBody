#version 120

const vec2 corners[4] = { 
    vec2(-1.0, 1.0), vec2(-1.0, -1.0), vec2(1.0, 1.0), vec2(1.0, -1.0) };

void main()
{
    gl_Position = vec4(corners[gl_VertexID], 0.0, 1.0);   
}