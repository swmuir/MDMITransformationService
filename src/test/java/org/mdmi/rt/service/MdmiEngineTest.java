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

import org.apache.commons.io.FilenameUtils;
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
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)

@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class MdmiEngineTest {

	@BeforeClass
	public static void setEnvironment() {
		System.setProperty("mdmi.maps", "/Users/seanmuir/git/njservices/mmisservices1/maps");
		System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "credentials/google_application_credentials.json");
		System.setProperty("mpi_usetoken", "true");

		// System.setProperty("your_project_id", "zanenet-njinck");
		//
		// System.setProperty("your_region_id", "us-central1");
		//
		// System.setProperty("your_dataset_id", "dev-zanenet-njinck");
		//
		// System.setProperty("your_fhir_id", "dev-mdix-datastore");

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

	private void runTransformation4(String source, String target, String message) throws Exception {
		runTransformation5(source, target, message, "json");
	}

	private void runTransformation(String source, String target, String message, String extension) throws Exception {
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.add("source", source);
		map.add("target", target);
		map.add("message", new FileSystemResource(Paths.get(message)));

		ClientHttpRequestFactory foo1 = template.getRestTemplate().getRequestFactory();

		// ((SimpleClientHttpRequestFactory) template.getRestTemplate().getRequestFactory()).setConnectTimeout(100000);
		// ((SimpleClientHttpRequestFactory) template.getRestTemplate().getRequestFactory()).setReadTimeout(100000);

		// SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = (SimpleClientHttpRequestFactory) template.getRequestFactory();
		// simpleClientHttpRequestFactory.setReadTimeout(100); // millis
		//

		ResponseEntity<String> response = template.postForEntity(
			"/mdmi/transformation/transformAndPost", map, String.class);
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

	private void runTransformation5(String source, String target, String message, String extension) throws Exception {
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.add("source", source);
		map.add("target", target);
		map.add("message", new FileSystemResource(Paths.get(message)));

		ClientHttpRequestFactory foo1 = template.getRestTemplate().getRequestFactory();

		// ((SimpleClientHttpRequestFactory) template.getRestTemplate().getRequestFactory()).setConnectTimeout(100000);
		// ((SimpleClientHttpRequestFactory) template.getRestTemplate().getRequestFactory()).setReadTimeout(100000);

		// SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = (SimpleClientHttpRequestFactory) template.getRequestFactory();
		// simpleClientHttpRequestFactory.setReadTimeout(100); // millis
		//

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

	private void runTransformation3(String testName, String source, String target, String message) throws Exception {

		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.add("source", source);
		map.add("target", target);
		map.add("message", new FileSystemResource(Paths.get(message)));
		ResponseEntity<String> response = template.postForEntity(
			"/mdmi/transformation/transformAndPost", map, String.class);
		System.out.println(response.getStatusCode());
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));

		Path sourcePath = Paths.get(message);
		// String testName = FilenameUtils.removeExtension(sourcePath.getFileName().toString());

		Path testPath = Paths.get("target/test-output/" + testName);
		if (!Files.exists(testPath)) {
			Files.createDirectories(testPath);
		}

		Path path = Paths.get("target/test-output/" + testName + "/" + testName + ".json");
		byte[] strToBytes = response.getBody().getBytes();

		Files.write(path, strToBytes);
	}

	public static <E> Optional<E> getRandom(Collection<E> e) {
		return e.stream().skip((int) (e.size() * Math.random())).findFirst();
	}

	@Test
	public void testclaims1() throws Exception {
		Set<String> documents = Stream.of(
			new File("/Users/seanmuir/git/ZaneNet-NJinCK-OnDemandPythonFunctions/samples").listFiles()).filter(
				file -> !file.isDirectory()).map(t -> {
					try {
						return t.getCanonicalPath();
					} catch (IOException e) {
						return "";
					}
				}).collect(Collectors.toSet());

		for (String fileName : documents) {
			System.err.println(fileName);
			if (fileName.contains("step3_")) {
				runTransformation("NJ.Claim", "FHIRR4JSON.MasterBundle", fileName);
			}
		}
	}

	@Test
	public void testClaimsandPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/claims5").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			try {
				runTransformation3("claim", "NJ.Claim", "FHIRR4JSON.MasterBundle", fileName);
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
					"provider", "NJ.PROVIDER", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Test
	public void testMMISPatientPost() {
		Set<String> documents = Stream.of(new File("src/test/resources/patient3").listFiles()).filter(
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
	public void testMMISPatientPost2() {
		Set<String> documents = Stream.of(new File("src/test/resources/AA1").listFiles()).filter(
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
	public void testTempCCDA() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/TEMPCCDA").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation("CDAR2.ContinuityOfCareDocument", "FHIRR4JSON.MasterBundle", fileName);
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
	public void testRCP_SPEC_PGM() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/rcp_spec_pgm").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation("NJ.RCP_SPEC_PGM", "FHIRR4JSON.MasterBundle", fileName);
		}
	}

	@Test
	public void testRCPMGCARE() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/rcpmgcare/issues").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation("NJ.RCPMGCARE", "FHIRR4JSON.MasterBundle", fileName);
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
				runTransformation3("patient", "NJ.Person", "FHIRR4JSON.MasterBundle", fileName);
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

	@Test
	public void testDiagPOA() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/diagpoa").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation("NJ.DiagPOA", "FHIRR4JSON.MasterBundle", fileName);
		}
	}

	@Test
	public void testPatient() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/patient").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {
			runTransformation("NJ.Person", "FHIRR4JSON.MasterBundle", fileName);
		}
	}

	@Test
	public void testPatientBoom() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/patientboom2").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {

			runTransformation("NJ.Person", "FHIRR4JSON.MasterBundle", fileName);

		}
	}

	@Test
	public void testProviderBoom3() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/boom").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {

			runTransformation("NJ.PROVIDER", "FHIRR4JSON.MasterBundle", fileName);

		}
	}

	@Test
	public void testClaims3() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/claims3").listFiles()).filter(
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
	public void testClaims4() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/claims4").listFiles()).filter(
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
	public void testClaims5a() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/claims5").listFiles()).filter(
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
	public void testClaims5() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/AA3").listFiles()).filter(
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
	public void testCDA() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/CDA").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {

			runTransformation("CDAR2.ContinuityOfCareDocument", "FHIRR4JSON.MasterBundle", fileName);

		}
	}

	@Test
	public void testPatientBoom2() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/AA4").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {

			runTransformation("NJ.Person", "FHIRR4JSON.MasterBundle", fileName);

		}
	}

	@Test
	public void testprovider2() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/provider2").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {

			runTransformation("NJ.PROVIDER", "FHIRR4JSON.MasterBundle", fileName);

		}
	}

	@Test
	public void testClaimsAll() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/Actual/A1/split").listFiles()).filter(
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
	public void testClaimsAll1() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/Actual/A1/all").listFiles()).filter(
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
	public void testClaimsAll12() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/claims5").listFiles()).filter(
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
	public void testPatientsAll() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/patient3").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {

			runTransformation("NJ.Person", "FHIRR4JSON.MasterBundle", fileName);

		}
	}

	@Test
	public void testPatients5() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/patient5").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {

			runTransformation("NJ.Person", "FHIRR4JSON.MasterBundle", fileName);
			Thread.sleep(100);
			runTransformation("NJ.Person", "FHIRR4JSON.MasterBundle", fileName);
		}
	}

	@Test
	public void testClaims6() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/claims6").listFiles()).filter(
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
	public void testClaims7() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/claims7").listFiles()).filter(
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
	// @Test
	// public void testMMISPatientPost() {
	// Set<String> documents = Stream.of(new File("src/test/resources/patient3").listFiles()).filter(
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
	// runTransformation3(
	// "NJ.Person", "NJ.Person", "FHIRR4JSON.MasterBundle", Files.readString(Path.of(fileName)));
	// } catch (Exception exception) {
	// exception.printStackTrace();
	// }
	// }
	// }

	@Test
	public void testPatients20220711() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/latest20220711/patients").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {

			runTransformation("NJ.Person", "FHIRR4JSON.MasterBundle", fileName);

		}
	}

	@Test
	public void testprovider20220711() throws Exception {
		// System.setProperty("mdmi.maps", "/Users/seanmuir/git/njservices/mmisservices1/maps");

		Set<String> documents = Stream.of(new File("src/test/resources/latest20220711/providers").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		for (String fileName : documents) {

			runTransformation("NJ.PROVIDER", "FHIRR4JSON.MasterBundle", fileName);

		}
	}

	@Test
	public void testClaims20220711() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/latest20220711/claims").listFiles()).filter(
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

}
