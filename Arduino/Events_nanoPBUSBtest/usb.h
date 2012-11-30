/*
 * usb.h
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */


/*
 * Commands:
 * ADK = 1-70
 * Brain = 71-141
 * Remote = 142-211
 * Shared: 211-255
 */
#ifndef USB_H_
#define USB_H_

//#include <Max3421e.h>
#include <UsbHost.h>
#include <AndroidAccessory.h>
#include "pins.h"

#define ARRAY_SIZE 25

// ADK COMMANDS
// Test signals
#define BLINKY_ON 1
#define BLINKY_OFF 2
// Drive control signals
#define MOTOR_CONTROL 3
#define PRINT_MESSAGE 4
// Requests
#define I2C_SENSOR_REQ 5
#define US_SENSOR_REQ 6
#define ALL_SENSOR_REQ 10

// Shared Commands
#define I2C_SENSOR_COMMAND 211
#define US_SENSOR_COMMAND 212
// Targets
#define TARGET_ADK 1
#define TARGET_PHONE 2
#define TARGET_CONTROLLER 3

extern AndroidAccessory acc;
extern byte rcvmsg[255];
extern byte rcvmsgInfo[3];
extern byte rcvPBmsg[252];
extern byte sendMsg[252];
extern int rcvPBmsgLength;
extern int sendMsgLength;
extern bool blinkyFlag;


void startBlinky();
void stopBlinky();
void USBsetup();
void decodeMsgType();
void sendMessage(int command, int target);

#endif /* USB_H_ */
