#version 120

uniform sampler2D sourceData;
uniform int sourceWidth;
uniform int sourceShift;
uniform int sourceMask;

vec4 particleData(int pixel) {
	return texture2D(sourceData, vec2(float(pixel & sourceMask) / sourceWidth, float(pixel >> sourceShift) / sourceWidth));
}

void main()
{
	vec3 pos = particleData(gl_VertexID << 1).xyz;
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * vec4(pos, 1);
}