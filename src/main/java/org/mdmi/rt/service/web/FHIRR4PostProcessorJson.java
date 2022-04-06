/*******************************************************************************
 * Copyright (c) 2019 seanmuir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     seanmuir - initial API and implementation
 *
 *******************************************************************************/
package org.mdmi.rt.service.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.ExplanationOfBenefit;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.ResourceType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mdmi.MessageModel;
import org.mdmi.core.MdmiMessage;
import org.mdmi.core.engine.postprocessors.IPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.IParserErrorHandler;
import ca.uhn.fhir.parser.json.JsonLikeValue.ScalarType;
import ca.uhn.fhir.parser.json.JsonLikeValue.ValueType;

/**
 * @author seanmuir
 *
 */

public class FHIRR4PostProcessorJson implements IPostProcessor {

	private static Logger logger = LoggerFactory.getLogger(FHIRR4PostProcessorJson.class);

	String credentials;

	String fhirStoreName;

	/**
	 * @param credentials
	 * @param fhirStoreName
	 */
	public FHIRR4PostProcessorJson(String credentials, String fhirStoreName) {
		this.credentials = credentials;
		this.fhirStoreName = fhirStoreName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.postprocessors.IPostProcessor#canProcess(org.mdmi.MessageModel)
	 */
	@Override
	public boolean canProcess(MessageModel messageModel) {
		if ("FHIRR4JSON".equals(messageModel.getGroup().getName()) ||
				"IPSFHIRJSON".equals(messageModel.getGroup().getName()) ||
				"CCDAonFHIRJSON".equals(messageModel.getGroup().getName())) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.postprocessors.IPostProcessor#getName()
	 */
	@Override
	public String getName() {
		return "FHIRR4PostProcessor";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.postprocessors.IPostProcessor#processMessage(org.mdmi.MessageModel, org.mdmi.core.MdmiMessage)
	 */
	@Override
	public void processMessage(MessageModel messageModel, MdmiMessage mdmiMessage) {
		FhirContext ctx = FhirContext.forR4();
		if (ctx != null) {
			IParser parse = ctx.newXmlParser();
			IParserErrorHandler doNothingHandler = new IParserErrorHandler() {

				@Override
				public void containedResourceWithNoId(IParseLocation arg0) {

				}

				@Override
				public void incorrectJsonType(IParseLocation arg0, String arg1, ValueType arg2, ScalarType arg3,
						ValueType arg4, ScalarType arg5) {

				}

				@Override
				public void invalidValue(IParseLocation arg0, String arg1, String arg2) {

				}

				@Override
				public void missingRequiredElement(IParseLocation arg0, String arg1) {

				}

				@Override
				public void unexpectedRepeatingElement(IParseLocation arg0, String arg1) {

				}

				@Override
				public void unknownAttribute(IParseLocation arg0, String arg1) {

				}

				@Override
				public void unknownElement(IParseLocation arg0, String arg1) {

				}

				@Override
				public void unknownReference(IParseLocation arg0, String arg1) {

				}
			};
			parse.setParserErrorHandler(doNothingHandler);
			HashMap<String, String> referenceMappings = new HashMap<String, String>();
			Bundle bundle = parse.parseResource(Bundle.class, mdmiMessage.getDataAsString());

			deduplicate(bundle);

			HashSet<String> noIdentifier = new HashSet<String>();

			for (BundleEntryComponent bundleEntry : bundle.getEntry()) {

				if (bundleEntry.getResource().getResourceType() != null) {

					ResourceType theResourceType = bundleEntry.getResource().getResourceType();
					if (theResourceType.equals(ResourceType.Patient)) {

						Patient patientResource = (Patient) bundleEntry.getResource();

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);
						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						if (!patientResource.getName().isEmpty()) {

							HumanName name = patientResource.getName().get(0);

							try {
								String id = proccessMPI(
									getAccessToken(), name.getFamily(), name.getGivenAsSingleString(),
									patientResource.getBirthDate());

								patientResource.setId(id);

								bundleEntry.getRequest().setUrl(
									bundleEntry.getResource().getResourceType().name() + "/" + id);
								bundleEntry.getRequest().setMethod(HTTPVerb.PUT);

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							if (!patientResource.getIdentifier().isEmpty()) {
								String pid = patientResource.getIdentifier().get(0).getValue();
								String result;
								try {
									result = FhirResourceCreate.query(
										credentials, fhirStoreName, "Patient?identifier=" + pid);
									patientResource.setId(result);

									bundleEntry.getRequest().setUrl(
										bundleEntry.getResource().getResourceType().name() + "/" + result);
									bundleEntry.getRequest().setMethod(HTTPVerb.PUT);

								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							// coverage.getBeneficiary().setReference("Patient/" + result);

						}

					}

					if (theResourceType.equals(ResourceType.Coverage)) {
						Coverage coverage = (Coverage) bundleEntry.getResource();
						if (coverage.getBeneficiary() != null && coverage.getBeneficiary().getReference() != null) {
							IIdType rid = coverage.getBeneficiary().getReferenceElement();
							try {
								String result = FhirResourceCreate.query(
									credentials, fhirStoreName, "Patient?identifier=" + rid.getIdPart());

								coverage.getBeneficiary().setReference("Patient/" + result);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);

					}

					if (theResourceType.equals(ResourceType.Claim)) {
						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);

						Claim claim = (Claim) bundleEntry.getResource();

						if (claim.getProvider() != null && !StringUtils.isEmpty(claim.getProvider().getDisplay())) {

							try {
								String result = FhirResourceCreate.query(
									credentials, fhirStoreName,
									"Practitioner?identifier=" + claim.getProvider().getDisplay());
								if (result != null) {
									claim.getProvider().setReference("Practitioner/" + result);
								}
							} catch (Exception e) {

							}

						}

						if (claim.getPatient() != null && !StringUtils.isEmpty(claim.getPatient().getDisplay())) {

							try {
								String result = FhirResourceCreate.query(
									credentials, fhirStoreName,
									"Patient?identifier=" + claim.getPatient().getDisplay());
								if (result != null) {
									claim.getPatient().setReference("Patient/" + result);
								}
							} catch (Exception e) {

							}

						}

						if (claim.getInsurer() != null && !StringUtils.isEmpty(claim.getInsurer().getDisplay())) {

							try {
								String result = FhirResourceCreate.query(
									credentials, fhirStoreName,
									"Organization?identifier=" + claim.getInsurer().getDisplay());
								if (result != null) {
									claim.getInsurer().setReference("Organization/" + result);
								}
							} catch (Exception e) {

							}

						}

					}

					/*
					 * "provider": {
					 * "display": "3675807"
					 * },
					 * "patient": {
					 * "display": "800004654951"
					 * },
					 * "insurer": {
					 * "display": "3675807"
					 * },
					 */

					if (theResourceType.equals(ResourceType.ExplanationOfBenefit)) {
						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);

						ExplanationOfBenefit explanationOfBenefit = (ExplanationOfBenefit) bundleEntry.getResource();

						if (explanationOfBenefit.getProvider() != null &&
								!StringUtils.isEmpty(explanationOfBenefit.getProvider().getDisplay())) {

							try {
								String result = FhirResourceCreate.query(
									credentials, fhirStoreName,
									"Practitioner?identifier=" + explanationOfBenefit.getProvider().getDisplay());
								if (result != null) {
									explanationOfBenefit.getProvider().setReference("Practitioner/" + result);
								}
							} catch (Exception e) {

							}

						}

						if (explanationOfBenefit.getPatient() != null &&
								!StringUtils.isEmpty(explanationOfBenefit.getPatient().getDisplay())) {

							try {
								String result = FhirResourceCreate.query(
									credentials, fhirStoreName,
									"Patient?identifier=" + explanationOfBenefit.getPatient().getDisplay());
								if (result != null) {
									explanationOfBenefit.getPatient().setReference("Patient/" + result);
								}
							} catch (Exception e) {

							}

						}

						if (explanationOfBenefit.getInsurer() != null &&
								!StringUtils.isEmpty(explanationOfBenefit.getInsurer().getDisplay())) {

							try {
								String result = FhirResourceCreate.query(
									credentials, fhirStoreName,
									"Organization?identifier=" + explanationOfBenefit.getInsurer().getDisplay());
								if (result != null) {
									explanationOfBenefit.getInsurer().setReference("Organization/" + result);
								}
							} catch (Exception e) {

							}

						}

					}

					if (theResourceType.equals(ResourceType.Practitioner)) {

						Practitioner practitioner = (Practitioner) bundleEntry.getResource();

						// bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());
						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());
						bundleEntry.getRequest().setMethod(HTTPVerb.POST);

						if (!practitioner.getIdentifier().isEmpty()) {

							for (Identifier identifier : practitioner.getIdentifier()) {
								if ("http://nj.org/id".equals(identifier.getSystem())) {

									try {
										String result = FhirResourceCreate.query(
											credentials, fhirStoreName,
											"Practitioner?identifier=" + identifier.getValue());

										if (result != null) {
											System.err.println(result);
											bundleEntry.getRequest().setMethod(HTTPVerb.PUT);
											practitioner.setId(result);
											bundleEntry.getRequest().setUrl(
												bundleEntry.getResource().getResourceType().name() + "/" + result);
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

							}

						}

					}

					if (theResourceType.equals(ResourceType.Organization)) {
						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);

					}

					if (theResourceType.equals(ResourceType.Encounter)) {
						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);

					}

					if (theResourceType.equals(ResourceType.MessageHeader)) {
						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);

					}

					if (theResourceType.equals(ResourceType.RelatedPerson)) {
						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);

					}

				}

				// UUID uuid = UUID.randomUUID();
				// bundleEntry.setFullUrl("urn:uuid:" + uuid);
				// if (bundleEntry.getResource() instanceof DomainResource) {
				// DomainResource dr = (DomainResource) bundleEntry.getResource();
				// if (!noIdentifier.contains(bundleEntry.getResource().getResourceType().name())) {
				// try {
				// Method sumInstanceMethod = dr.getClass().getMethod("getIdentifier");
				//
				// List<Identifier> identifiers = (List<Identifier>) sumInstanceMethod.invoke(dr);
				//
				// for (Identifier identifier : identifiers) {
				// String theSystem = identifier.getSystem();
				// String theValue = identifier.getValue();
				// if (!StringUtils.isEmpty(theValue)) {
				// String theKey = (!StringUtils.isEmpty(theSystem)
				// ? theSystem + "::"
				// : "") + theValue;
				//
				// referenceMappings.put(
				// bundleEntry.getResource().getResourceType().name() + "/" + theKey,
				// "urn:uuid:" + uuid);
				// }
				// }
				// } catch (Exception e) {
				// noIdentifier.add(bundleEntry.getResource().getResourceType().name());
				// }
				// }
				// }

			}

			String result = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);

			JSONParser parser = new JSONParser();

			try {
				Object obj = parser.parse(result);
				JSONObject jsonObject = (JSONObject) obj;
				walk(jsonObject, referenceMappings);
				mdmiMessage.setData(StringEscapeUtils.unescapeJson(jsonObject.toJSONString()));
				return;

			} catch (ParseException e) {
				e.printStackTrace();
			}

			mdmiMessage.setData(result);
		}
	}

	private void walk(JSONObject jsonObject, HashMap<String, String> referenceMappings) {
		for (Iterator iterator = jsonObject.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			if (key.equals("reference")) {
				System.out.println(key + " : " + jsonObject.get(key));
				if (referenceMappings.containsKey(jsonObject.get(key))) {
					jsonObject.replace(key, referenceMappings.get(jsonObject.get(key)));
				}

			}
			if (jsonObject.get(key) instanceof JSONObject) {
				walk((JSONObject) jsonObject.get(key), referenceMappings);
			}
			if (jsonObject.get(key) instanceof JSONArray) {
				JSONArray array = (JSONArray) jsonObject.get(key);
				Consumer walkit = new Consumer() {
					@Override
					public void accept(Object t) {
						// System.out.println(t);
						if (t instanceof JSONObject) {
							walk((JSONObject) t, referenceMappings);
						}

					}
				};
				array.forEach(walkit);
			}
		}

	}

	private void deduplicate(Bundle bundle) {
		HashMap<String, String> map = new HashMap<String, String>();
		ArrayList<BundleEntryComponent> removelist = new ArrayList<>();
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
			if (bundleEntry.getResource().fhirType().equals("Practitioner")) {
				Practitioner practitioner = (Practitioner) bundleEntry.getResource();
				for (Identifier id : practitioner.getIdentifier()) {
					String sid = "Prac" + id.getSystem() + "::" + id.getValue();
					if (!map.containsKey(sid)) {
						map.put(sid, "");
					} else {
						removelist.add(bundleEntry);
					}
				}
			} else if (bundleEntry.getResource().fhirType().equals("Organization")) {
				Organization organization = (Organization) bundleEntry.getResource();
				for (Identifier id : organization.getIdentifier()) {
					String sid = "Org" + id.getSystem() + "::" + id.getValue();
					if (!map.containsKey(sid)) {
						map.put(sid, "");
					} else {
						removelist.add(bundleEntry);
					}
				}
			} else if (bundleEntry.getResource().fhirType().equals("Medication")) {
				Medication medication = (Medication) bundleEntry.getResource();
				for (Identifier id : medication.getIdentifier()) {
					String sid = "Med" + id.getSystem() + "::" + id.getValue();
					if (!map.containsKey(sid)) {
						map.put(sid, "");
					} else {
						removelist.add(bundleEntry);
					}
				}
			}
		}
		bundle.getEntry().removeAll(removelist);
	}

