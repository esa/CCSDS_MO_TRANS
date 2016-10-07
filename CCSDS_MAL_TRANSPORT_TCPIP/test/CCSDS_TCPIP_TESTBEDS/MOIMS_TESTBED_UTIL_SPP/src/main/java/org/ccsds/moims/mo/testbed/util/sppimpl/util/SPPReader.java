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

import java.io.IOException;
import java.io.InputStream;

import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacketHeader;

public class SPPReader {
  
  private byte[] apidQualifierBuffer;
  
  private byte[] inHeaderBuffer;
  
  //private byte[] inCrcBuffer;
  
  private InputStream is;
  
  public SPPReader(InputStream is) {
    this.is = is;
    apidQualifierBuffer = new byte[2];
    inHeaderBuffer = new byte[6];
    //inCrcBuffer = new byte[2];
  }
  
  private int read(final byte[] b, final int initialOffset, final int totalLength) throws IOException {
    int n;
    int len = 0;
    do {
      n = is.read(b, initialOffset + len, totalLength - len);
      if (n != -1) {
        len += n;
      }
    } while (len < totalLength);
    return len;
  }
  
  public SpacePacket receive() throws Exception {
    // 1- Read the APID qualifier
    read(apidQualifierBuffer, 0, 2);
    int apidQualifier = (((apidQualifierBuffer[0] & 0xFF) << 8) | (apidQualifierBuffer[1] & 0xFF));

    SpacePacketHeader header = new SpacePacketHeader();
    byte[] body = new byte[65536];
    SpacePacket packet = new SpacePacket(header, body, 0, body.length);
    
    packet.setApidQualifier(apidQualifier);
    
    // 2- Read the Space Packet
    read(inHeaderBuffer, 0, 6);
    int pk_ident = inHeaderBuffer[0] & 0xFF;
    pk_ident = (pk_ident<<8) | (inHeaderBuffer[1] & 0xFF);
    int vers_nb = (pk_ident>>13) & 0x0007;
    int pkt_type = (pk_ident>>12) & 0x0001;
    int sec_head_flag = (pk_ident>>11) & 0x0001;
    int apid = pk_ident & 0x07FF;
    
    int pkt_seq_ctrl = inHeaderBuffer[2] & 0xFF;
    pkt_seq_ctrl = (pkt_seq_ctrl<<8) | (inHeaderBuffer[3] & 0xFF);
    int segt_flag = (pkt_seq_ctrl>>14) & 0x0003;
    int seq_count = pkt_seq_ctrl & 0x3FFF;
    
    int pkt_length_value = inHeaderBuffer[4] & 0xFF;
    pkt_length_value = ((pkt_length_value<<8) | (inHeaderBuffer[5] & 0xFF)) + 1;
    
    SpacePacketHeader sph = packet.getHeader();
    sph.setApid(apid);
    sph.setSecondaryHeaderFlag(sec_head_flag);
    sph.setPacketType(pkt_type);
    sph.setPacketVersionNumber(vers_nb);
    sph.setSequenceCount(seq_count);
    sph.setSequenceFlags(segt_flag);
    
    // Don't read the CRC (last two bytes)
    //int dataLength = pkt_length_value - 2;
    
    int dataLength = pkt_length_value;
    byte[] data = packet.getBody();
    packet.setLength(dataLength);
    read(data, packet.getOffset(), dataLength);
    
    /*
    int CRC = SPPHelper.computeCRC(inHeaderBuffer, data, packet.getOffset(), dataLength);
  
    // Read CRC
    is.read(inCrcBuffer);
    int readCRC = inCrcBuffer[0] & 0xFF;
    readCRC = (readCRC<<8) | (inCrcBuffer[1] & 0xFF);
    
    if (CRC != readCRC) throw new Exception("CRC Error: expected=" + CRC + " , read=" + readCRC);
    */
    
    return packet;
  }
}
