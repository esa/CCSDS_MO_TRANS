/*******************************************************************************
 * Copyright or © or Copr. CNES
 *
 * This software is a computer program whose purpose is to provide a 
 * framework for the CCSDS Mission Operations services.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package org.ccsds.moims.mo.testbed.transport;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.Element;
import org.ccsds.moims.mo.mal.transport.MALEncodedBody;
import org.ccsds.moims.mo.mal.transport.MALEncodedElement;
import org.ccsds.moims.mo.mal.transport.MALMessageBody;

public class TestMessageBody implements MALMessageBody
{

  private Element bodyElement;
  
  public TestMessageBody(Element bodyElement)
  {
    this.bodyElement = bodyElement;
  }

  public MALEncodedBody getBodyElement() throws MALException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object getBodyElement(int index, Object element)
      throws IllegalArgumentException, MALException
  {
    switch (index)
    {
    case 0:
      return bodyElement;
    default:
      throw new MALException("Out of bound index");
    }
  }

  public int getElementCount()
  {
    return 1;
  }

  public MALEncodedElement getEncodedBodyElement(int index) throws MALException
  {
    throw new MALException("Invalid call");
  }

  @Override
  public String toString()
  {
    return "TestMessageBody [bodyElement=" + bodyElement + "]";
  }

	public MALEncodedBody getEncodedBody() throws MALException {
		throw new MALException("Not implemented");
  }
}
