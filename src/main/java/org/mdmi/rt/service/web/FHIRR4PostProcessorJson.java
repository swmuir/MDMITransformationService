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
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
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
import org.hl7.fhir.r4.model.Patient.LinkType;
import org.hl7.fhir.r4.model.Patient.PatientLinkComponent;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
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

	String mpiurl;

	private String mpi_client_id;

	private String mpi_grant_type;

	private String mpi_client_secret;

	private String mpi_scope;

	private Boolean mpi_usetoken;

	private String mpi_tokenurl;

	/**
	 * @param credentials
	 * @param fhirStoreName
	 * @param mpiurl
	 */
	public FHIRR4PostProcessorJson(String credentials, String fhirStoreName, String mpiurl, String mpi_client_id,
			String mpi_grant_type, String mpi_client_secret, String mpi_scope, String mpi_tokenurl,
			Boolean mpiusetoken) {
		this.credentials = credentials;
		this.fhirStoreName = fhirStoreName;
		this.mpiurl = mpiurl;

		this.mpi_client_id = mpi_client_id;

		this.mpi_grant_type = mpi_grant_type;

		this.mpi_client_secret = mpi_client_secret;

		this.mpi_scope = mpi_scope;

		this.mpi_usetoken = mpiusetoken;

		this.mpi_tokenurl = mpi_tokenurl;
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

	private static boolean skipReference = false;

	private void resolveReference(HashMap<String, String> referenceMappings, Reference reference, String resource) {
		if (skipReference) {
			return;
		}
		logger.trace("Start resolveReference for " + resource + " " + reference.getDisplay());
		if (reference != null && !StringUtils.isEmpty(reference.getDisplay())) {
			String referenceMappingsKey = resource + "_" + reference.getDisplay();

			if (referenceMappings.containsKey(referenceMappingsKey)) {
				logger.trace("cache " + referenceMappingsKey);
				if (referenceMappings.get(referenceMappingsKey) != null) {
					// System.err.println(referenceMappingsKey + " : " + referenceMappings.get(referenceMappingsKey));
					reference.setReference(resource + "/" + referenceMappings.get(referenceMappingsKey));
				}

			} else {
				logger.trace("query  " + referenceMappingsKey);
				try {

					logger.trace("Start FhirResourceCreate.query");
					String result = FhirResourceCreate.query(
						credentials, fhirStoreName,
						resource + "?identifier=" + URLEncoder.encode(reference.getDisplay(), StandardCharsets.UTF_8));
					logger.trace("End FhirResourceCreate.query");
					if (result != null) {
						reference.setReference(resource + "/" + result);
					}
					// System.err.println(referenceMappingsKey + " : " + result);
					referenceMappings.put(referenceMappingsKey, result);

				} catch (Exception e) {
					logger.error(e.getLocalizedMessage());

				}
			}
		}
		logger.trace("Done resolveReference for " + resource + " " + reference.getDisplay());

	}

	private String searchForExistingResource(String resourceType, Identifier identifier) {

		if (identifier != null) {
			if (!StringUtils.isEmpty(identifier.getValue())) {
				try {
					return FhirResourceCreate.query(
						credentials, fhirStoreName, resourceType + "?identifier=" + identifier.getValue());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		return null;
	}

	// static HashMap<String, String> referenceMappings = new HashMap<String, String>();

	public static class ReferenceHashMap extends LinkedHashMap<String, String> {

		private static final int MAX_ENTRIES = 100000;

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
		 */
		@Override
		protected boolean removeEldestEntry(Entry eldest) {
			return size() > MAX_ENTRIES;
		}
	}

	static ReferenceHashMap referenceMappings = new ReferenceHashMap();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.postprocessors.IPostProcessor#processMessage(org.mdmi.MessageModel, org.mdmi.core.MdmiMessage)
	 */
	@Override
	public void processMessage(MessageModel messageModel, MdmiMessage mdmiMessage) {

		String tokenRequest = null;
		if (this.mpi_usetoken) {
			try {
				tokenRequest = getAccessToken();
			} catch (Exception e1) {
				logger.error("getAccessToken() Error", e1);
			}
		}
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

				@Override
				public void extensionContainsValueAndNestedExtensions(IParseLocation theLocation) {
					// TODO Auto-generated method stub

				}
			};
			parse.setParserErrorHandler(doNothingHandler);

			// System.err.println(mdmiMessage.getDataAsString());
			Bundle bundle = parse.parseResource(Bundle.class, mdmiMessage.getDataAsString());

			// String asdf = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
			// System.err.println(asdf);

			deduplicate(bundle);

			for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
				UUID uuid = UUID.randomUUID();
				String resourceId = "urn:uuid:" + uuid;
				bundleEntry.setFullUrl(resourceId);

				try {
					Method getIdentifier = bundleEntry.getResource().getClass().getDeclaredMethod("getIdentifier");

					@SuppressWarnings("unchecked")
					List<Identifier> identifiers = (List<Identifier>) getIdentifier.invoke(bundleEntry.getResource());

					if (!identifiers.isEmpty()) {
						for (Identifier identifier : identifiers) {
							referenceMappings.put(identifier.getValue(), resourceId);
						}

					} else {
						identifiers.add(new Identifier().setValue(uuid.toString()));
					}

				} catch (Exception e) {
					logger.trace(e.getMessage());
				}
			}

			HashSet<String> claims = new HashSet<String>();
			HashSet<String> patients = new HashSet<String>();

			int resourceCount = 0;
			for (BundleEntryComponent bundleEntry : bundle.getEntry()) {

				if (bundleEntry.getResource().getResourceType() != null) {

					logger.trace("RESOURCE COUNT : " + ++resourceCount);

					ResourceType theResourceType = bundleEntry.getResource().getResourceType();
					if (theResourceType.equals(ResourceType.Patient)) {

						Patient patientResource = (Patient) bundleEntry.getResource();

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);
						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						if (!patientResource.getName().isEmpty()) {

							HumanName name = patientResource.getName().get(0);

							try {

								// UUID uuid = UUID.randomUUID();
								// String id = uuid.toString();
								String id = proccessMPI(
									tokenRequest, name.getFamily(), name.getGivenAsSingleString(),
									patientResource.getBirthDate());

								Identifier identifier = new Identifier();
								identifier.setId(id);
								identifier.setSystem(
									"https://master-patient-index-test-ocp.nicheaimlabs.com/api/v1/patients/");
								patientResource.getIdentifier().add(identifier);

								if (patients.contains(id)) {
									bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());
									bundleEntry.getRequest().setMethod(HTTPVerb.POST);

									PatientLinkComponent link = patientResource.addLink();
									// LinkType LinkType.SEEALSO;
									link.setType(LinkType.SEEALSO);
									Reference other = new Reference();
									other.setId(id);
									link.setOther(other);

								} else {
									patients.add(id);
									patientResource.setId(id);
									bundleEntry.getRequest().setUrl(
										bundleEntry.getResource().getResourceType().name() + "/" + id);
									bundleEntry.getRequest().setMethod(HTTPVerb.PUT);
								}

							} catch (Exception e) {
								logger.trace(e.getMessage());
							}
						}
						// else {
						// if (!patientResource.getIdentifier().isEmpty()) {
						// String pid = patientResource.getIdentifier().get(0).getValue();
						// String result;
						// try {
						// result = FhirResourceCreate.query(
						// credentials, fhirStoreName, "Patient?identifier=" + pid);
						// patientResource.setId(result);
						//
						// bundleEntry.getRequest().setUrl(
						// bundleEntry.getResource().getResourceType().name() + "/" + result);
						// bundleEntry.getRequest().setMethod(HTTPVerb.PUT);
						//
						// } catch (Exception e) {
						// logger.trace(e.getMessage());
						// }
						// }
						// }

					}

					if (theResourceType.equals(ResourceType.Coverage)) {
						Coverage coverage = (Coverage) bundleEntry.getResource();

						resolveReference(referenceMappings, coverage.getBeneficiary(), "Patient");
						for (Reference payor : coverage.getPayor()) {
							resolveReference(referenceMappings, payor, "Organization");
						}

						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);

					}

					if (theResourceType.equals(ResourceType.Claim)) {

						Claim claim = (Claim) bundleEntry.getResource();

						resolveReference(referenceMappings, claim.getProvider(), "Practitioner");
						resolveReference(referenceMappings, claim.getPatient(), "Patient");
						resolveReference(referenceMappings, claim.getInsurer(), "Organization");

						Identifier current = claim.getIdentifierFirstRep();

						if (!claims.contains(claim.getIdentifierFirstRep().getValue())) {
							bundleEntry.getRequest().setUrl(
								bundleEntry.getResource().getResourceType().name() + "/" + current.getValue());
							bundleEntry.getRequest().setMethod(HTTPVerb.PUT);
							claims.add(current.getValue());
						} else {

							Reference claimreference = new Reference();
							claimreference.setId(claim.getIdentifierFirstRep().getValue());

							claim.addRelated().setClaim(claimreference);
							bundleEntry.getRequest().setMethod(HTTPVerb.POST);
							bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());
						}

					}

					if (theResourceType.equals(ResourceType.ExplanationOfBenefit)) {
						bundleEntry.getRequest().setUrl(bundleEntry.getResource().getResourceType().name());

						bundleEntry.getRequest().setMethod(HTTPVerb.POST);

						ExplanationOfBenefit explanationOfBenefit = (ExplanationOfBenefit) bundleEntry.getResource();

						resolveReference(referenceMappings, explanationOfBenefit.getProvider(), "Practitioner");
						resolveReference(referenceMappings, explanationOfBenefit.getPatient(), "Patient");
						resolveReference(referenceMappings, explanationOfBenefit.getInsurer(), "Organization");
						resolveReference(referenceMappings, explanationOfBenefit.getClaim(), "Claim");

					}

					if (theResourceType.equals(ResourceType.Practitioner)) {

						Practitioner practitioner = (Practitioner) bundleEntry.getResource();

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
											bundleEntry.getRequest().setMethod(HTTPVerb.PUT);
											practitioner.setId(result);
											bundleEntry.getRequest().setUrl(
												bundleEntry.getResource().getResourceType().name() + "/" + result);
										}
									} catch (Exception e) {
										logger.trace(e.getMessage());
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

	boolean skipMPI = false;

	public String proccessMPI(String token, String family, String given, Date dob)
			throws URISyntaxException, ClientProtocolException, IOException {
		if (skipMPI) {
			UUID uuid = UUID.randomUUID();
			String resourceId = "" + uuid;
			return resourceId;
		}

		logger.debug("START MPI ");

		HttpClient httpClient = HttpClients.createDefault();

		URIBuilder uriBuilder = new URIBuilder(mpiurl);

		logger.debug(
			String.format(
				"{\"name\": [{\"use\": \"official\", \"family\": \"%s\", \"given\": [\"%s\"]}], \"dob\": \"%tF\", \"createIfNotExist\":\"True\"}",
				family, given, dob));

		StringEntity requestEntity = new StringEntity(
			String.format(
				"{\"name\": [{\"use\": \"official\", \"family\": \"%s\", \"given\": [\"%s\"]}], \"dob\": \"%tF\", \"createIfNotExist\":\"True\"}",
				family, given, dob));

		HttpUriRequest request = null;

		if (this.mpi_usetoken) {
			request = RequestBuilder.post().setUri(uriBuilder.build()).setEntity(requestEntity).addHeader(
				"Content-Type", "application/json").addHeader("Accept-Charset", "utf-8").addHeader(
					"Accept", "application/json; charset=utf-8").addHeader("Authorization", "Bearer " + token).build();

		} else {
			request = RequestBuilder.post().setUri(uriBuilder.build()).setEntity(requestEntity).addHeader(
				"Content-Type", "application/json").addHeader("Accept-Charset", "utf-8").addHeader(
					"Accept", "application/json; charset=utf-8").build();
		}
		HttpResponse response = httpClient.execute(request);

		HttpEntity responseEntity = response.getEntity();
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			logger.error(String.format("Exception MPI: %s\n", response.getStatusLine().toString()));
			// responseEntity.writeTo(System.err);
			throw new RuntimeException();
		}

		String responseString = EntityUtils.toString(responseEntity, "UTF-8");

		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(responseString);
			JSONArray array = (JSONArray) obj;
			JSONObject jsonObject = (JSONObject) array.get(0);

			logger.debug("Recieved MPI ");

			return (String) jsonObject.get("uuid");

		} catch (ParseException e) {
			logger.error(e.getLocalizedMessage());
		}

		logger.debug("END MPI ");
		return responseString;
	}

	public String getAccessToken() throws IOException, URISyntaxException {

		HttpClient httpClient = HttpClients.createDefault();

		URIBuilder uriBuilder = new URIBuilder(mpi_tokenurl);

		List<NameValuePair> form = new ArrayList<>();
		form.add(new BasicNameValuePair("client_id", mpi_client_id));
		form.add(new BasicNameValuePair("grant_type", mpi_grant_type));
		form.add(new BasicNameValuePair("client_secret", mpi_client_secret));
		form.add(new BasicNameValuePair("scope", mpi_scope));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

		// HttpEntity form;
		HttpUriRequest request = RequestBuilder.post().setUri(uriBuilder.build()).setEntity(entity).addHeader(
			"Content-Type", "application/x-www-form-urlencoded").addHeader("Accept-Charset", "utf-8").addHeader(
				"Accept", "application/json; charset=utf-8").build();
		HttpResponse response = httpClient.execute(request);

		HttpEntity responseEntity = response.getEntity();
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			logger.error(String.format("TOKEN EXCEPTION: %s\n", response.getStatusLine().toString()));
			// responseEntity.writeTo(System.err);
			throw new RuntimeException();
		}

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
