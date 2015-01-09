package com.pi;

import java.util.HashMap;
import java.util.Map;

import com.pi.gl.Shaders.ShaderInjector;

public abstract class InteractionScript implements ShaderInjector {
	private final Map<String, Double> constants = new HashMap<String, Double>();
	private String particleParticleInteract;
	private String particleInteract;

	@Override
	public String vertShaderCallback(String src) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fragShaderCallback(String src) {
		// TODO Auto-generated method stub
		return null;
	}
}
