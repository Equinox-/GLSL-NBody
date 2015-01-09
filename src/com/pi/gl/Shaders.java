package com.pi.gl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.pi.Main;

public enum Shaders {
	NBODY("nbody"), OUTPUT("output");
	private static Shaders current = null;

	private final String fname;
	private int program;

	private Shaders(String fname) {
		this.fname = fname;
	}

	private static String textFileRead(File f) {
		try {
			StringBuilder res = new StringBuilder();
			BufferedReader r = new BufferedReader(new FileReader(f));
			while (true) {
				String line = r.readLine();
				if (line == null)
					break;
				res.append(line).append('\n');
			}
			r.close();
			return res.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void ensureLoaded() {
		if (program == 0) {
			program = GL20.glCreateProgram();
			int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
			int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
			GL20.glShaderSource(vertexShader, textFileRead(new File(
					Main.dataDir, "shade/" + fname + "_vert.glsl")));
			GL20.glCompileShader(vertexShader);
			if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
				System.err
						.println("Vertex shader wasn't able to be compiled correctly. Error log:");
				System.err.println(GL20.glGetShaderInfoLog(vertexShader, 1024));
			}
			GL20.glShaderSource(fragmentShader, textFileRead(new File(
					Main.dataDir, "shade/" + fname + "_frag.glsl")));
			GL20.glCompileShader(fragmentShader);
			if (GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
				System.err
						.println("Fragment shader wasn't able to be compiled correctly. Error log:");
				System.err.println(GL20
						.glGetShaderInfoLog(fragmentShader, 1024));
			}
			GL20.glAttachShader(program, vertexShader);
			GL20.glAttachShader(program, fragmentShader);
			GL20.glLinkProgram(program);
			if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
				System.err.println("Shader program wasn't linked correctly.");
				System.err.println(GL20.glGetProgramInfoLog(program, 1024));
				throw new RuntimeException();
			}
			GL20.glDeleteShader(vertexShader);
			GL20.glDeleteShader(fragmentShader);
		}
	}

	public void use() {
		ensureLoaded();
		GL20.glUseProgram(program);
		current = this;
	}

	public int uniform(final String label) {
		ensureLoaded();
		return GL20.glGetUniformLocation(program, label);
	}

	public static void noProgram() {
		GL20.glUseProgram(0);
		current = null;
	}

	public static Shaders current() {
		return current;
	}
}
