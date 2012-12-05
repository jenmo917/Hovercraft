/*
 * sensors.h
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */

#ifndef SENSORS_H_
#define SENSORS_H_

#include <Arduino.h>
#include "command.pb.h"
#include "pins.h"

#define I2C_MAX_SENSORS 5
#define MAX_US_SENSORS 4

extern struct I2CSensor I2CSensorList[ I2C_MAX_SENSORS ];
extern struct USSensor USSensorList[ MAX_US_SENSORS ];

struct I2CSensor
{
	String type;  //compass = 1
	String description;
	int address;
	int value;
};

struct USSensor
{
	String type;  //compass = 1
	String description;
	int echopin;
	int triggerpin;
	int value;
};



void sensorSetup();

#endif /* SENSORS_H_ */
