/*******************************************************************************
 * Copyright (c) 2022 seanmuir.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.jupiter.api.Test;
import org.mdmi.rt.service.web.Deliminated2XML;

/**
 * @author seanmuir
 *
 */
class TestPreprocessor {

	public void cleanup(List<String> inputLines, String delim) {
		// CLM_BILL_PROV_IDN
		// CLM_SERV_PROV_IDN
		List<String> header = new ArrayList<>(
			Arrays.asList(inputLines.get(0).replaceAll("\"", "").split(delim))).stream().map(String::trim).collect(
				Collectors.toList());
		int a = header.indexOf("CLM_BILL_PROV_IDN");

		int aa = header.indexOf("CLM_ADD_PROV_NUM_BILL_NPI_IDN");
		int b = header.indexOf("CLM_SERV_PROV_IDN");
		int bb = header.indexOf("CLM_ADD_PROV_NUM_SERV_NPI_IDN");

		ListIterator<String> crunchifyListIterator = inputLines.listIterator();
		int rowctr = 0;
		System.err.println(inputLines.get(0));
		boolean has1 = false;
		boolean has2 = false;
		while (crunchifyListIterator.hasNext()) {
			String row = crunchifyListIterator.next();
			List<String> cells = Arrays.asList(row.split(delim));

			if (StringUtils.isEmpty(cells.get(a))) {
				// System.err.println(rowctr + "missing CLM_BILL_PROV_IDN" + cells.get(a));
				if (!has1) {
					System.err.println(row);
					has1 = true;
				}
			}
			if (StringUtils.isEmpty(cells.get(b))) {
				// System.err.println(rowctr + "missing CLM_SERV_PROV_IDN" + cells.get(b));
				if (!has2) {
					System.err.println(row);
					has2 = true;
				}
			}
			rowctr++;
			// System.out.println(crunchifyListIterator.next());
		}

	}

	public void cleanup2(List<String> inputLines, String delim) {
		// CLM_BILL_PROV_IDN
		// CLM_SERV_PROV_IDN
		List<String> header = new ArrayList<>(
			Arrays.asList(inputLines.get(0).replaceAll("\"", "").split(delim))).stream().map(String::trim).collect(
				Collectors.toList());

		// RCP_FIRST_NAME|RCP_MIDDLE_INIT|RCP_LAST_NAME|BIRTH_DTE
		int a = header.indexOf("RCP_FIRST_NAME");
		int b = header.indexOf("RCP_MIDDLE_INIT");
		int c = header.indexOf("RCP_LAST_NAME");
		int d = header.indexOf("BIRTH_DTE");

		// int aa = header.indexOf("CLM_ADD_PROV_NUM_BILL_NPI_IDN");
		// int b = header.indexOf("CLM_SERV_PROV_IDN");
		// int bb = header.indexOf("CLM_ADD_PROV_NUM_SERV_NPI_IDN");

		ListIterator<String> crunchifyListIterator = inputLines.listIterator();
		int rowctr = 0;
		HashMap<String, String> ids = new HashMap<String, String>();

		// System.err.println(inputLines.get(0));
		while (crunchifyListIterator.hasNext()) {
			String row = crunchifyListIterator.next();
			List<String> cells = Arrays.asList(row.split(delim));
			String k = cells.get(a) + "___" + cells.get(b) + cells.get(c) + "___" + cells.get(d) + "___";
			// System.out.println(k);

			if (!ids.containsKey(k)) {
				ids.put(k, row);
			} else {
				System.err.println(rowctr + "  duplicate " + k);
				System.err.println(row);
				System.err.println(ids.get(k));
			}

			// if (StringUtils.isEmpty(cells.get(a)) && StringUtils.isEmpty(cells.get(aa))) {
			// // System.err.println(rowctr + "missing CLM_BILL_PROV_IDN" + cells.get(a));
			// System.err.println(row);
			// }
			// if (StringUtils.isEmpty(cells.get(b)) && StringUtils.isEmpty(cells.get(bb))) {
			// // System.err.println(rowctr + "missing CLM_SERV_PROV_IDN" + cells.get(b));
			// System.err.println(row);
			// }
			rowctr++;
			// System.out.println(crunchifyListIterator.next());
		}

	}

