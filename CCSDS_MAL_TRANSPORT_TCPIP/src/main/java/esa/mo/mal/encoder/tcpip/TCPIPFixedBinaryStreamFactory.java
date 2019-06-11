/* ----------------------------------------------------------------------------
 * Copyright (C) 2014      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : CCSDS MO TCP/IP Transport Framework
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.mal.encoder.tcpip;

import esa.mo.mal.encoder.binary.base.BaseBinaryStreamFactory;
import esa.mo.mal.encoder.binary.base.BinaryTimeHandler;

/**
 * A factory implementation for the generation of input and output stream classes, which manage
 * decoding and encoding, respectively.
 *
 * @author Rian van Gijlswijk
 *
 */
public class TCPIPFixedBinaryStreamFactory extends BaseBinaryStreamFactory
{

  public TCPIPFixedBinaryStreamFactory()
  {
    super(TCPIPFixedBinaryElementInputStream.class, TCPIPFixedBinaryElementOutputStream.class,
        new BinaryTimeHandler());
  }
}
