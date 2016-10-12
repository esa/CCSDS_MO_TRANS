cd CCSDS_MO_TRANS/
mvn -P ESA clean install
cd ../CCSDS_TCPIP_TESTBEDS/MOIMS_TESTBED_POM/
mvn clean install
cd ../MOIMS_TESTBED_UTIL/
mvn -P ESA clean install
cd ../MOIMS_TESTBED_MAL/
mvn -P ESA_TCPIP clean install
