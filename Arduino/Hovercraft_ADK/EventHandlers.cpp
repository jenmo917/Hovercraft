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
#include "Streaming.h"
#include "Event.h"
#include "Drive.h"

/**
* \brief Prints the number of seconds since reset
*
* Prints the number of seconds since reset
*
*
* @param event Not used
*
* @param param Not used
*
* @return No return
*
* \author Rickard Dahm
*/
void timeHandler1000( int event, int param )
{
	Serial.print( "Time elapsed in seconds: " );
	Serial.println( millis() / 1000 );
}

/**
* \brief Prints the number of half seconds since reset
*
* Prints the number of half seconds since reset
*
*
* @param event Not used
*
* @param param Not used
*
* @return No return
*
* \author Rickard Dahm
*/
void timeHandler500( int event, int param )
{
	Serial.print( "Time elapsed in half seconds: " );
	Serial.println( millis() / 500 );
}

/**
* \brief Prints the number of times 100 milliseconds have passed since reset
*
* Prints the number of times 100 milliseconds have passed since reset
*
*
* @param event Not used
*
* @param param Not used
*
* @return No return
*
* \author Rickard Dahm
*/
void timeHandler100( int event, int param )
{
	Serial.print( "Time elapsed in 1/10 seconds: " );
	Serial.println( millis() / 100 );
}

/**
* \brief Change the output on the LED pin
*
* Inverts the output on LEDpin. Used together with a timeManager to obtain blinky
*
*
* @param event Not used
*
* @param param Not used
*
* @return No return
*
* \author Rickard Dahm
*/
void blinkyHandler( int event, int param )
{
	if ( blinkyFlag == true )
	{
		digitalWrite( LED, !digitalRead( LED ) );
	}
}

/**
* \brief Prints the value of param
*
* Prints the value of param
*
*
* @param event Not used
*
* @param param Value to be printed. Printed as an integer
*
* @return No return
*
* \author Rickard Dahm
*/
void analogHandler( int event, int param )
{
	Serial.print( "Analog value: " );
	Serial.println( param );
}

/**
* \brief Reads I2C data and starts an event to send it to target
*
* Reads I2C data and starts an event to send it to target
*
* @param event Not used
*
* @param target Used when adding a EV_I2C_SENSOR_FINISHED event to queue.
*
* @return No return
*
* \author Rickard Dahm
*/
void I2CSensorDataHandler( int event, int target )
{
	Serial << "********************************************************" << endl;
	Serial << "Reading data from I2C Sensors" << endl;
	for( int i = 0; i < I2C_MAX_SENSORS; i++ )
	{
		//Requires that all sensors want the commands our compass does.
		//Will need to be changed if other I2C devices requires other commands
		if( I2CSensorList[i].type != "Empty" )
		{
			// step 1: instruct sensor to read echoes
			Wire.beginTransmission( I2CSensorList[i].address );  // transmit to device
			// the address specified in the datasheet is 66 (0x42)
			// but i2c adressing uses the high 7 bits so it's 33
			Wire.write( 'A' );        // command sensor to measure angle
			Wire.endTransmission(); // stop transmitting

			// step 2: wait for readings to happen
			delay( 10 ); // datasheet suggests at least 6000 microseconds

			// step 3: request reading from sensor
			Wire.requestFrom( I2CSensorList[0].address, 2 ); // request 2 bytes from slave device #33

			// step 4: receive reading from sensor
			if (2 <= Wire.available()) // if two bytes were received
			{
				reading = Wire.read(); // receive high byte (overwrites previous reading)
				reading = reading << 8; // shift high byte to be high 8 bits
				reading += Wire.read(); // receive low byte as lower 8 bits
				reading /= 10;
				Serial.print( "Sensor Data: " );
				Serial.println( reading ); // print the reading
			}
			else
			{
				Serial.print( "I2C reading failed" );
			}
		}
	}
	q.enqueueEvent( Events::EV_I2C_SENSOR_FINISHED, target );
}

