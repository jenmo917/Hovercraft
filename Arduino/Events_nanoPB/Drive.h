/*
 * Drive.h
 *
 *  Created on: 9 nov 2012
 *      Author: ricda841
 */

#ifndef DRIVE_H_
#define DRIVE_H_

extern int rightPower;
extern int leftPower;

void driveSetup();
void setRightDir(int dir);
void setRightPower(int* power);
void setLeftDir(int dir);
void setLeftPower(byte* power);
void enableMotors();

#endif /* DRIVE_H_ */
