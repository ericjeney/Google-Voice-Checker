package com.ericjeney.voice;
import java.io.PrintStream;


public class OutputOverride extends PrintStream {
	public OutputOverride() {
		super(System.out);
	}
	
	public void println(String line) {
		if(!line.startsWith("https://www.google.com/voice")) {
			super.println(line);
		}
	}
}
