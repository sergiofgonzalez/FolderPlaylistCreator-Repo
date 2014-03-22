package org.joolzminer.fpc.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;

@Component
public class SimpleDirFilesReader implements DirFilesReader {

	@Override
	public Set<Path> read(Path fromDir) {
		return null;
	}

}
