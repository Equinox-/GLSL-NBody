#version 120

uniform int sourceSize;
uniform int sourceWidth;
uniform float deltaT;

uniform float gravConst;

uniform sampler1D sourceData;

varying float texel;

vec4 particleData(int pixel) {
	return texture1D(sourceData, float(pixel) / sourceWidth);
}

void main() {
	float gravDelta = deltaT * gravConst;

	int pixel = int(texel * sourceWidth);
	vec4 me1 = particleData(pixel & ~1);
	vec4 me2 = particleData(pixel | 1);
	if (pixel & 1) {
		gl_FragColor = me2;
		// Update velocity.
		for (int px = 0; px < sourceWidth; px += 2) {
			if (px != pixel) {
				vec4 it1 = particleData(px & ~1);
				vec4 it2 = particleData(px | 1);

				vec3 meToIt = it1.xyz - me1.xyz;
				float distLen = length(meToIt);
				gl_FragColor.xyz += it1[3] * gravDelta * pow(distLen + .01f, -3)
						* meToIt;
			}
		}
	} else {
		gl_FragColor = me1;
		// Update position.
		gl_FragColor.xyz += me2.xyz * deltaT;
	}
}

