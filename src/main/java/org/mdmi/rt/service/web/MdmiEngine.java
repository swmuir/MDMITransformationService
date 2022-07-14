package org.mdmi.rt.service.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.mdmi.core.Mdmi;
import org.mdmi.core.engine.MdmiUow;
import org.mdmi.core.engine.postprocessors.CDAPostProcessor;
import org.mdmi.core.engine.semanticprocessors.LogSemantic;
import org.mdmi.core.engine.semanticprocessors.LogSemantic.DIRECTION;
import org.mdmi.core.engine.terminology.FHIRTerminologyTransform;
import org.mdmi.core.runtime.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/mdmi/transformation")
public class MdmiEngine {

	@Autowired
	FHIRTerminologySettings terminologySettings;

	@Autowired
	FHIRClientSettings fhirClientSettings;

	@Autowired
	ServletContext context;

	// /us-east4/datasets/dev-zanenet-njinck/fhirStores/dev-mdix-datastore2

	static Boolean loaded = Boolean.FALSE;

	@Value("#{systemProperties['mdmi.maps'] ?: '/maps'}")
	private String mapsFolder;

	@Value("#{systemProperties['GOOGLE_APPLICATION_CREDENTIALS'] ?: '/credentials/google_application_credentials.json'}")
	private String credentials;

	@Value("#{systemProperties['your_project_id'] ?: 'zanenet-njinck'}")
	private String your_project_id;

	@Value("#{systemProperties['your_region_id'] ?: 'us-east4'}")
	private String your_region_id;

	@Value("#{systemProperties['your_dataset_id'] ?: 'dev-zanenet-njinck'}")
	private String your_dataset_id;

	@Value("#{systemProperties['your_fhir_id'] ?: 'dev-mdix-datastore2'}")
	private String your_fhir_id;

	@Value("#{systemProperties['mpiurl'] ?: 'https://master-patient-index-test-ocp.nicheaimlabs.com/api/v1/patients/'}")
	private String mpiurl;

	@Value("#{systemProperties['mpi_client_id'] ?: 'master_patient_index_api'}")
	private String mpi_client_id;

	@Value("#{systemProperties['grant_type'] ?: 'client_credentials'}")
	private String mpi_grant_type;

	@Value("#{systemProperties['mpi_client_secret'] ?: 'c1742c9e-d9cc-4450-bea6-f1be317d5dae'}")
	private String mpi_client_secret;

	@Value("#{systemProperties['mpi_scope'] ?: 'openid email'}")
	private String mpi_scope;

	@Value("#{systemProperties['mpi_usetoken'] ?: 'true'}")
	private Boolean mpi_usetoken;

	@Value("#{systemProperties['mpi_tokenurl'] ?: 'https://iam.mynjinck.com/auth/realms/ocp/protocol/openid-connect/token'}")
	String mpi_tokenurl;

	/*
	 * List<NameValuePair> form = new ArrayList<>();
	 * form.add(new BasicNameValuePair("client_id", "master_patient_index_api"));
	 * form.add(new BasicNameValuePair("grant_type", "client_credentials"));
	 * form.add(new BasicNameValuePair("client_secret", "c1742c9e-d9cc-4450-bea6-f1be317d5dae"));
	 * form.add(new BasicNameValuePair("scope", "openid email"));
	 * UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
	 *
	 */

	private HashMap<String, Properties> mapProperties = new HashMap<String, Properties>();

	private static Logger logger = LoggerFactory.getLogger(MdmiEngine.class);

	private void loadMaps() throws IOException {
		synchronized (this) {
			if (loaded) {
				return;
			}

			FHIRTerminologyTransform.codeValues.clear();

			FHIRTerminologyTransform.processTerminology = true;

			FHIRTerminologyTransform.setFHIRTerminologyURL(terminologySettings.getUrl());

			FHIRTerminologyTransform.setUserName(terminologySettings.getUserName());

			FHIRTerminologyTransform.setPassword(terminologySettings.getPassword());

			Set<String> maps = Stream.of(new File(mapsFolder).listFiles()).filter(
				file -> (!file.isDirectory() && file.toString().endsWith("mdmi"))).map(File::getName).collect(
					Collectors.toSet());
			for (String map : maps) {
				InputStream targetStream = new FileInputStream(mapsFolder + "/" + map);
				try {
					Mdmi.INSTANCE().getResolver().resolve(targetStream);
				} catch (Exception exception) {
					logger.error("invalid map");
				}
			}
			loaded = Boolean.TRUE;
		}
	}

