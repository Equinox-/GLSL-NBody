#version 120

uniform sampler1D sourceData;
uniform int sourceWidth;

vec4 particleData(int pixel) {
	return texture1D(sourceData, float(pixel) / sourceWidth);
}

void main()
{
	vec3 pos = particleData(gl_VertexID << 1).xyz;
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * vec4(pos, 1);
}