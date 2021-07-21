/*******************************************************************************
 * Copyright (c) 2021 seanmuir.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.mdht.uml.cda.ClinicalDocument;
import org.eclipse.mdht.uml.cda.Section;
import org.eclipse.mdht.uml.cda.util.CDAUtil;
import org.eclipse.mdht.uml.cda.util.CDAUtil.Filter;
import org.eclipse.mdht.uml.hl7.datatypes.DatatypesPackage;
import org.eclipse.mdht.uml.hl7.datatypes.ED;
import org.mdmi.MessageModel;
import org.mdmi.core.MdmiMessage;
import org.mdmi.core.engine.preprocessors.IPreProcessor;

/**
 *
 * CDAPreProcesor walks the cda document and normalizes ED references from the narrative into inlined content
 * @author seanmuir
 *
 */
public class CDAPreProcesor implements IPreProcessor {

	static String getValue(Section section, ED ed) {
		if (section != null && section.getText() != null && ed != null) {
			if (ed.getReference() != null) {
				String reference = ed.getReference().getValue();
				if (!StringUtils.isEmpty(reference)) {
					String result = section.getText().getText(reference.substring(1));
					if (!StringUtils.isEmpty(result)) {
						return result;
					}
				} else {
					String mixed = ed.getReference().getText();
					if (!StringUtils.isEmpty(mixed)) {
						String result = section.getText().getText(mixed.substring(1));
						if (!StringUtils.isEmpty(result)) {
							return result;
						}
					}
				}
			}
		}
		return "";
	}

	static String getReferenceId(ED ed) {

		String reference = ed.getReference().getValue();
		if (!StringUtils.isEmpty(reference)) {
			return reference.substring(1);
		} else {
			String mixed = ed.getReference().getText();
			if (!StringUtils.isEmpty(mixed)) {
				return mixed.substring(1);
			}
		}

		return "";

	}

	public static class EDFilter implements Filter<ED> {

		public HashSet<String> referenceIds = new HashSet<String>();

		public ArrayList<EObject> duplications = new ArrayList<EObject>();

		@Override
		public boolean accept(ED ed) {
			if (ed.getReference() != null) {

				String referenceId = getReferenceId(ed);

				if (referenceIds.contains(referenceId)) {
					duplications.add(ed.eContainer());
				} else {
					referenceIds.add(referenceId);
				}

				Section section = CDAUtil.getSection(ed);
				if (section != null) {
					String result = getValue(section, ed);
					if (!StringUtils.isEmpty(result)) {
						EcoreUtil.delete(ed.getReference());
						try {
							ed.eUnset(
								ed.eClass().getEStructuralFeature(
									DatatypesPackage.eINSTANCE.getED_Reference().getFeatureID()));

							ed.eUnset(
								ed.eClass().getEStructuralFeature(
									DatatypesPackage.eINSTANCE.getED_Mixed().getFeatureID()));

							// DatatypesPackage.ED__MIXED
						} catch (AssertionError ae) {
							ae.printStackTrace();

						}
						ed.addText(result.trim());
					}
				}
			}
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.preprocessors.IPreProcessor#canProcess(org.mdmi.MessageModel)
	 */
	@Override
	public boolean canProcess(MessageModel messageModel) {
		if ("CDAR2".equals(messageModel.getGroup().getName())) {
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
		// TODO Auto-generated method stub
		return "CDAPreProcesor";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.preprocessors.IPreProcessor#processMessage(org.mdmi.MessageModel, org.mdmi.core.MdmiMessage)
	 */
	@Override
	public void processMessage(MessageModel messageModel, MdmiMessage mdmiMessage) {

		InputStream targetStream = new ByteArrayInputStream(mdmiMessage.getDataAsString().getBytes());

		try {
			ClinicalDocument sourceDocument = CDAUtil.load(targetStream);
			org.eclipse.mdht.uml.cda.util.CDAUtil.Query query = new org.eclipse.mdht.uml.cda.util.CDAUtil.Query(
				sourceDocument);

			EDFilter edFilter = new EDFilter();

			query.getEObjects(ED.class, edFilter);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CDAUtil.save(sourceDocument, baos);

			System.out.println(baos.toString());
			mdmiMessage.setData(baos.toString());

		} catch (Exception e) {
		}

	}

}
