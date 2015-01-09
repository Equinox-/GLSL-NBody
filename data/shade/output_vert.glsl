#version 130

uniform sampler2D sourceData;
uniform int sourceWidth;
uniform int sourceShift;
uniform int sourceMask;

#define DIRECT_TEXEL_FETCH 1
vec4 particleData(int pixel) {
#if DIRECT_TEXEL_FETCH
	return texelFetch(sourceData,
			ivec2(pixel & sourceMask, pixel >> sourceShift), 0);
#else
	return texture2D(sourceData,
			vec2(float(pixel & sourceMask) / sourceWidth,
					float(pixel >> sourceShift) / sourceWidth));
#endif
}

void main()
{
	vec3 pos = particleData(gl_VertexID << 1).xyz;
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * vec4(pos, 1);
}