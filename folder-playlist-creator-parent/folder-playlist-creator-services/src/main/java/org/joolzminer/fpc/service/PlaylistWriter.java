package org.joolzminer.fpc.service;

import java.nio.file.Path;
import java.util.Collection;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PlaylistWriter {
	private Collection<Path> files;
	private String playlistFilename;
	
	public Collection<Path> getFiles() {
		return files;
	}

	public void setFiles(Collection<Path> files) {
		this.files = files;
	}

	public String getPlaylistFilename() {
		return playlistFilename;
	}

	public void setPlaylistFilename(String playlistFilename) {
		this.playlistFilename = playlistFilename;
	}

	public void write() {		
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append(playlistFilename)
			.append(files)
			.toString();
	}
}