	public void cleanup3(List<String> inputLines, String delim) {
		// CLM_BILL_PROV_IDN
		// CLM_SERV_PROV_IDN
		List<String> header = new ArrayList<>(
			Arrays.asList(inputLines.get(0).replaceAll("\"", "").split(delim))).stream().map(String::trim).collect(
				Collectors.toList());

		// RCP_FIRST_NAME|RCP_MIDDLE_INIT|RCP_LAST_NAME|BIRTH_DTE
		int a = header.indexOf("CLM_DRG_CDE");
		// int b = header.indexOf("RCP_MIDDLE_INIT");
		// int c = header.indexOf("RCP_LAST_NAME");
		// int d = header.indexOf("BIRTH_DTE");

		// int aa = header.indexOf("CLM_ADD_PROV_NUM_BILL_NPI_IDN");
		// int b = header.indexOf("CLM_SERV_PROV_IDN");
		// int bb = header.indexOf("CLM_ADD_PROV_NUM_SERV_NPI_IDN");

		ListIterator<String> crunchifyListIterator = inputLines.listIterator();
		int rowctr = 0;
		HashMap<String, String> ids = new HashMap<String, String>();
		crunchifyListIterator.next();

		// System.err.println(inputLines.get(0));
		while (crunchifyListIterator.hasNext()) {
			String row = crunchifyListIterator.next();

			long count = row.chars().filter(ch -> ch == '|').count();
			// assertEquals(2, count);

			List<String> cells = Arrays.asList(row.split(delim, -1));
			if (StringUtils.isEmpty(cells.get(a))) {
				System.out.println(rowctr + " missing " + row);
				System.out.println(rowctr + "  " + row);
				System.out.println("split size" + cells.size());
				System.out.println("coutn size" + count);

			}
			// String k = cells.get(a) + "___" + cells.get(b) + cells.get(c) + "___" + cells.get(d) + "___";
			// // System.out.println(k);
			//
			// if (!ids.containsKey(k)) {
			// ids.put(k, row);
			// } else {
			// System.err.println(rowctr + " duplicate " + k);
			// System.err.println(row);
			// System.err.println(ids.get(k));
			// }

			// if (StringUtils.isEmpty(cells.get(a)) && StringUtils.isEmpty(cells.get(aa))) {
			// // System.err.println(rowctr + "missing CLM_BILL_PROV_IDN" + cells.get(a));
			// System.err.println(row);
			// }
			// if (StringUtils.isEmpty(cells.get(b)) && StringUtils.isEmpty(cells.get(bb))) {
			// // System.err.println(rowctr + "missing CLM_SERV_PROV_IDN" + cells.get(b));
			// System.err.println(row);
			// }
			rowctr++;
			// System.out.println(crunchifyListIterator.next());
		}

	}

	// @Test
	// void testToken() throws ClientProtocolException, URISyntaxException, IOException {
	// System.out.println(FHIRR4PostProcessorJson.getAccessToken());
	// }

	@Test
	void testMPI() throws ClientProtocolException, URISyntaxException, IOException {
		// System.out.println(
		// FHIRR4PostProcessorJson.proccessMPI(FHIRR4PostProcessorJson.getAccessToken(), "doe2", "given", new Date()));
	}

	@Test
	void testSplit() {
		// System.out.println(
		// FHIRR4PostProcessorJson.proccessMPI(FHIRR4PostProcessorJson.getAccessToken(), "doe2", "given", new Date()));
		String input = "202203553769101|FFS|02/09/2022|1|1|800004274428|233081595920|0|202203553769101|18|22B|2|086|19399|S2000|01/10/2022|01/10/2022|2|481|6|M|0056286|1326168840||08608|66|730|0056286|1326168840||08608|299.43|299.43|0.00||Y9333||||1|00|0||||||||||||";
		String[] s = input.split("\\|");
		System.out.println(s.length);
		s = "|||||||||||".split("\\|", -1);
		System.out.println(s.length);
		s = "|a|b|c|d|e|r|g|h|i|j|".split("\\|", -1);
		System.out.println(s.length);
	}

	@Test
	public void testPatientBoom() throws Exception {
		Set<String> documents = Stream.of(new File("src/test/resources/Actual/A1").listFiles()).filter(
			file -> !file.isDirectory()).map(t -> {
				try {
					return t.getCanonicalPath();
				} catch (IOException e) {
					return "";
				}
			}).collect(Collectors.toSet());

		Deliminated2XML deliminated2XML = new Deliminated2XML("NJ", "\\|");

		// for (int i = 0; i < 100; i++) {

		for (String fileName : documents) {

			System.err.println(fileName);
			Path filePath = Path.of(fileName);

			String content = Files.readString(filePath);

			Reader inputString = new StringReader(content);
			BufferedReader inputReader = new BufferedReader(inputString);
			List<String> lines = new ArrayList<>();
			for (;;) {
				String line;
				line = inputReader.readLine();
				if (line == null)
					break;
				lines.add(line);
			}
			cleanup3(lines, "\\|");
			// String r = deliminated2XML.toXML(lines, "\\|", "root", "patient");

			// runTransformation("NJ.Person", "FHIRR4JSON.MasterBundle", fileName);
		}
		// }
	}

}
