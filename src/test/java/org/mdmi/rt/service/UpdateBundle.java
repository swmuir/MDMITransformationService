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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.Goal.GoalLifecycleStatus;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * @author seanmuir
 *
 */
class UpdateBundle {

	/// Users/seanmuir/git/deletethis/mcc-sample-data/mcc-careplan/fhir
	@Test
	void test() {

		Set<String> set = new HashSet<String>() {
			{
				add("Chronic sinusitis (disorder)");
				add("Stress (finding)");
				add("Viral sinusitis (disorder)");
				add("Osteoarthritis of knee");
				add("Chronic intractable migraine without aura");
				add("Prediabetes");
				add("Acute bronchitis (disorder)");
				add("Anemia (disorder)");
				add("Chronic obstructive bronchitis (disorder)");
				add("Hypertension");
				add("Chronic low back pain (finding)");
				add("Acute viral pharyngitis (disorder)");
				add("Acute myeloid leukemia, disease (disorder)");
				add("Febrile neutropenia (disorder)");
				add("Chronic pain");
				add("Antepartum eclampsia");
				add("Laceration of thigh");
				add("Chronic neck pain (finding)");
				add("Severe anxiety (panic) (finding");
				add("Otitis media");
				add("Gout");
				add("Injury of tendon of the rotator cuff of shoulder");
				add("Hyperlipidemia");
				add("Osteoarthritis of hip");
				add("Coronary Heart Disease");
				add("Laceration of forearm");
				add("History of myocardial infarction (situation)");
				add("Seizure disorder");
				add("Epilepsy");
				add("History of cardiac arrest (situation)");
				add("Blighted ovum");
				add("Escherichia coli urinary tract infection");
				add("Cough (finding)");
				add("Sputum finding (finding)");
				add("Fatigue (finding)");
				add("Fever (finding)");
				add("COVID-19");
				add("Streptococcal sore throat (disorder)");
				add("Impacted molars");
			}
		};

		FhirContext ctx = FhirContext.forR4();
		if (ctx != null) {
			IParser parse = ctx.newJsonParser();

			Set<String> documents = Stream.of(
				new File("/Users/seanmuir/git/deletethis/mcc-sample-data/mcc-careplan/fhir").listFiles()).filter(
					file -> !file.isDirectory()).map(t -> {
						try {
							return t.getCanonicalPath();
						} catch (IOException e) {
							return "";
						}
					}).collect(Collectors.toSet());

			for (String fileName : documents) {
				ArrayList<Condition> plist = new ArrayList<Condition>();
				ArrayList<Goal> glist = new ArrayList<Goal>();
				try {
					if (fileName.endsWith(".json")) {
						System.out.println(fileName);
						Path filePath = Path.of(fileName);
						Bundle bundle = parse.parseResource(Bundle.class, Files.readString(filePath));

						for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
							// bundleEntry.getRequest().setMethod(HTTPVerb.PUT);
							ResourceType theResourceType = bundleEntry.getResource().getResourceType();

							if (theResourceType.equals(ResourceType.CarePlan)) {
								CarePlan carePlan = (CarePlan) bundleEntry.getResource();
								for (CodeableConcept category : carePlan.getCategory()) {
									if (!StringUtils.isEmpty(category.getText())) {
										System.err.println(category.getText());

										Goal goal = new Goal();

										if (carePlan.getPeriod() != null) {
											if (carePlan.getPeriod().getStart() != null) {
												DateType dateType = new DateType();
												dateType.setValue(carePlan.getPeriod().getStart());
												goal.setStart(dateType);
											}
										}

										CodeableConcept cc = new CodeableConcept();
										cc.setText(category.getText());
										goal.setDescription(cc);
										CodeableConcept priority = new CodeableConcept();
										priority.addCoding().setCode("high-priority").setDisplay("High Priority");
										priority.setText("High Priority");
										goal.setPriority(priority);

										goal.addAddresses(carePlan.getAddressesFirstRep());

										goal.setExpressedBy(carePlan.getSubject());

										CodeableConcept achievementStatus = new CodeableConcept();
										achievementStatus.setText("in-progress");
										goal.setAchievementStatus(achievementStatus);

										goal.setText(carePlan.getText());

										goal.setLifecycleStatus(GoalLifecycleStatus.ACTIVE);

										goal.setSubject(carePlan.getSubject());
										goal.addCategory().setText(category.getText());
										glist.add(goal);

									}

								}

							}
							if (theResourceType.equals(ResourceType.Condition)) {
								// System.out.println(bundleEntry.getResource().getResourceType());
								Condition condition = (Condition) bundleEntry.getResource();

								if (set.contains(condition.getCode().getText())) {
									System.out.println(condition.getCode().getText());
									Condition problemlistcondition = parse.parseResource(
										Condition.class, parse.encodeResourceToString(condition));
									problemlistcondition.getCategory().get(0).setText("Problem List Item");
									problemlistcondition.getCategory().get(0).getCoding().get(0).setCode(
										"problem-list-item");
									problemlistcondition.getCategory().get(0).getCoding().get(0).setDisplay(
										"Problem List Item");
									plist.add(problemlistcondition);

								}

							}
						}
						for (Condition c : plist) {
							UUID uuid = UUID.randomUUID();
							BundleEntryComponent be = bundle.addEntry();
							be.getRequest().setMethod(HTTPVerb.POST);
							be.setFullUrl("urn:uuid:" + uuid).setResource(c);
						}

						for (Goal g : glist) {
							UUID uuid = UUID.randomUUID();
							BundleEntryComponent be = bundle.addEntry();
							be.getRequest().setMethod(HTTPVerb.POST);
							be.setFullUrl("urn:uuid:" + uuid).setResource(g);
						}

						Path tofilePath = Path.of(fileName);
						parse.setPrettyPrint(true);
						Files.writeString(tofilePath, parse.encodeResourceToString(bundle), StandardOpenOption.CREATE);
						;

					}

				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}

		}
	}

}
