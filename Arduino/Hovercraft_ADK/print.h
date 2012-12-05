/*
 * print.h
 *
 *  Created on: 26 nov 2012
 *      Author: ricda841
 */

#ifndef PRINT_H_
#define PRINT_H_

#include <Arduino.h>
#include "command.pb.h"

void printRightMotorSignal( DriveSignals* engines );
void printLeftMotorSignal( DriveSignals* engines );
void printMotorSignal( Engines* engines );

#endif /* PRINT_H_ */
