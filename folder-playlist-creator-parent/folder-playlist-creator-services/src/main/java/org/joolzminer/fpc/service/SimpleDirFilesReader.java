package org.joolzminer.fpc.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class SimpleDirFilesReader implements DirFilesReader {

	@Override
	public Set<Path> read(Path fromDir) throws IOException {
		Assert.notNull(fromDir, "fromDir cannot be null");
		Assert.hasText(fromDir.toString(), "fromDir must be initialized");
		Assert.isTrue(Files.exists(fromDir), "fromDir must be an existing directory");
		
		return (new InternalFileVisitor()).doWalkFileTree(fromDir);		
	}

	static class InternalFileVisitor extends SimpleFileVisitor<Path> {
		
		private Set<Path> visitedFiles;
		
		public Set<Path> doWalkFileTree(Path fromDir) throws IOException {
			 visitedFiles = new TreeSet<>();
			 Files.walkFileTree(fromDir, this);
			 return visitedFiles;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (Files.isRegularFile(file)) {
				visitedFiles.add(file);
			}
			return super.visitFile(file, attrs);
		}	
	}	
}
