/*
 * sensors.h
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */

#ifndef SENSORS_H_
#define SENSORS_H_

#include <Arduino.h>

#define I2C_MAX_SENSORS 5
#define MAX_SENSORS 4

extern struct sensor I2CsensorList[I2C_MAX_SENSORS];
extern struct sensor sensorList[MAX_SENSORS];

struct sensor
{
	String type;  //compass = 1
	String description;
	int address;
	int value;
};

void sensorSetup();


#endif /* SENSORS_H_ */
