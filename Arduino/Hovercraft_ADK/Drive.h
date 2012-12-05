/*
 * Drive.h
 *
 *  Created on: 9 nov 2012
 *      Author: ricda841
 */

#ifndef DRIVE_H_
#define DRIVE_H_

#include "command.pb.h"

extern int rightPower;
extern int leftPower;

void driveSetup();
void motorControl( Engines *motors );
void rightMotorControl( DriveSignals *signal );
void setRightDir( int dir );
void setRightPower( int power );
void enableRightMotor( bool enable );
void leftMotorControl( DriveSignals *signal );
void setLeftDir( int dir );
void setLeftPower( int power );
void enableLeftMotor( bool enable );
Engines getMotorSignals();

#endif /* DRIVE_H_ */
