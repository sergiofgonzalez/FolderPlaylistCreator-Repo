package org.joolzminer.fpc.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class PlaylistWriterImpl implements PlaylistWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistWriterImpl.class);
	
	@Override
	public void write(final Path playlistName, final Collection<Path> playlistFiles) {
		Assert.notNull(playlistName, "playlistName must not be null");
		Assert.hasText(playlistName.toString(), "playlistName contents must not be null or empty");
		Assert.notEmpty(playlistFiles, "playlistFiles must not be null or empty");
		if (Files.exists(playlistName)) {
			LOGGER.warn("there is already a file with the playlistName: {}", playlistName);
		}
		
		Charset utf8Charset = Charset.forName("utf-8");
		try (	BufferedWriter bufferedWriter = Files.newBufferedWriter(playlistName, utf8Charset, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				PrintWriter printWriter = new PrintWriter(bufferedWriter);) {
			for (Path playlistFile : playlistFiles) {
				String relativeFilename = playlistName.getParent().relativize(playlistFile).toString();				
				printWriter.write(relativeFilename.replace("\\", "/"));
				printWriter.write("\n");
			}
		} catch (IOException e) {
			LOGGER.error("Error found while writing the playlist file", e);
			throw new RuntimeException(e);
		}
	}
}
