/*
 * pins.cpp
 *
 *  Created on: 9 nov 2012
 *      Author: ricda841
 */

#include "pins.h"

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

void LEDPinsSetup()
{
	pinMode(LED, OUTPUT);
	digitalWrite( LED , 0 );
}

void rightMotorPinsSetup()
{
	pinMode( rightEnable, OUTPUT );
	digitalWrite( rightEnable , 1 );
	pinMode( rightPWM , OUTPUT );
	analogWrite( rightPWM , 0 );
	pinMode( rightDir, OUTPUT );
	digitalWrite( rightDir , 1 );
}

void leftMotorPinsSetup()
{
	pinMode( leftEnable, OUTPUT );
	digitalWrite( leftEnable , 1 );
	pinMode( leftPWM , OUTPUT );
	analogWrite( leftPWM , 0 );
	pinMode( leftDir, OUTPUT );
	digitalWrite( leftDir , 1 );
}

void sensorPinsSetup()
{
	int triggerPins[4] = {trigPin1, trigPin2, trigPin3, trigPin4};
	int echoPins[4] = {echoPin1, echoPin2, echoPin3, echoPin4};
	for (int i = 0; i < 4; i++)
	{
		pinMode(triggerPins[i], OUTPUT);
		pinMode(echoPins[i], INPUT);
	}
}

void testPins()
{
	pinMode(22, OUTPUT);
}
