#version 120

const vec2 corners[4] = { 
    vec2(-1.0, 1.0), vec2(-1.0, -1.0), vec2(1.0, 1.0), vec2(1.0, -1.0) };

uniform int sourceSize;
varying vec2 texel;

void main()
{
	texel = (corners[gl_VertexID].xy + 1) / 2;
    gl_Position = vec4(corners[gl_VertexID], 0.0, 1.0);   
}