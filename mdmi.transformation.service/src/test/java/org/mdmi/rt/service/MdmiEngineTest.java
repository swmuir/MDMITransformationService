/*******************************************************************************
 * Copyright (c) 2018 seanmuir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     seanmuir - initial API and implementation
 *
 *******************************************************************************/
package org.mdmi.rt.service;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mdmi.rt.service.web.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)

@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class MdmiEngineTest {

	@BeforeClass
	public static void setEnvironment() {
		System.setProperty("mdmi.maps", "src/test/resources/testmaps");
	}

	@Autowired
	private TestRestTemplate template;

	@Test
	public void testGetTransformations() {
		ResponseEntity<String> response = template.getForEntity("/mdmi/transformation", String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		System.out.println(response.getBody());
	}

	@Test
	public void testReset() throws Exception {

		Set<String> files = Stream.of(new File("src/test/resources/testreset").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		try {

			ResponseEntity<String> response = template.getForEntity("/mdmi/transformation", String.class);
			assertTrue(response.getStatusCode().equals(HttpStatus.OK));
			System.out.println(response.getBody());

			for (String fileLocation : files) {
				Path source = Paths.get(fileLocation);
				Path newdir = Paths.get("src/test/resources/testmaps");
				Files.copy(Paths.get(fileLocation), newdir.resolve(source.getFileName()));
			}

			ResponseEntity<String> resetResponse = template.getForEntity("/mdmi/transformation/reset", String.class);
			assertTrue(resetResponse.getStatusCode().equals(HttpStatus.OK));
			System.out.println(resetResponse.getBody());

			assertTrue((response.getBody().length() != resetResponse.getBody().length()));

		} finally {
			for (String fileLocation : files) {
				Path source = Paths.get(fileLocation);
				Path newdir = Paths.get("src/test/resources/testmaps");
				Files.deleteIfExists(newdir.resolve(source.getFileName()));
			}
		}
	}

	@Test
	public void testCDA2FHIR() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/samples/CCDA").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (int count = 0; count < 10; count++) {
			Optional<String> document = getRandom(documents);
			if (document.isPresent()) {
				runTransformation("CDAR2.ContinuityOfCareDocument", "R4JSON.PortalBundle", document.get());
			}
		}
	}

	private void runTransformation(String source, String target, String message) throws Exception {
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.add("source", source);
		map.add("target", target);
		map.add("message", new FileSystemResource(Paths.get(message)));
		ResponseEntity<String> response = template.postForEntity("/mdmi/transformation", map, String.class);
		System.out.println(response.getStatusCode());
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		System.out.println(response.getBody());
	}

	public static <E> Optional<E> getRandom(Collection<E> e) {
		return e.stream().skip((int) (e.size() * Math.random())).findFirst();
	}

}
