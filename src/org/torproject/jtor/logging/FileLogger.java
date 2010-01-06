package org.torproject.jtor.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.torproject.jtor.Logger;

public class FileLogger implements Logger {
	
	File out;
	
	public FileLogger(File out) {
		this.out = out;
		try {
			out.createNewFile();
		} catch (IOException e) {}
	}

	public void debug(String message) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			bw.write(message);
			bw.close();
		} catch (FileNotFoundException e) {
			try {
				out.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(out));
				bw.write(message);
				bw.close();
			} catch (IOException e1) {}
		} catch (IOException e) {}
	}

	public void error(String message) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			bw.write(message);
			bw.close();
		} catch (FileNotFoundException e) {
			try {
				out.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(out));
				bw.write(message);
				bw.close();
			} catch (IOException e1) {}
		} catch (IOException e) {}
	}

	public void warn(String message) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			bw.write(message);
			bw.close();
		} catch (FileNotFoundException e) {
			try {
				out.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(out));
				bw.write(message);
				bw.close();
			} catch (IOException e1) {}
		} catch (IOException e) {}
	}

}
