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
#include "Streaming.h"

int rightPower;
int leftPower;

/**
* \brief Change timer 3 frequency
*
* Change the prescaler for timer 3
* Results in a higher frequency and a smoother pwm signal
*
* @return No return
*
* \author Rickard Dahm
*
*/
void driveSetup()
{
	int eraser = 7;
	TCCR3B &= ~eraser;
	int prescaler = 1;
	TCCR3B |= prescaler;
}

/**
* \brief Change the outputs to the motors
*
* Used to control both motors
*
* @param signal A pointer to a Engines object
*
* @return No return
*
* \author Rickard Dahm
*
*/
void motorControl( Engines *motors )
{
	rightMotorControl( &motors->right );
	leftMotorControl( &motors->left );
}

/**
* \brief Change the outputs to the right motor
*
* Used to control the right motor
*
* @param signal A pointer to a DriveSignals object
*
* @return No return
*
* \author Rickard Dahm
*
*/
void rightMotorControl( DriveSignals *signal )
{
	setRightDir( (int) signal->forward );
	if( signal->enable != true )
	{
		setRightPower( 0 );
	}
	else
	{
		setRightPower( (int) signal->power );
		//Serial << "Power: " << (int) signal->power << endl;
 	}
	enableRightMotor( signal->enable );
}

/**
* \brief Sets the direction of the right motor.
*
* Sets the direction of the right motor.
* Changes the value on the pin defined as rightDir in pins.h
* @param dir Direction: dir = 0 = backwards, dir = 1 = forward
*
* @return No return
*
* \author Rickard Dahm
*
*/
void setRightDir( int dir )
{
	digitalWrite( rightDir, dir );
}

/**
* \brief Sets the dutycycle of the right motors PWM.
*
* Sets the dutycycle of the right motors PWM.
*
* @param power A int between 0 and 255. 0 means 0% dutycycle while 255 means 100%
*
* @return No return
*
* \author Rickard Dahm
*
*/
void setRightPower( int power )
{
	if( power > 255 )
	{
		power = 255;
		Serial << "Error: Power for right motor was larger than 255, power = 255 will be used!" << endl;
	}
	else if( power < 0 )
	{
		power = 0;
		Serial << "Error: Power for right motor was less than 0, power = 0 will be used!" << endl;
	}

	rightPower = power;
	analogWrite( rightPWM , power );
}

/**
* \brief Enables or disables the right motor
*
* Enables or disables the right motor
* Sends a signal to the disable pin on the H-bridge
*
* @param enable A bool that describes if the H-brige should be enabled or not
*
* @return No return
*
* \author Rickard Dahm
*
*/
void enableRightMotor( bool enable )
{
	digitalWrite( rightEnable , (int) !enable );
	//setRightPower(0);
}

/**
* \brief Change the outputs to the left motor
*
* Used to control the left motor
*
* @param signal A pointer to a DriveSignals object
*
* @return No return
*
* \author Rickard Dahm
*
*/
void leftMotorControl( DriveSignals *signal )
{
	setLeftDir( (int) signal->forward );
	if( signal->enable != true )
	{
		setLeftPower( 0 );
	}
	else
	{
		setLeftPower( (int) signal->power );
	}
	enableLeftMotor( signal->enable );
}

/**
* \brief Sets the direction of the left motor.
*
* Sets the direction of the left motor.
* Changes the value on the pin defined as leftDir in pins.h
*
* @param dir Direction: dir = 0 = backwards, dir = 1 = forward
*
* @return No return
*
* \author Rickard Dahm
*
*/
void setLeftDir( int dir )
{
	digitalWrite( leftDir, dir );
}

/**
* \brief Sets the dutycycle of the left motors PWM.
*
* Sets the dutycycle of the left motors PWM.
*
* @param power A int between 0 and 255. 0 means 0% dutycycle while 255 means 100%
*
* @return No return
*
* \author Rickard Dahm
*
*/
void setLeftPower( int power )
{

	if( power > 255 )
	{
		power = 255;
		Serial << "Error: Power for left motor was larger than 255, power = 255 will be used!" << endl;
	}
	else if( power < 0 )
	{
		power = 0;
		Serial << "Error: Power for left motor was less than 0, power = 0 will be used!" << endl;
	}

	leftPower = power;
	analogWrite( leftPWM , power );
}

/**
* \brief Write a short description of the function here.
*
* This is a more detailed description of the function.
*
* @param variableName1 Description of parameter
*
* @param variableName3 Description of parameter
*
* @return Description
*
* \author Rickard Dahm
*
*/
void enableLeftMotor( bool enable )
{
	digitalWrite( leftEnable , (int) !enable );
	//setLeftPower(0);
}

/**
* \brief Collects info on the current drivesignals and returns them
*
* Collects info on the current drivesignals and returns them
*
* @return Returns an Engines object
*
* \author Rickard Dahm
*
*/
Engines getMotorSignals()
{
	digitalWrite( LED, !digitalRead( LED ) );
	DriveSignals right = { (bool) digitalRead( rightDir ), (bool) !digitalRead( rightEnable ), rightPower };
	DriveSignals left = { (bool) digitalRead( leftDir ), (bool) !digitalRead( leftEnable ), leftPower };
	Engines engines={ right, left };
	return engines;
}

void liftFansControl(bool liftOn)
{
	digitalWrite( liftFans, (int) liftOn);
}

int liftFansStatus()
{
	return digitalRead(liftFans);
}
