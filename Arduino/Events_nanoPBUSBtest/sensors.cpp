/*
 * sensors.cpp
 *
 *  Created on: 19 nov 2012
 *      Author: ricda841
 */

#include "sensors.h"

int compassAddress = 0x42 >> 1; // From datasheet compass address is 0x42

struct sensor I2CsensorList[I2C_MAX_SENSORS] = {
		{
				"Compass",
				"Digital Compass",
				compassAddress,
		},
};

struct sensor sensorList[MAX_SENSORS] = {
		{
				"IR",
				"FrontLeft IR sensor",
				6,
		},
};

void sensorSetup()
{
	for (int i = 0; i < I2C_MAX_SENSORS; i++)
	{
		struct sensor empty;
		empty.type = "Empty";
		empty.description = "No sensor";
		empty.address = 0x40 >> 1;

		//memcpy(&compass[1], foo, sizeof(struct sensor));
		I2CsensorList[i] = empty;
	}

	for (int i = 0; i < MAX_SENSORS; i++)
	{
		struct sensor empty;
		empty.type = "Empty";
		empty.description = "No sensor";
		empty.address = 0x40 >> 1;

		//memcpy(&compass[1], foo, sizeof(struct sensor));
		sensorList[i] = empty;
	}

	I2CsensorList[0].type = "Compass";
	I2CsensorList[0].description = "Only one";
	I2CsensorList[0].address = 0x42 >> 1;
	I2CsensorList[0].value = 0;

	sensorList[0].type="IR";
	sensorList[0].description = "FrontLeft IR sensor";
	sensorList[0].address = 6;
}
