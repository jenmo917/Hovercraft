/*
 * usb.h
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */

#ifndef USB_H_
#define USB_H_

#include <Max3421e.h>
#include <UsbHost.h>
#include <AndroidAccessory.h>
#include "pins.h"

#define ARRAY_SIZE 25

// COMMANDS
//#define COMMAND_TEXT 0xF
// Test signals
#define BLINKY_ON 0x1
#define BLINKY_OFF 0x2
// Drive control signals
#define RIGHT_DIR_FORWARD 0x3// Direction for right engine: Forward
#define RIGHT_DIR_BACK 0x4// Direction for right engine: Backward
#define RIGHT_POWER	0x5// A number between 0-255. Describes the dutycycle of the PWM. 0 = 0%, 255 = 100%
#define LEFT_DIR_FORWARD 0x6// Direction for left engine: Forward
#define LEFT_DIR_BACK 0x7// Direction for left engine: Backward
#define LEFT_POWER	0x8// A number between 0-255. Describes the dutycycle of the PWM. 0 = 0%, 255 = 100%
#define MOTOR_STOP 0x9 //Sets both PWMs dutycycle to 0%
#define ENABLE_MOTORS 0x10
// Requests
#define ALL_SENSOR_REQ 0x11
#define COMPASS_REQ 0x12


#define TARGET_DEFAULT 0x1

extern AndroidAccessory acc;
extern byte rcvmsg[3];
extern byte rcvPBmsg[252];
extern bool blinkyFlag;

void startBlinky();
void stopBlinky();
void USBsetup();
void decodeMsgType();

#endif /* USB_H_ */