	private void reloadMaps() throws IOException {
		synchronized (this) {
			loaded = false;
			mapProperties.clear();
			loadMaps();
		}
	}

	private Properties getMapProperties(String target) {
		if (!mapProperties.containsKey(target)) {
			Properties properties = new Properties();
			Path propertyFile = Paths.get(context.getRealPath(mapsFolder + "/" + target + ".properties"));
			if (Files.exists(propertyFile)) {
				try {
					properties.load(Files.newInputStream(propertyFile));
				} catch (IOException e) {
				}
			}
			Path valuesFile = Paths.get(context.getRealPath(mapsFolder + "/" + target + ".json"));
			if (Files.exists(valuesFile)) {
				try {
					properties.put("InitialValues", new String(Files.readAllBytes(valuesFile)));
				} catch (IOException e) {
				}
			}
			mapProperties.put(target, properties);
		}
		return mapProperties.get(target);
	}

	@GetMapping
	public String get(HttpServletRequest req) throws Exception {
		loadMaps();
		return Mdmi.INSTANCE().getResolver().getActiveMaps();
	}

	@GetMapping(path = "reset")
	public String reset(HttpServletRequest req) throws Exception {
		reloadMaps();
		return Mdmi.INSTANCE().getResolver().getActiveMaps();
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public String transformation(@Context HttpServletRequest req, @RequestParam("source") String source,
			@RequestParam("target") String target, @RequestPart("message") MultipartFile uploadedInputStream)
			throws Exception {
		logger.debug("DEBUG Start transformation ");
		loadMaps();
		MdmiUow.setSerializeSemanticModel(false);

		// Set Stylesheet for CDA document section generation
		CDAPostProcessor.setStylesheet("perspectasections.xsl");

		// add in fhir post processor

		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new Deliminated2XML("NJ", "\\|"));
		String fhirStoreName = String.format(
			FhirResourceCreate.FHIR_NAME, your_project_id, your_region_id, your_dataset_id, your_fhir_id);

		Mdmi.INSTANCE().getPostProcessors().addPostProcessor(
			new FHIRR4PostProcessorJson(
				credentials, fhirStoreName, mpiurl, mpi_client_id, mpi_grant_type, mpi_client_secret, mpi_scope,
				mpi_tokenurl, mpi_usetoken));

		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new HL7V2MessagePreProcessor());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new PreProcessorForFHIRJson());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new CDAPreProcesor());
		Mdmi.INSTANCE().getSourceSemanticModelProcessors().addSourceSemanticProcessor(new LogSemantic(DIRECTION.TO));
		Mdmi.INSTANCE().getTargetSemanticModelProcessors().addTargetSemanticProcessor(new LogSemantic(DIRECTION.FROM));

		String result = RuntimeService.runTransformation(
			source, uploadedInputStream.getBytes(), target, null, getMapProperties(source), getMapProperties(target));
		return result;
	}

	@PostMapping(path = "transformAndPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public String transformAndPost(@Context HttpServletRequest req, @RequestParam("source") String source,
			@RequestParam("target") String target, @RequestPart("message") MultipartFile uploadedInputStream)
			throws Exception {

		logger.debug("DEBUG Start transformation ");
		loadMaps();
		MdmiUow.setSerializeSemanticModel(false);

		// Set Stylesheet for CDA document section generation
		CDAPostProcessor.setStylesheet("perspectasections.xsl");

		// add in fhir post processor

		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new Deliminated2XML("NJ", "\\|"));
		String fhirStoreName = String.format(
			FhirResourceCreate.FHIR_NAME, your_project_id, your_region_id, your_dataset_id, your_fhir_id);

		Mdmi.INSTANCE().getPostProcessors().addPostProcessor(
			new FHIRR4PostProcessorJson(
				credentials, fhirStoreName, mpiurl, mpi_client_id, mpi_grant_type, mpi_client_secret, mpi_scope,
				mpi_tokenurl, mpi_usetoken));

		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new HL7V2MessagePreProcessor());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new PreProcessorForFHIRJson());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new CDAPreProcesor());
		Mdmi.INSTANCE().getSourceSemanticModelProcessors().addSourceSemanticProcessor(new LogSemantic(DIRECTION.TO));
		Mdmi.INSTANCE().getTargetSemanticModelProcessors().addTargetSemanticProcessor(new LogSemantic(DIRECTION.FROM));

		String result = RuntimeService.runTransformation(
			source, uploadedInputStream.getBytes(), target, null, getMapProperties(source), getMapProperties(target));
		// System.err.println(result);
		return FhirResourceCreate.postBundle(credentials, fhirStoreName, result);
	}

}
