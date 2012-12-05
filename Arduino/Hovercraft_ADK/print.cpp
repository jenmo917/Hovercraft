/*
 * print.cpp
 *
 *  Created on: 26 nov 2012
 *      Author: ricda841
 */
#include "print.h"
#include "Streaming.h"

/**
* \brief Prints info from an Engines object
*
* Prints info from an Engines object
*
* @param motors A pointer to a Engines object
*
* @return No return
*
* \author Rickard Dahm
*
*/
void printMotorSignal( Engines *motors )
{
	printRightMotorSignal( &motors->right );
	printLeftMotorSignal( &motors->left );
}

/**
* \brief Prints info from the right driveSignal
*
* Prints info from the right driveSignal
*
* @param right A pointer to a DriveSignals object
*
* @return No return
*
* \author Rickard Dahm
*
*/
void printRightMotorSignal( DriveSignals* right )
{
	Serial << "Right Motor:" << endl;

	if( right->enable )
	{
		Serial << "Enable: True" << endl;
	}
	else
	{
		Serial << "Enable: False" << endl;
	}
	if( right->forward )
	{
		Serial << "Direction: Forward" << endl;
	}
	else
	{
		Serial << "Direction: Backward" << endl;
	}
	Serial << "Power: " << (int) right->power <<  endl;
}

/**
* \brief Prints info from the left driveSignal
*
* Prints info from the left driveSignal
*
* @param left A pointer to a DriveSignals object
*
* @return No return
*
* \author Rickard Dahm
*
*/
void printLeftMotorSignal( DriveSignals* left )
{
	Serial << "Left Motor:" << endl;

	if( left->enable )
	{
		Serial << "Enable: True" << endl;
	}
	else
	{
		Serial << "Enable: False" << endl;
	}
	if( left->forward )
	{
		Serial << "Direction: Forward" << endl;
	}
	else
	{
		Serial << "Direction: Backward" << endl;
	}
	Serial << "Power: " << (int) left->power <<  endl;
}

