/*
 * EventHandlers.h
 *
 *  Created on: 7 nov 2012
 *      Author: ricda841
 */

#ifndef EVENTHANDLERS_H_
#define EVENTHANDLERS_H_

#include <Events.h>
#include <EventQueue.h>
#include <EventDispatcher.h>
#include <Arduino.h>
#include "I2C.h"
#include "usb.h"
#include "nanoPB.h"

// Time event handlers
void timeHandler1000(int event, int param);
void timeHandler500(int event, int param);
void timeHandler100(int event, int param);
void blinkyHandler(int event, int param);
// analog event handler
void analogHandler(int event, int param);
// Handler that gets I2C sensor data
void I2CSensorDataHandler(int event, int param);
void IRSensorHandler(int event, int param);
void USBReadHandler(int event, int param);
void usbSoftwareTestHandler(int event, int param);
void USBSendSensorDataHandler(int event, int param);

#endif /* EVENTHANDLERS_H_ */