/**
* \brief Reads Ultrasonic sensor data and starts an event to send it to target
*
* Reads Ultrasonic sensor data and starts an event to send it to target
*
* @param event Not used
*
* @param target Used when adding a EV_US_SENSOR_FINISHED event to queue.
*
* @return No return
*
* \author Rickard Dahm
*/
void USSensorHandler( int event, int target )
{
	Serial << "********************************************************" << endl;
	Serial << "Reading data from Ultrasonic Sensors" << endl;
	int duration, distance;
	for( int i = 0; i < MAX_US_SENSORS; i++ )
	{
		if( USSensorList[i].type != "Empty" )
		{
			digitalWrite( USSensorList[i].triggerpin, HIGH );
			delayMicroseconds( 1000 );
			digitalWrite( USSensorList[i].triggerpin, LOW );
			duration = pulseIn( USSensorList[i].echopin, HIGH );
			distance = ( duration / 2 ) / 29.1;
			Serial << "Sensor " << i << ": ";
			if ( distance >= 200 || distance <= 0 )
			{
				Serial.println( "Out of range" );
			}
			else
			{
				Serial.print( distance );
				Serial.println( " cm" );
			}
		}
	}
	q.enqueueEvent( Events::EV_US_SENSOR_FINISHED, target );
}

/**
* \brief Sorts the read USBdata to their correct buffer
*
* Sorts the read USBdata to their correct buffer
*
* @param event Not used
*
* @param param Not used
*
* @return No return
*
* \author Rickard Dahm
*/
void USBReadHandler( int event, int param )
{
	int i = 0;
	rcvPBmsgLength = 0;
	for( i = 0; i < 3; i++ )
	{
		rcvmsgInfo[i] = rcvmsg[i];
	}
	for( i = 3; i < param; i++ )
	{
		rcvPBmsg[i-3] = rcvmsg[i];
		rcvPBmsgLength++;
	}
	decodeMsgType();
}

/**
* \brief Sends the ultrasonic sensors data saved in USSensorList via the USB
*
* Sends the ultrasonic sensors data saved in USSensorList via the USB
*
* @param event Not used
*
* @param target Describes which target to send to.
*
* @return No return
*
* \author Rickard Dahm
*/
void USBSendUSSensorDataHandler( int event, int target )
{
	//Serial << "USBSendUSSensorDataHandler" << endl;
	if ( acc.isConnected() )
	{
		if( encodeUSSensorListMsg() )
		{
			sendMessage( US_SENSOR_COMMAND, target );
		}
		else
		{
			Serial << "Failed to encode sensors in function: USBSendSensorDataHandler";
		}
	}
}

/**
* \brief Sends the I2C sensors data saved in I2CSensorList via the USB
*
* Sends the I2C sensors data saved in I2CSensorList via the USB
*
* @param event Not used
*
* @param target Describes which target to send to.
*
* @return No return
*
* \author Rickard Dahm
*/
void USBSendI2CSensorDataHandler( int event, int target )
{
	Serial << "USBSendI2CSensorDataHandler" << endl;
	if ( acc.isConnected() )
	{
		if( encodeI2CSensorListMsg() )
		{
			sendMessage( I2C_SENSOR_COMMAND, target );
		}
		else
		{
			Serial << "Failed to encode sensor in function: USBSendI2CSensorDataHandler";
		}
	}
}

/**
* \brief Sends the current drivesignals via the USB
*
* Sends the current drivesignals via the USB
*
* @param event Not used
*
* @param target Describes which target to send to.
*
* @return No return
*
* \author Rickard Dahm
*/
void USBSendEnginesObject( int event, int target )
{
	Engines engineSignals = getMotorSignals();
	if( encodeEngines(engineSignals) )
	{
		if ( target == 0 )
		{
			sendMessage( MOTOR_CONTROL, 3 );
		}
		else
		{
			sendMessage( MOTOR_CONTROL, target );
		}
	}
	else
	{
		Serial << "Failed to encode Engines object in function: " << endl << "USBSendEnginesObject";
	}
}

/**
* \brief Increases a counter and checks if the counter is 10. If 10 stops motors.
*
* Increases a counter and checks if the counter is 10. If 10 stops motors.
*
* @param event Not used
*
* @param param Not used
*
* @return No return
*
* \author Rickard Dahm
*/
void connectionCheckEngines( int event, int param )
{
	connectionCounter++;
	if( connectionCounter == 10 )
	{
		Serial << "Connection lost? No enginesignals recieved" << endl;
		Serial << "Hovercraft will now stop!" << endl;

		DriveSignals stop = { false, true, 0 };
		rightMotorControl( &stop );
		leftMotorControl( &stop );
	}
}
