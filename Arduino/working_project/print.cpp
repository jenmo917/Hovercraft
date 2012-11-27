/*
 * print.cpp
 *
 *  Created on: 26 nov 2012
 *      Author: ricda841
 */
#include "print.h"
#include "Streaming.h"

void printRightMotorSignal(Engines engines)
{
	Serial << "Right Motor:" << endl;

	if(engines.right.enable)
	{
		Serial << "Enable: True" << endl;
	}
	else
	{
		Serial << "Enable: False" << endl;
	}
	if(engines.right.forward)
	{
		Serial << "Direction: Forward" << endl;
	}
	else
	{
		Serial << "Direction: Backward" << endl;
	}
	Serial << "Power: " << (int) engines.right.power <<  endl;
}

void printLeftMotorSignal(Engines engines)
{
	Serial << "Left Motor:" << endl;

	if(engines.left.enable)
	{
		Serial << "Enable: True" << endl;
	}
	else
	{
		Serial << "Enable: False" << endl;
	}
	if(engines.left.forward)
	{
		Serial << "Direction: Forward" << endl;
	}
	else
	{
		Serial << "Direction: Backward" << endl;
	}
	Serial << "Power: " << (int) engines.left.power <<  endl;
}

