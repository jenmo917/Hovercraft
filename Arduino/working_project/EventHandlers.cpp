/*
 * EventHandlers.cpp
 *
 *  Created on: 7 nov 2012
 *      Author: ricda841
 */
#include "EventHandlers.h"
#include "EventManagers.h"
#include <AndroidAccessory.h>
#include <UsbHost.h>
#include <AndroidAccessory.h>
#include "sensors.h"

// time event handler
void timeHandler1000(int event, int param)
{
	Serial.print("Time elapsed in seconds: ");
	Serial.println(millis() / 1000);
}

void timeHandler500(int event, int param)
{
	Serial.print("Time elapsed in half seconds: ");
	Serial.println(millis() / 500);
}

void timeHandler100(int event, int param)
{
	Serial.print("Time elapsed in 1/10 seconds: ");
	Serial.println(millis() / 100);
}

void blinkyHandler(int event, int param)
{
	if (blinkyFlag == true)
	{
		digitalWrite(LED, !digitalRead(LED));
	}
}

// analog event handler
void analogHandler(int event, int param)
{
	Serial.print("Analog value: ");
	Serial.println(param);
}

void I2CSensorDataHandler(int event, int param)
{
	// step 1: instruct sensor to read echoes
	Wire.beginTransmission(I2CsensorList[0].address);  // transmit to device
	// the address specified in the datasheet is 66 (0x42)
	// but i2c adressing uses the high 7 bits so it's 33
	Wire.write('A');        // command sensor to measure angle
	Wire.endTransmission(); // stop transmitting

	// step 2: wait for readings to happen
	delay(10); // datasheet suggests at least 6000 microseconds

	// step 3: request reading from sensor
	Wire.requestFrom(I2CsensorList[0].address, 2); // request 2 bytes from slave device #33

	// step 4: receive reading from sensor
	if (2 <= Wire.available()) // if two bytes were received
	{
		reading = Wire.read(); // receive high byte (overwrites previous reading)
		reading = reading << 8; // shift high byte to be high 8 bits
		reading += Wire.read(); // receive low byte as lower 8 bits
		reading /= 10;
		Serial.print("Sensor Data: ");
		Serial.println(reading); // print the reading
	}
	else
	{
		Serial.print("I2C reading failed");
	}
}

void IRSensorHandler(int event, int param)
{
	int duration, distance;
	digitalWrite(trigPin1, HIGH);
	delayMicroseconds(1000);
	digitalWrite(trigPin1, LOW);
	duration = pulseIn(echoPin1, HIGH);
	distance = (duration/2) / 29.1;
	if (distance >= 200 || distance <= 0){
		Serial.println("Out of range");
		digitalWrite(LED, LOW);
	}
	else {
		Serial.print(distance);
		Serial.println(" cm");
		digitalWrite(LED, HIGH);
	}
}

void USBReadHandler(int event, int param)
{
	int i = 0;
	for(i = 0; i < 3; i++)
	{
		rcvmsgInfo[i] = rcvmsg[i];
	}
	for(i = 3; i < param; i++)
	{
		rcvPBmsg[i-3] = rcvmsg[i];
	}
	decodeMsgType();
	/*
	if (acc.isConnected())
	{
		int i = 0;
		byte *infoPtr = &rcvmsg[0];
		byte *msgPtr = &rcvPBmsg[0];
		bool bufferFlag = false;
		while (acc.peek() != -1 && i < 3)
		{
			*infoPtr++ = acc.read();
			bufferFlag = true;
			i++;
		}

		while (acc.peek() != -1)
		{
			*msgPtr++ = acc.read();
			bufferFlag = true;
		}

		if (bufferFlag)
		{
			decodeMsgType();
		}
	}
	*/
}

void USBSendSensorDataHandler(int event, int param)
{
	encodeSensorMsg(I2CsensorList[0]);
	sendMessage(SENSOR_DATA, TARGET_PHONE);
}
/*
void usbSoftwareTestHandler(int event, int param)
{
	static unsigned long prev = 0;
	byte *msgPtr = &rcvmsg[0];

	switch(prev)
	{
	case 0:
		*msgPtr++ = ENABLE_MOTORS;
		*msgPtr++ = TARGET_DEFAULT;
		//*msgPtr++ = 1; 	//Arraysize
		//*msgPtr++ = 200;
		prev++;
		decodeMsgType();
		break;
	case 1:
		*msgPtr++ = RIGHT_POWER;
		*msgPtr++ = TARGET_DEFAULT;
		*msgPtr++ = 1; 	//Arraysize
		*msgPtr++ = 100;
		prev++;
		decodeMsgType();
		break;
	case 2:
		*msgPtr++ = RIGHT_DIR_BACK;
		*msgPtr++ = TARGET_DEFAULT;
		//*msgPtr++ = 1; 	//Arraysize
		//*msgPtr++ = 200;
		prev++;
		decodeMsgType();
		break;
	case 3:
		*msgPtr++ = LEFT_POWER;
		*msgPtr++ = TARGET_DEFAULT;
		*msgPtr++ = 1; 	//Arraysize
		*msgPtr++ = 200;
		prev++;
		decodeMsgType();
		break;
	case 4:
		*msgPtr++ = LEFT_DIR_BACK;
		*msgPtr++ = TARGET_DEFAULT;
		//*msgPtr++ = 1; 	//Arraysize
		//*msgPtr++ = 200;
		prev++;
		decodeMsgType();
		break;
	case 5:
		*msgPtr++ = RIGHT_DIR_FORWARD;
		*msgPtr++ = TARGET_DEFAULT;
		//*msgPtr++ = 1; 	//Arraysize
		//*msgPtr++ = 200;
		prev++;
		decodeMsgType();
		break;
	default:
		break;
	}
}*/
