/*
 * nanoPB.h
 *
 *  Created on: 20 nov 2012
 *      Author: ricda841
 */

#ifndef NANOPB_H_
#define NANOPB_H_
#include "command.pb.h"
#include "sensors.h"

Engines decodeEngines();
void encodeMsg();
bool encodeEngines( Engines engineSignals );
//USSensorData decodeUSSensorMsg();
bool encodeUSSensorMsg( USSensor sensorObject );
bool encodeI2CSensorMsg( I2CSensor sensorObject );
bool encodeI2CSensorListMsg();
void fillI2CSensorFields( I2CSensorData* sensorToBeFilled, int number );
bool encodeUSSensorListMsg();
void fillUSSensorFields( USSensorData* sensorToBeFilled, int number );
void prepareUSSensorsForUSBTransfer();
String convertInt(int number);

#endif /* NANOPB_H_ */
