package org.joolzminer.fpc.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.joolzminer.fpc.ApplicationConfig;
import org.joolzminer.fpc.service.DirFilesReader;
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
 * + Non-existent root folder cannot be assigned
 * + If the folder is empty, the Set returned is empty
 * + If the folder contains one file, only one file is returned
 * + If the folder contains one dir, and the dir is empty, no files returned
 * + If the folder contains several files, the files are returned sorted in asc order
 * + If the folder contains several folders with files on it, the files are returned sorted by path, then name
 * 
 * @author sergio.f.gonzalez
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfig.class)
public class DirFilesReaderIntegrationTest {

	@Inject
	private DirFilesReader reader;
	
	private static final Path TEST_ROOT_PATH = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "File-Tests");
	
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
	
	@Test(expected =  IllegalArgumentException.class)
	public void testNullFolderCannotBeAssigned() throws IOException {
		reader.read(null);
	}
	
	@Test(expected =  IllegalArgumentException.class)
	public void testEmptyFolderCannotBeAssigned() throws IOException {
		reader.read(Paths.get(""));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNonExistentFolderCannotBeRead() throws IOException {
		reader.read(Paths.get("this", "does", "not", "exist"));
	}
	
	@Test
	public void testReadEmptyFolder() throws IOException {
		Set<Path> files = reader.read(TEST_ROOT_PATH);
		
		assertThat(files, is(empty()));
	}
	
	@Test
	public void testReadFolderWithOneFile() throws IOException {		
		Path file = Paths.get(TEST_ROOT_PATH.toString(), "FILE1.txt");
		Files.createFile(file);
		Set<Path> files = reader.read(TEST_ROOT_PATH);

		assertThat(files, hasSize(1));
		assertThat(files, hasItem(file));
	}
	
	@Test
	public void testReadFolderWithOneFileAndFolders() throws IOException {
		Path file = Paths.get(TEST_ROOT_PATH.toString(), "FILE1.txt");
		Files.createFile(file);
		Files.createDirectories(Paths.get(TEST_ROOT_PATH.toString(), "DIR1", "DIR2"));
		Set<Path> files = reader.read(TEST_ROOT_PATH);
		
		assertThat(files, hasSize(1));
		assertThat(files, hasItem(file));
	}
	
	@Test
	public void testReadIsAwareOfSubfolders() throws IOException {
		Files.createDirectories(Paths.get(TEST_ROOT_PATH.toString(), "DIR1", "DIR2"));
		Path file = Paths.get(TEST_ROOT_PATH.toString(), "DIR1", "DIR2", "FILE1.txt");
		Files.createFile(file);
		
		Set<Path> files = reader.read(TEST_ROOT_PATH);
		
		assertThat(files, hasSize(1));
		assertThat(files, hasItem(file));		
	}
	
	@Test
	public void testReadReturnsFilesInAscOrder() throws IOException {
		Path file1 = Paths.get(TEST_ROOT_PATH.toString(), "FILE1.txt");
		Path file2 = Paths.get(TEST_ROOT_PATH.toString(), "FILE2.txt");
		Path file3 = Paths.get(TEST_ROOT_PATH.toString(), "FILE3.txt");

		Files.createFile(file3);
		Files.createFile(file2);
		Files.createFile(file1);

		
		Set<Path> files = reader.read(TEST_ROOT_PATH);
		
		assertThat(files, hasSize(3));
		assertThat(files, contains(file1, file2, file3));	
	}

	/*
	 * 	+ FileTests
	 *  |----- + DIR1A
	 *         | ----- * FILE12.txt
	 *         | ----- * FILE21.txt
	 *  |----- + DIR1B
	 *         |----- + DIR2A
	 *                |----- * FILE13.txt
	 *  |----- * FILE11.txt
	 *  |----- + DIR1C
	 *  
	 *  String comparison will get: FILE11.txt < FILE12.txt < FILE21.txt < FILE13.txt
	 */
	@Test
	public void testReadComplexFolderStructure() throws IOException {
		Path file11 = Paths.get(TEST_ROOT_PATH.toString(), "FILE1.txt");
		Files.createFile(file11);
				
		Path path = Paths.get(TEST_ROOT_PATH.toString(), "DIR1B", "DIR2A");
		Files.createDirectories(path);
		
		Path file13 = Paths.get(path.toString(), "FILE1.txt");
		Files.createFile(file13);	
		
		path = Paths.get(TEST_ROOT_PATH.toString(), "DIR1C");
		Files.createDirectory(path);
		
		path = Paths.get(TEST_ROOT_PATH.toString(), "DIR1A");		
		Files.createDirectory(path);
		Path file12 = Paths.get(path.toString(), "FILE1.txt");
		Files.createFile(file12);
		
		Path file21 = Paths.get(path.toString(), "FILE2.txt");
		Files.createFile(file21);
		
		Set<Path> files = reader.read(TEST_ROOT_PATH);
		
		assertThat(files, hasSize(4));
		assertThat(files, contains(file12, file21, file13, file11));	
	}
}
