package org.joolzminer.fpc.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public interface DirFilesReader {
	Set<Path> read(Path fromDir);
}
