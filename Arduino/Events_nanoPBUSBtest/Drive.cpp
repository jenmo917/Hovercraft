/*
 * Drive.cpp
 *
 *  Created on: 9 nov 2012
 *      Author: ricda841
 */

#include <Arduino.h>
#include "Drive.h"
#include "pins.h"
#include "command.pb.h"

int rightPower;
int leftPower;

void driveSetup()
{
	int eraser = 7;
	TCCR3B &= ~eraser;
	int prescaler = 1;
	TCCR3B |= prescaler;
}

void rightMotorControl(DriveSignals signal)
{
	enableRightMotor(signal.enable);
	setRightDir((int) signal.forward);
	setRightPower((int) signal.power);
}

// dir = 0 => backward, dir = 1 => forward
void setRightDir(int dir)
{
	digitalWrite(rightDir, dir);
}

void setRightPower(int power)
{
	rightPower = power;
	//Serial.println("setRightPower: " + power);
	analogWrite( rightPWM , power );
}

void enableRightMotor(bool enable)
{
	digitalWrite( rightEnable , (int) !enable);
}

void leftMotorControl(DriveSignals signal)
{
	enableLeftMotor(signal.enable);
	setLeftDir((int) signal.forward);
	setLeftPower((int) signal.power);
}

// dir = 0 => backward, dir = 1 => forward
void setLeftDir(int dir)
{
	digitalWrite(leftDir, dir);
}

void setLeftPower(int power)
{
	leftPower = (int) power;
	analogWrite( leftPWM , (int) power );
}

void enableLeftMotor(bool enable)
{
	digitalWrite( leftEnable , (int) !enable);
}

void enableMotors()
{
	digitalWrite( rightEnable , 0 );
	digitalWrite( leftEnable , 0 );
}


