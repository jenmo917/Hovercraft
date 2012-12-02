/*
 * sensors.cpp
 *
 *  Created on: 19 nov 2012
 *      Author: ricda841
 */

#include "sensors.h"

int compassAddress = 0x42 >> 1; // From datasheet compass address is 0x42

struct I2CSensor I2CSensorList[I2C_MAX_SENSORS];
struct USSensor USSensorList[MAX_US_SENSORS];

/**
* \brief Setup for sensors
*
* Setup for sensors
*
* @return No return
*
* \author Rickard Dahm
*
*/
void sensorSetup()
{
	for (int i = 0; i < I2C_MAX_SENSORS; i++)
	{
		struct I2CSensor empty;
		empty.type = "Empty";
		empty.description = "No sensor";
		empty.address = 0x40 >> 1;
		empty.value = 0;
		//memcpy(&compass[1], foo, sizeof(struct sensor));
		I2CSensorList[i] = empty;
	}

	for (int i = 0; i < MAX_US_SENSORS; i++)
	{
		struct USSensor empty;
		empty.type = "Empty";
		empty.description = "No sensor";
		empty.echopin = 0;
		empty.triggerpin = 0;
		empty.value = 0;

		//memcpy(&compass[1], foo, sizeof(struct sensor));
		USSensorList[i] = empty;
	}

	I2CSensorList[0].type = "Compass";
	I2CSensorList[0].description = "Only one";
	I2CSensorList[0].address = 0x42 >> 1;

	int triggerPins[4] = {trigPin1, trigPin2, trigPin3, trigPin4};
	int echoPins[4] = {echoPin1, echoPin2, echoPin3, echoPin4};

	for (int i = 0; i < 2; i++)
	{
		USSensorList[i].type="Ultrasonic";
		USSensorList[i].echopin = echoPins[i];
		USSensorList[i].triggerpin = triggerPins[i];
		USSensorList[i].value = 0;
	}
	USSensorList[0].description = "frontRight";
	USSensorList[1].description = "frontLeft";
	USSensorList[2].description = "backRight";
	USSensorList[3].description = "backLeft";
}
