/*
 * I2C.cpp
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */
#include "I2C.h"
#include "sensors.h"

// shift the address 1 bit right, the Wire library only needs the 7
// most significant bits for the address
int reading = 0;

/**
* \brief Joins the I2C bus
*
* Joins the I2C bus
*
* @return No return
*
* \author Rickard Dahm
*/
void I2CSetup()
{
	Wire.begin();       // join i2c bus (address optional for master)
}


