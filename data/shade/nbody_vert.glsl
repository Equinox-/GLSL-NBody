#version 120

const vec2 pts[2] = { 
    vec2(-1.0, -1.0), vec2(1.0, 1.0)};

uniform int sourceSize;
varying float texel;

void main()
{
	texel = (pts[gl_VertexID].x + 1) / 2;
    gl_Position = vec4(pts[gl_VertexID], 0.0, 1.0);   
}