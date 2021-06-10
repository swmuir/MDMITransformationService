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

import org.mdmi.MessageModel;
import org.mdmi.core.MdmiMessage;
import org.mdmi.core.engine.preprocessors.IPreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.GenericModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

/**
 * @author seanmuir
 *
 */
public class HL7V2MessagePreProcessor implements IPreProcessor {

	private static Logger logger = LoggerFactory.getLogger(HL7V2MessagePreProcessor.class);

	HapiContext genericContext = null;

	PipeParser genericParser = null;

	@Override
	public String getName() {
		return "HL7V2PreProcessor";
	}

	/**
	 *
	 */
	public HL7V2MessagePreProcessor() {
		super();
		genericContext = new DefaultHapiContext();
		genericContext.setModelClassFactory(new GenericModelClassFactory());
		genericParser = genericContext.getPipeParser();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.postprocessors.IPostProcessor#canProcess(org.mdmi.MessageModel)
	 */
	@Override
	public boolean canProcess(MessageModel messageModel) {
		if ("HL7V2".equals(messageModel.getGroup().getName())) {
			logger.trace("HL7V2 Pre Processing is Enabled");
			return true;
		}
		logger.trace("HL7V2 Pre Processing is not Enabled");
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.postprocessors.IPostProcessor#processMessage(org.mdmi.MessageModel, org.mdmi.core.MdmiMessage)
	 */
	@Override
	public void processMessage(MessageModel messageModel, MdmiMessage message) {

		Message msg;

		if (logger.isTraceEnabled()) {
			logger.trace(message.getDataAsString());
		}
		/**
		 * @TODO
		 *       This appears to be an issue with the HAPI parser itself
		 *       Need to investigate further if this in fact the appropriate fix
		 *       https://sourceforge.net/p/hl7api/mailman/message/33160852/
		 *
		 */

		try (HapiContext messageContext = new DefaultHapiContext()) {

			messageContext.getExecutorService();

			String theMessage = message.getDataAsString().replace("MSH|^~\\&#", "MSH|^~\\&");

			if (!theMessage.contains("\r")) {
				theMessage = theMessage.replaceAll("\n", "\r\n");
			}

			logger.trace(theMessage);

			logger.trace("message version is " + genericParser.getVersion(theMessage));

			CanonicalModelClassFactory mcf = new CanonicalModelClassFactory("2.6");
			messageContext.setModelClassFactory(mcf);
			PipeParser messageParser = messageContext.getPipeParser();
			messageParser.setValidationContext(new NoValidation());
			msg = messageParser.parse(theMessage);
			XMLParser xmlParser = messageContext.getXMLParser();
			logger.trace(msg.encode());
			message.setData(xmlParser.encode(msg).getBytes());
			if (logger.isTraceEnabled()) {
				logger.trace(message.getDataAsString());
			}
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());

		} catch (HL7Exception e) {
			logger.error(e.getLocalizedMessage());
		}

	}

}