	public static String proccessMPI(String token, String family, String given, Date dob)
			throws URISyntaxException, ClientProtocolException, IOException {

		HttpClient httpClient = HttpClients.createDefault();

		String uri = "https://master-patient-index-test-ocp.nicheaimlabs.com/api/v1/patients/?";
		URIBuilder uriBuilder = new URIBuilder(uri);

		logger.trace(
			String.format(
				"{\"name\": [{\"use\": \"official\", \"family\": \"%s\", \"given\": [\"%s\"]}], \"dob\": \"%tF\", \"createIfNotExist\":\"True\"}",
				family, given, dob));

		StringEntity requestEntity = new StringEntity(
			String.format(
				"{\"name\": [{\"use\": \"official\", \"family\": \"%s\", \"given\": [\"%s\"]}], \"dob\": \"%tF\", \"createIfNotExist\":\"True\"}",
				family, given, dob));

		HttpUriRequest request = RequestBuilder.post().setUri(uriBuilder.build()).setEntity(requestEntity).addHeader(
			"Content-Type", "application/json").addHeader("Accept-Charset", "utf-8").addHeader(
				"Accept", "application/json; charset=utf-8").addHeader("Authorization", "Bearer " + token).build();
		HttpResponse response = httpClient.execute(request);

		HttpEntity responseEntity = response.getEntity();
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			logger.error(String.format("Exception MPI: %s\n", response.getStatusLine().toString()));
			responseEntity.writeTo(System.err);
			throw new RuntimeException();
		}
		logger.info("FHIR resource created: ");

