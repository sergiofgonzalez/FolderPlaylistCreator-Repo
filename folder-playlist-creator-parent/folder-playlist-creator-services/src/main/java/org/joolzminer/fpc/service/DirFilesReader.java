package org.joolzminer.fpc.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public interface DirFilesReader {
	Set<Path> read(final Path fromDir) throws IOException;
}
