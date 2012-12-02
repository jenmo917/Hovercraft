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
bool encodeEngines(Engines engineSignals);
//USSensorData decodeUSSensorMsg();
bool encodeUSSensorMsg(USSensor sensorObject);
bool encodeI2CSensorMsg(I2CSensor sensorObject);

#endif /* NANOPB_H_ */
