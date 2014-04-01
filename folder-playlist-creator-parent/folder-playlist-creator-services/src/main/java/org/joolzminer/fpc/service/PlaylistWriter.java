package org.joolzminer.fpc.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface PlaylistWriter {
	void write(final Path playlistName, final Collection<Path> playlistFiles) throws IOException;
}
