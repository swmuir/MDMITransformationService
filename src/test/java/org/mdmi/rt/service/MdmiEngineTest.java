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

import static org.junit.Assert.assertFalse;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mdmi.rt.service.web.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)

@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class MdmiEngineTest {

	@BeforeClass
	public static void setEnvironment() {
		System.setProperty("mdmi.maps", "maps");
		System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "credentials/google_application_credentials.json");

		System.setProperty("your_project_id", "zanenet-njinck");

		System.setProperty("your_region_id", "us-central1");

		System.setProperty("your_dataset_id", "dev-zanenet-njinck");

		System.setProperty("your_fhir_id", "dev-mdix-datastore");

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
	public void testADT_A01() {
		Set<String> documents = Stream.of(new File("src/test/resources/ADT_A01").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation("HL7V2.ADTA01CONTENT", "FHIRR4JSON.MasterBundle", fileName);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testADT_A01Post() {
		Set<String> documents = Stream.of(new File("src/test/resources/ADT_A01").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"ADT_A01POST", "HL7V2.ADTA01CONTENT", "FHIRR4JSON.MasterBundle",
					Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testADT_A01A() {

		Set<String> documents = Stream.of(new File("src/test/resources/ADT_A01").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				if (fileName.endsWith("ADT_A01 - 2.txt")) {
					runTransformation("HL7V2.ADTA08CONTENT", "FHIRR4JSON.MasterBundle", fileName);
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testADT_A03Post() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/ADT_A03").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation3(
				"ADT_A03POST", "HL7V2.ADTA03CONTENT", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
		}
	}

	@Test
	public void testADT_A03() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/ADT_A03").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation("HL7V2.ADTA03CONTENT", "FHIRR4JSON.MasterBundle", fileName);
		}
	}

	@Test
	public void testADT_A04() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/ADT_A04").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation("HL7V2.ADTA04CONTENT", "FHIRR4JSON.MasterBundle", fileName);
		}
	}

	@Test
	public void testADT_A04Post() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/ADT_A04").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation3(
				"ADT_A04POST", "HL7V2.ADTA04CONTENT", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
		}
	}

	@Test
	public void testADT_A08() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/ADT_A08").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation("HL7V2.ADTA08CONTENT", "FHIRR4JSON.MasterBundle", fileName);
		}
	}

	@Test
	public void testADT_A08Post() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/ADT_A08").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation3(
				"ADT_A08POST", "HL7V2.ADTA08CONTENT", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
		}
	}

	private void runTransformation(String source, String target, String message) throws Exception {
		runTransformation(source, target, message, "json");
	}

	private void runTransformation(String source, String target, String message, String extension) throws Exception {
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.add("source", source);
		map.add("target", target);
		map.add("message", new FileSystemResource(Paths.get(message)));
		ResponseEntity<String> response = template.postForEntity("/mdmi/transformation", map, String.class);
		System.out.println(response.getStatusCode());
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));

		Path sourcePath = Paths.get(message);
		String testName = FilenameUtils.removeExtension(sourcePath.getFileName().toString());

		Path testPath = Paths.get("target/test-output/" + testName);
		if (!Files.exists(testPath)) {
			Files.createDirectories(testPath);
		}

		Path path = Paths.get("target/test-output/" + testName + "/" + testName + "." + extension);
		byte[] strToBytes = response.getBody().getBytes();

		Files.write(path, strToBytes);

	}

	private String runTransformation2(String source, String target, String message) throws Exception {

		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(message, headers);

		ResponseEntity<String> response = template.postForEntity(
			"/mdmi/transformation/byvalue?source=" + source + "&target=" + target, request, String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		String value = response.getBody();
		assertFalse(StringUtils.isEmpty(value));
		System.out.println(value);
		return value;
	}

	private void runTransformation3(String testName, String source, String target, String message) throws Exception {
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(message, headers);

		ResponseEntity<String> response = template.postForEntity(
			"/mdmi/transformation/transformAndPost?source=" + source + "&target=" + target, request, String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		String value = response.getBody();
		assertFalse(StringUtils.isEmpty(value));

		Path testPath = Paths.get("target/test-output/" + testName);
		if (!Files.exists(testPath)) {
			Files.createDirectories(testPath);
		}

		Path path = Paths.get("target/test-output/" + testName + "/" + testName + ".json");
		byte[] strToBytes = response.getBody().getBytes();

		Files.write(path, strToBytes);

		System.out.println(value);
		// return value;
	}

	public static <E> Optional<E> getRandom(Collection<E> e) {
		return e.stream().skip((int) (e.size() * Math.random())).findFirst();
	}

	@Test
	public void testMMISPatient() {

		Set<String> documents = Stream.of(new File("src/test/resources/patient").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				// if (fileName.endsWith("ADT_A01 - 2.txt")) {
				runTransformation("NJ.Person", "FHIRR4JSON.MasterBundle", fileName);
				// }
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testclaims1() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/claims1").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation("NJ.Claim", "FHIRR4JSON.MasterBundle", fileName);
		}
	}

	@Test
	public void testClaimsandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/claims1").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"claims1", "NJ.Claim", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testProviderandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/provider").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"provider", "NJ.Provider", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testMMISPatientPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/patient").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"NJ.Person", "NJ.Person", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testSurgProcandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/surgproc").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"surgproc", "NJ.SurgProc", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testRevenue() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/revenue").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation("NJ.Revenue", "FHIRR4JSON.MasterBundle", fileName);
		}
	}

	@Test
	public void testRevenueandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/revenue").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"revenue", "NJ.Revenue", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testProviderNPIandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/providernpi").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"providernpi", "NJ.ProviderNPI", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testProviderNPI() {
		// Set<String> documents = Stream.of(new File("src/test/resources/providernpi").listFiles()).filter(
		// file -> !file.isDirectory()).map(t -> {
		// try {
		// return t.getCanonicalPath();
		// } catch (IOException e) {
		// return "";
		// }
		// }).collect(Collectors.toSet());
		//
		// for (String fileName : documents) {
		// try {
		//
		// runTransformation("providernpi", "NJ.ProviderNPI", "FHIRR4JSON.MasterBundle", fileName);
		// } catch (Exception exception) {
		// exception.printStackTrace();
		// }
		// }

		Set<String> documents = Stream.of(new File("src/test/resources/providernpi").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				// if (fileName.endsWith("ADT_A01 - 2.txt")) {
				runTransformation("NJ.ProviderNPI", "FHIRR4JSON.MasterBundle", fileName);
				// }
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testDiagPOAandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/diagpoa").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"diagpoa", "NJ.DiagPOA", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testRCP_SPEC_PGMandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/rcp_spec_pgm").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"rcp_spec_pgm", "NJ.RCP_SPEC_PGM", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testRCPMGCAREandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/rcpmgcare").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"rcpmgcare", "NJ.RCP_SPEC_PGM", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testRCPELIGandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/rcpelig").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"rcpelig", "NJ.RCPELIG", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testPatientandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/patient").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"patient", "NJ.Person", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testProviderAllandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/provider").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"provider", "NJ.Provider", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}

		documents = Stream.of(new File("src/test/resources/providernpi").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3(
					"providernpi", "NJ.ProviderNPI", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

}
