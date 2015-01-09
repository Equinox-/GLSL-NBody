#version 130
precision highp float;

uniform int sourceSize;
uniform int sourceWidth;
uniform int sourceShift;
uniform int sourceMask;
uniform int velocityMask;

uniform float deltaT;

uniform float gravConst;

uniform sampler2D sourceData;

#define FAST_INVERSE_SQRT 1
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

void main() {
	int pixel = (int(gl_FragCoord.x) & sourceMask) | (int(gl_FragCoord.y) << sourceShift);
	int particle = pixel & ~velocityMask;

	float gravDelta = deltaT * gravConst;

	vec4 me1 = particleData(particle);
	vec4 me2 = particleData(particle | velocityMask);
	if (pixel & velocityMask) {
		gl_FragColor = me2;
		// Update velocity.
		for (int px = 0; px < sourceSize; ++px) {
			if (px != particle) {
				vec4 it1 = particleData(px & ~velocityMask);
				vec4 it2 = particleData(px | velocityMask);

				vec3 meToIt = it1.xyz - me1.xyz;
#if FAST_INVERSE_SQRT
				float invRoot = inversesqrt(dot(meToIt, meToIt) + 0.001f);
				gl_FragColor.xyz += it1[3] * gravDelta * invRoot * invRoot
				* invRoot * meToIt;
#else
				float distLen = length(meToIt);
				gl_FragColor.xyz += it1[3] * gravDelta * pow(distLen + .01f, -3)
						* meToIt;
#endif
			}
		}
	} else {
		gl_FragColor = me1;
		// Update position.
		gl_FragColor.xyz += me2.xyz * deltaT;
	}
}

