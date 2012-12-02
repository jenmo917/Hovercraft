/*
 * I2C.cpp
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */
#include "I2C.h"
#include "sensors.h"

int compassAddress = 0x42 >> 1; // From datasheet compass address is 0x42
// shift the address 1 bit right, the Wire library only needs the 7
// most significant bits for the address
int reading = 0;

struct sensor sensorList[I2C_MAX_SENSORS] = {
		{
			"Compass",
			"Digital Compass",
			compassAddress,
		},
	};

void I2CSetup()
{
	sensorList[0].type = "Compass";
	sensorList[0].description = "Only one";
	sensorList[0].address = 0x42 >> 1;
	//sensorList.pushback(compass);
	Wire.begin();       // join i2c bus (address optional for master)

	struct sensor foo;
	foo.type = "compass";
	foo.description = "Compass 2";
	foo.address = 0x43 >> 1;

	//memcpy(&compass[1], foo, sizeof(struct sensor));
	sensorList[1] = foo;
}


