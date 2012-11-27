/*
 * Drive.cpp
 *
 *  Created on: 9 nov 2012
 *      Author: ricda841
 */

#include <Arduino.h>
#include "Drive.h"
#include "pins.h"

int rightPower;
int leftPower;

void driveSetup()
{
	int eraser = 7;
	TCCR3B &= ~eraser;
	int prescaler = 1;
	TCCR3B |= prescaler;
}

// dir = 0 => backward, dir = 1 => forward
void setRightDir(int dir)
{
	digitalWrite(rightDir, dir);
}

void setRightPower(int* power)
{
	rightPower = *power;
	//Serial.println("setRightPower: " + power);
	analogWrite( rightPWM , *power );
}

// dir = 0 => backward, dir = 1 => forward
void setLeftDir(int dir)
{
	digitalWrite(leftDir, dir);
}

void setLeftPower(byte* power)
{
	leftPower = (int) *power;
	analogWrite( leftPWM , (int) *power );
}

void enableMotors()
{
	digitalWrite( rightEnable , 0 );
	digitalWrite( leftEnable , 0 );
}


