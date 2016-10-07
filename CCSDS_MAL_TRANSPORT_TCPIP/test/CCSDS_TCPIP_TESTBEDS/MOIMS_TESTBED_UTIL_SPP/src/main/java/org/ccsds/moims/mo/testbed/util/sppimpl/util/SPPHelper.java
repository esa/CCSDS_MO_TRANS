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
package org.ccsds.moims.mo.testbed.util.sppimpl.util;

public class SPPHelper {
  
  /**
   * Table used to compute the CRC
   */
  private static int[] lookUpTable;
  
  static {
    lookUpTable = new int[256];
    for (int i = 0; i < 256; i++) {
      int tmp = 0;
      if ((i & 0x01) != 0)
        tmp = tmp ^ 0x1021;
      if ((i & 0x02) != 0)
        tmp = tmp ^ 0x2042;
      if ((i & 0x04) != 0)
        tmp = tmp ^ 0x4084;
      if ((i & 0x08) != 0)
        tmp = tmp ^ 0x8108;
      if ((i & 0x10) != 0)
        tmp = tmp ^ 0x1231;
      if ((i & 0x20) != 0)
        tmp = tmp ^ 0x2462;
      if ((i & 0x40) != 0)
        tmp = tmp ^ 0x48C4;
      if ((i & 0x80) != 0)
        tmp = tmp ^ 0x9188;
      lookUpTable[i] = tmp;
    }
  }
  
  public static int computeCRC(byte[] header, byte[] data, int offset, int length) {
    int CRC = 0xFFFF;
    for (int i = 0; i < header.length; i++) {
      CRC = ((CRC<<8) & 0xFF00) ^ lookUpTable[(((CRC >> 8) ^ header[i]) & 0x00FF)];
    }
    for (int i = offset; i < offset + length; i++) {
      CRC = ((CRC<<8) & 0xFF00) ^ lookUpTable[(((CRC >> 8) ^ data[i]) & 0x00FF)];
    }
    return CRC;
  }

}
