/*
 * pins.cpp
 *
 *  Created on: 9 nov 2012
 *      Author: ricda841
 */

#include "pins.h"

/**
* \brief Setup all pins
*
* Setup all pins
*
* @return No return
*
* \author Rickard Dahm
*
*/
void pinSetup()
{
	//LED
	LEDPinsSetup();
	// Right motor controls
	rightMotorPinsSetup();
	// Left motor controls
	leftMotorPinsSetup();
	sensorPinsSetup();
}

/**
* \brief Setup LED pins
*
* Setup LED pins
*
* @return No return
*
* \author Rickard Dahm
*
*/
void LEDPinsSetup()
{
	pinMode( LED, OUTPUT );
	digitalWrite( LED , 0 );
}

/**
* \brief Setup right motors pins
*
* Setup right motors pins
*
* @return No return
*
* \author Rickard Dahm
*/
void rightMotorPinsSetup()
{
	pinMode( rightEnable, OUTPUT );
	digitalWrite( rightEnable , 1 );
	pinMode( rightPWM , OUTPUT );
	analogWrite( rightPWM , 0 );
	pinMode( rightDir, OUTPUT );
	digitalWrite( rightDir , 1 );
}

/**
* \brief Setup left motors pins
*
* Setup left motors pins
*
* @return No return
*
* \author Rickard Dahm
*/
void leftMotorPinsSetup()
{
	pinMode( leftEnable, OUTPUT );
	digitalWrite( leftEnable , 1 );
	pinMode( leftPWM , OUTPUT );
	analogWrite( leftPWM , 0 );
	pinMode( leftDir, OUTPUT );
	digitalWrite( leftDir , 1 );
}

/**
* \brief Setup pins for sensors
*
* Setup pins for sensors
*
* @return No return
*
* \author Rickard Dahm
*/
void sensorPinsSetup()
{
	int triggerPins[4] = { trigPin1, trigPin2, trigPin3, trigPin4 };
	int echoPins[4] = { echoPin1, echoPin2, echoPin3, echoPin4 };
	for (int i = 0; i < 4; i++)
	{
		pinMode( triggerPins[i], OUTPUT );
		pinMode( echoPins[i], INPUT );
	}
}

/**
* \brief Setup pins for software testing
*
* Setup pins for software testing
*
* @return No return
*
* \author Rickard Dahm
*/
void testPins()
{
	pinMode( 22, OUTPUT );
}
