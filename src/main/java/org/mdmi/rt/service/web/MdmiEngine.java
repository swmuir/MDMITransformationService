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
import org.mdmi.core.engine.terminology.FHIRTerminologyTransform;
import org.mdmi.core.runtime.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	ServletContext context;

	static Boolean loaded = Boolean.FALSE;

	@Value("#{systemProperties['mdmi.maps'] ?: '/maps'}")
	private String mapsFolder;

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
				Mdmi.INSTANCE().getResolver().resolve(targetStream);
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
		Mdmi.INSTANCE().getPostProcessors().addPostProcessor(new FHIRR4PostProcessorJson());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new HL7V2MessagePreProcessor());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new PreProcessorForFHIRJson());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new CDAPreProcesor());

		String result = RuntimeService.runTransformation(
			source, uploadedInputStream.getBytes(), target, null, getMapProperties(source), getMapProperties(target));
		return result;
	}

	@PostMapping(path = "byvalue", consumes = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces =

	{ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public String transformation2(@Context HttpServletRequest req, @RequestParam("source") String source,
			@RequestParam("target") String target, @RequestBody String message) throws Exception {
		logger.debug("DEBUG Start transformation ");
		loadMaps();
		MdmiUow.setSerializeSemanticModel(false);

		// Set Stylesheet for CDA document section generation
		CDAPostProcessor.setStylesheet("perspectasections.xsl");
		Mdmi.INSTANCE().getPostProcessors().addPostProcessor(new FHIRR4PostProcessorJson());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new HL7V2MessagePreProcessor());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new PreProcessorForFHIRJson());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new CDAPreProcesor());

		// add in fhir post processor
		// Mdmi.INSTANCE().getPostProcessors().addPostProcessor(new FHIRR4JsonPostProcessor());
		String result = RuntimeService.runTransformation(
			source, message.getBytes(), target, null, getMapProperties(source), getMapProperties(target));
		return result;
	}

}
