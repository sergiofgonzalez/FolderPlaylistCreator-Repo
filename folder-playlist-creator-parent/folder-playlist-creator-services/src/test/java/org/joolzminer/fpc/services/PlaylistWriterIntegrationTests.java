package org.joolzminer.fpc.services;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.joolzminer.fpc.ApplicationConfig;
import org.joolzminer.fpc.service.PlaylistWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


/**
 * + Playlist must have a name
 * + Playlist files must not be empty
 * + If the playlist has one file, the playlist will have a single entry relativize to playlist location
 * + If the playlist has several files, the playlist will have as many lines as files in the same order,
 *   and relativize to the playlist location.
 * + Only .mp3 files are added to the playlist
 *   
 * File relativization examples:
 * 			c:/temp/playlists/kermode-mayo/kermode.m3u
 * 			c:/temp/playlists/kermode-mayo/2014-01-05.mp3 -> 2014-01-05.mp3
 *			c:/temp/playlists/kermode-mayo/2014-02-05.mp3 -> 2014-02-05.mp3
 *			c:/temp/playlists/kermode-mayo/2014-03-05.mp3 -> 2014-03-05.mp3
 *
 *			c:/temp/playlists/kermode.m3u
 *			c:/temp/playlists/kermode-mayo/2014-01-05.mp3 -> ./kermode-mayo/2014-01-05.mp3
 *			c:/temp/playlists/kermode-mayo/2014-02-05.mp3 -> ./kermode-mayo/2014-02-05.mp3
 *			c:/temp/playlists/kermode-mayo/2014-03-05.mp3 -> ./kermode-mayo/2014-03-05.mp3
 * 			
 * 
 * @author sergio.f.gonzalez
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfig.class)
public class PlaylistWriterIntegrationTests {
	private static final Path TEST_ROOT_PATH = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "File-Tests");
	
	@Inject
	private PlaylistWriter writer;
	
	@Before
	public void setup() throws IOException {
		Assert.isTrue(Files.exists(TEST_ROOT_PATH.getParent()), "'./src/test/resources' should exist");		
		FileUtils.deleteQuietly(TEST_ROOT_PATH.toFile());
		Files.createDirectory(TEST_ROOT_PATH);
	}
	
	@After
	public void tearDown() {
		FileUtils.deleteQuietly(TEST_ROOT_PATH.toFile());		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPlaylistNameMustNotBeNull() {
		List<Path> files = Arrays.asList(new Path[] { Paths.get("./mp3z/file1.txt") });
		
		writer.write(null, files);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPlaylistNameMustNotBeEmpty() {
		List<Path> files = Arrays.asList(new Path[] { Paths.get("./mp3z/file1.txt") });
		writer.write(Paths.get(""), files);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPlaylistNameMustBeValidFilename() {
		List<Path> files = Arrays.asList(new Path[] { Paths.get("./mp3z/file1.txt") });
		writer.write(Paths.get("*%$jskjlj\\//"), files);
	}
	
	@Test
	public void testPlaylistNameIsOverwrittenIfPreviouslyExist() throws IOException {
		Path playlist = Paths.get(TEST_ROOT_PATH.toString(), "playlist.m3u");
		Files.createFile(playlist);
		List<Path> files = Arrays.asList(new Path[] { Paths.get(TEST_ROOT_PATH.toString(), "mp3z", "song1.mp3") });
		writer.write(playlist, files);
		
		List<Path> newFiles = Arrays.asList(new Path[] { Paths.get(TEST_ROOT_PATH.toString(), "mp3z", "song2.mp3") });
		writer.write(playlist, newFiles);
		List<String> fileLines = Files.readAllLines(playlist, Charset.defaultCharset());
		
		assertThat(Files.exists(playlist), is(true));
		assertThat(fileLines, hasSize(1));
		assertThat(fileLines, hasItem("mp3z/song2.mp3"));		
	}
	
	@Test
	public void testCreatePlaylistWithSingleEntry() throws IOException {
		Path playlist = Paths.get(TEST_ROOT_PATH.toString(), "playlist.m3u");
		List<Path> files = Arrays.asList(new Path[] { Paths.get(TEST_ROOT_PATH.toString(), "mp3z", "song1.mp3") });
		writer.write(playlist, files);
		List<String> fileLines = Files.readAllLines(playlist, Charset.defaultCharset());
		
		assertThat(Files.exists(playlist), is(true));
		assertThat(fileLines, hasSize(1));
		assertThat(fileLines, hasItem("mp3z/song1.mp3"));
	}	
	
	@Test
	public void testCreatePlaylistWithSeveralEntriesOneFolder() throws IOException {
		Path playlist = Paths.get(TEST_ROOT_PATH.toString(), "playlist.m3u");
		List<Path> files = Arrays.asList(new Path[] { 
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z", "song1.mp3"),
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z", "song2.mp3"),
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z", "song3.mp3") });

		writer.write(playlist, files);
		List<String> fileLines = Files.readAllLines(playlist, Charset.defaultCharset());
		
		assertThat(Files.exists(playlist), is(true));
		assertThat(fileLines, hasSize(3));
		assertThat(fileLines, contains("mp3z/song1.mp3", "mp3z/song2.mp3", "mp3z/song3.mp3"));
	}	
	
	@Test
	public void testCreatePlaylistWithSeveralEntriesSeveralFolder() throws IOException {
		Path playlist = Paths.get(TEST_ROOT_PATH.toString(), "playlist.m3u");
		List<Path> files = Arrays.asList(new Path[] { 
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z-001", "song1.mp3"),
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z-001", "song2.mp3"),
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z-002", "song3.mp3") });

		writer.write(playlist, files);
		List<String> fileLines = Files.readAllLines(playlist, Charset.defaultCharset());
		
		assertThat(Files.exists(playlist), is(true));
		assertThat(fileLines, hasSize(3));
		assertThat(fileLines, contains("mp3z-001/song1.mp3", "mp3z-001/song2.mp3", "mp3z-002/song3.mp3"));
	}	
	
	@Test
	public void testCreatePlaylistWithMp3AndNonMp3Files() throws IOException {
		Path playlist = Paths.get(TEST_ROOT_PATH.toString(), "playlist.m3u");
		List<Path> files = Arrays.asList(new Path[] { 
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z-001", "photo.jpg"),
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z-001", "song1.mp3"),
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z-001", "song2.mp3"),
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z-001", "kermode.m3u"),
				Paths.get(TEST_ROOT_PATH.toString(), "mp3z-002", "song3.mp3") });

		writer.write(playlist, files);
		List<String> fileLines = Files.readAllLines(playlist, Charset.defaultCharset());
		
		assertThat(Files.exists(playlist), is(true));
		assertThat(fileLines, hasSize(3));
		assertThat(fileLines, contains("mp3z-001/song1.mp3", "mp3z-001/song2.mp3", "mp3z-002/song3.mp3"));
	}
}
