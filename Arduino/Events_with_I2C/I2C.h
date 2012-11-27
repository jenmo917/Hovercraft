/*
 * I2C.h
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */

#ifndef I2C_H
#define I2C_H

#include <Arduino.h>
#include <Wire.h>
#include "sensors.h"

#define I2C_MAX_SENSORS 5

extern struct sensor sensorList[I2C_MAX_SENSORS];
extern int reading;

void I2CSetup();

#endif /* I2C_H */
