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

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.mdmi.MessageModel;
import org.mdmi.core.MdmiMessage;
import org.mdmi.core.engine.preprocessors.IPreProcessor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.IParserErrorHandler;
import ca.uhn.fhir.parser.json.JsonLikeValue.ScalarType;
import ca.uhn.fhir.parser.json.JsonLikeValue.ValueType;

/**
 * @author seanmuir
 *
 */
public class PreProcessorForFHIRJson implements IPreProcessor {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.preprocessors.IPreProcessor#canProcess(org.mdmi.MessageModel)
	 */
	@Override
	public boolean canProcess(MessageModel messageModel) {
		if ("xFHIRR4JSON".equals(messageModel.getGroup().getName())) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.preprocessors.IPreProcessor#getName()
	 */
	@Override
	public String getName() {
		return "PreProcessorForFHIRJson";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.preprocessors.IPreProcessor#processMessage(org.mdmi.MessageModel, org.mdmi.core.MdmiMessage)
	 */
	@Override
	public void processMessage(MessageModel messageModel, MdmiMessage mdmiMessage) {
		FhirContext ctx = FhirContext.forR4();

		if (ctx != null) {

			IParser parse = ctx.newJsonParser();
			IParserErrorHandler aaa = new IParserErrorHandler() {

				@Override
				public void containedResourceWithNoId(IParseLocation arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void incorrectJsonType(IParseLocation arg0, String arg1, ValueType arg2, ScalarType arg3,
						ValueType arg4, ScalarType arg5) {
					// TODO Auto-generated method stub

				}

				@Override
				public void invalidValue(IParseLocation arg0, String arg1, String arg2) {
					// TODO Auto-generated method stub

				}

				@Override
				public void missingRequiredElement(IParseLocation arg0, String arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void unexpectedRepeatingElement(IParseLocation arg0, String arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void unknownAttribute(IParseLocation arg0, String arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void unknownElement(IParseLocation arg0, String arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void unknownReference(IParseLocation arg0, String arg1) {
					// TODO Auto-generated method stub

				}
			};
			parse.setParserErrorHandler(aaa);

			// Bundle bundle = ctx.newJsonParser().parseResource(Bundle.class, mdmiMessage.getDataAsString());

			IBaseResource b = parse.parseResource(mdmiMessage.getDataAsString());

			// // System.out.println(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(b));

			mdmiMessage.setData(ctx.newXmlParser().encodeResourceToString(b));
			System.out.println(mdmiMessage.getDataAsString());
		}
	}

}
