#version 120

uniform int sourceSize;
uniform int sourceWidth;
uniform int sourceShift;
uniform int sourceMask;
uniform float deltaT;

uniform float gravConst;

uniform sampler2D sourceData;

varying vec2 texel;

vec4 particleData(int pixel) {
	return texture2D(sourceData, vec2(float(pixel & sourceMask) / sourceWidth, float(pixel >> sourceShift) / sourceWidth));
}

void main() {
	int pixel = (int(texel.x * sourceWidth) & sourceMask) | (int(texel.y * sourceWidth) << sourceShift);
	
	float gravDelta = deltaT * gravConst;

	vec4 me1 = particleData(pixel & ~1);
	vec4 me2 = particleData(pixel | 1);
	if (pixel & 1) {
		gl_FragColor = me2;
		// Update velocity.
		for (int px = 0; px < sourceSize << 1; px += 2) {
			if (px != pixel) {
				vec4 it1 = particleData(px & ~1);
				vec4 it2 = particleData(px | 1);

				vec3 meToIt = it1.xyz - me1.xyz;
				float distLen = length(meToIt);
				float invRoot = inversesqrt(dot(meToIt,meToIt));
				gl_FragColor.xyz += it1[3] * gravDelta * invRoot * invRoot * invRoot
						* meToIt;
			}
		}
	} else {
		gl_FragColor = me1;
		// Update position.
		gl_FragColor.xyz += me2.xyz * deltaT;
	}
}

