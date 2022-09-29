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
package org.mdmi.rt.service.web;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.text.StringEscapeUtils;
import org.mdmi.Bag;
import org.mdmi.MessageModel;
import org.mdmi.core.MdmiMessage;
import org.mdmi.core.engine.preprocessors.IPreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author seanmuir
 *
 */
public class Deliminated2XML implements IPreProcessor {

	private String name;

	private String delim;

	private static Logger logger = LoggerFactory.getLogger(Deliminated2XML.class);

	/**
	 * @param name
	 * @param delim
	 */
	public Deliminated2XML(String name, String delim) {
		super();
		this.name = name;
		this.delim = delim;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.IPreProcessor#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.IPreProcessor#canProcess(org.mdmi.MessageModel)
	 */
	@Override
	public boolean canProcess(MessageModel messageModel) {
		if (messageModel != null && messageModel.getGroup() != null) {
			if (name.equals(messageModel.getGroup().getName())) {
				return true;
			}
		}
		return false;
	}

	boolean printXML = true;

	/*
	 *
	 * CLM_RCP_IDN
	 * CLM_MCARE_CLM_TYPE_CDE
	 * CLM_SVC_DTE
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.IPreProcessor#processMessage(org.mdmi.core.MdmiMessage)
	 */
	@Override
	public void processMessage(MessageModel messageModel, MdmiMessage message) {

		try {
			logger.info("Start process message");
			Reader inputString = new StringReader(message.getDataAsString());
			BufferedReader inputReader = new BufferedReader(inputString);
			List<String> lines = new ArrayList<>();
			for (;;) {
				String line;
				line = inputReader.readLine();
				if (line == null) {
					break;
				}
				if (line.startsWith("TRAILER")) {
					continue;
				}

				lines.add(line.replaceAll("\"", ""));
			}
			if (lines.isEmpty()) {
				logger.error("Error parsing inbound message");

				logger.error("message is " + message.getDataAsString());
				return;
			}
			logger.info("parsed message");
			if (messageModel.getSyntaxModel() != null && messageModel.getSyntaxModel().getRoot() instanceof Bag) {
				Bag rootBag = (Bag) messageModel.getSyntaxModel().getRoot();
				if (!rootBag.getNodes().isEmpty()) {
					String root = rootBag.getLocation();
					String element = messageModel.getMessageModelName();
					logger.info("Start toXML");
					if (printXML) {
						System.err.println(toXML(lines, delim, root, element).getBytes());
					}
					message.setData(toXML(lines, delim, root, element).getBytes());
					logger.info("end toXML");
				}

			}

		} catch (Exception e) {
			logger.error("processMessage error " + e.getMessage(), e);

		}

	}

	public String toXML(List<String> inputLines, String delim, String root, String elementName) {

		/*
		 * Use escape to java because of mishandling of \ in name space
		 */
		List<String> header = new ArrayList<>(
			Arrays.asList(inputLines.get(0).replaceAll("\"", "").split(delim))).stream().map(String::trim).collect(
				Collectors.toList());

		String output = "<" + root + ">" + System.lineSeparator() + inputLines.stream().skip(1).map(line -> {
			List<String> cells = Arrays.asList(line.split(delim));
			return "<" + elementName + ">" + System.lineSeparator() +
					IntStream.range(0, cells.size()).mapToObj(
						i -> "<" + header.get(i) + ">" +
								StringEscapeUtils.escapeHtml4(StringEscapeUtils.escapeJava(cells.get(i))) + "</" +
								header.get(i) + ">").collect(Collectors.joining(System.lineSeparator())) +
					"</" + elementName + ">" + System.lineSeparator();
		}).collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator() + "</" + root + ">";
		return "<?xml version=\"1.0\" ?>" + System.lineSeparator() + output + System.lineSeparator();
	}

}
