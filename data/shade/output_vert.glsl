#version 130

uniform sampler2D sourceData;
uniform int sourceWidth;
uniform int sourceShift;
uniform int sourceMask;

#define DIRECT_TEXEL_FETCH 1

varying vec4 lol;

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
	vec4 data = particleData(gl_VertexID);
	vec3 pos = data.xyz;
	vec4 viewPt = gl_ModelViewMatrix * vec4(pos, 1);
	gl_PointSize = 1.25 + 1E-1 * sqrt(data[3]) / (1E-4*-viewPt.z);
	if (data[3] > 1E3){gl_PointSize =1; lol = vec4(0,0,0,1);}
	else lol = vec4(1,1,1,1);
    gl_Position = gl_ProjectionMatrix * viewPt;
}