		String responseString = EntityUtils.toString(responseEntity, "UTF-8");

		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(responseString);
			JSONArray array = (JSONArray) obj;
			JSONObject jsonObject = (JSONObject) array.get(0);
			return (String) jsonObject.get("uuid");

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return responseString;
	}

	public static String getAccessToken() throws IOException, URISyntaxException {

		HttpClient httpClient = HttpClients.createDefault();

		String uri = "https://iam.mynjinck.com/auth/realms/ocp/protocol/openid-connect/token";
		URIBuilder uriBuilder = new URIBuilder(uri);

		List<NameValuePair> form = new ArrayList<>();
		form.add(new BasicNameValuePair("client_id", "master_patient_index_api"));
		form.add(new BasicNameValuePair("grant_type", "client_credentials"));
		form.add(new BasicNameValuePair("client_secret", "c1742c9e-d9cc-4450-bea6-f1be317d5dae"));
		form.add(new BasicNameValuePair("scope", "openid email"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

		// HttpEntity form;
		HttpUriRequest request = RequestBuilder.post().setUri(uriBuilder.build()).setEntity(entity).addHeader(
			"Content-Type", "application/x-www-form-urlencoded").addHeader("Accept-Charset", "utf-8").addHeader(
				"Accept", "application/json; charset=utf-8").build();
		HttpResponse response = httpClient.execute(request);

		HttpEntity responseEntity = response.getEntity();
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			logger.error(String.format("TOKEN EXCEPTION: %s\n", response.getStatusLine().toString()));
			responseEntity.writeTo(System.err);
			throw new RuntimeException();
		}
		logger.info("TOKEN created: ");

		String responseString = EntityUtils.toString(responseEntity, "UTF-8");

		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(responseString);
			JSONObject jsonObject = (JSONObject) obj;
			return (String) jsonObject.get("access_token");

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return responseString;

	}

}
