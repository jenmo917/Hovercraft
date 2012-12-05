/*
 * EventManagers.cpp
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */
#include "EventManagers.h"
#include <UsbHost.h>
#include <AndroidAccessory.h>
#include "sensors.h"
#include "Event.h"

/**
* \brief Creates an event every 5 seconds
*
* Creates an event every 5 seconds
*
* @param q The eventqueue
*
* @return No return
*
* \author Rickard Dahm
*/
void timeManager5000( EventQueue* q )
{
	static unsigned long prevMillis = 0;
	unsigned long currMillis;

	currMillis = millis();
	if( currMillis - prevMillis >= 5000 )
	{
		prevMillis = currMillis;
		q->enqueueEvent( Events::EV_TIME_5000, 0 );    // param is not used here
	}
}

/**
* \brief Creates an event every second
*
* Creates an event every 1 seconds
* Code from http://arduino.cc/forum/index.php/topic,37650.0.html
*
* @param q The eventqueue
*
* @return No return
*
* \author Rickard Dahm
*/
void timeManager1000( EventQueue* q )
{
	static unsigned long prevMillis = 0;
	unsigned long currMillis;

	currMillis = millis();
	if( currMillis - prevMillis >= 1000 )
	{
		prevMillis = currMillis;
		q->enqueueEvent( Events::EV_TIME_1000, 0 );    // param is not used here
	}
}

/**
* \brief Creates an event twice every second
*
* Creates an event every 5 seconds
*
* @param q The eventqueue
*
* @return No return
*
* \author Rickard Dahm
*/
void timeManager500( EventQueue* q )
{
	static unsigned long prevMillis = 0;
	unsigned long currMillis;

	currMillis = millis();
	if ( currMillis - prevMillis >= 500 )
	{
		prevMillis = currMillis;
		q->enqueueEvent( Events::EV_TIME_500, 0 );    // param is not used here
	}
}

/**
* \brief Creates an event every 100 milliseconds
*
* Creates an event every 100 milliseconds
*
* @param q The eventqueue
*
* @return No return
*
* \author Rickard Dahm
*/
void timeManager100( EventQueue* q )
{
	static unsigned long prevMillis = 0;
	unsigned long currMillis;

	currMillis = millis();
	if ( currMillis - prevMillis >= 100 )
	{
		prevMillis = currMillis;
		q->enqueueEvent( Events::EV_TIME_100, 0 );    // param is not used here
	}
}

/**
* \brief Calls all timeManagers
*
* Calls all timeManagers
*
* @param q The eventqueue
*
* @return No return
*
* \author Rickard Dahm
*/
void timeManager( EventQueue* q )
{
	timeManager5000( q );
    timeManager1000( q );
    timeManager500( q );
    timeManager100( q );
}

/**
* \brief Generates an event when analoginput changes. Not used in current code
*
* Generated an EV_ANALOG event when analog channel AN_CHAN changes
* Code from http://arduino.cc/forum/index.php/topic,37650.0.html
*
* @param q The eventqueue
*
* @return No return
*
* \author Rickard Dahm
*/
void analogManager( EventQueue* q )
{
    static int prevValue = 0;
    int currValue;

    currValue = analogRead( AN_CHAN );

    if( abs( currValue - prevValue ) >= AN_DELTA )
    {
        prevValue = currValue;
        q->enqueueEvent( Events::EV_ANALOG0, currValue );    // use param to pass analog value to event handler
    }
}

/**
* \brief Reads data from USB-input
*
* Reads data from USB and generates an event when there are data in the input buffer
*
* @param q The eventqueue
*
* @return No return
*
* \author Rickard Dahm
*/
void USBReadManager( EventQueue* q )
{
	int len;

	if ( acc.isConnected() )
	{
		len = acc.read( rcvmsg,sizeof(rcvmsg), 1 );
		if (len > 0)
		{
			q->enqueueEvent( Events::EV_SERIAL, len );
		}
	}
}

void USSensorManager( EventQueue* q )
{
	int duration;
	int distance;
	int warningFlag = 0;

	for( int i = 0; i < MAX_US_SENSORS; i++ )
	{
		if( USSensorList[i].type != "Empty" )
		{
			digitalWrite( USSensorList[i].triggerpin, HIGH );
			delayMicroseconds( 1000 );
			digitalWrite( USSensorList[i].triggerpin, LOW );
			duration = pulseIn( USSensorList[i].echopin, HIGH );
			distance = ( duration / 2 ) / 29.1;
		}
		if ( distance <= 200 && distance >= 0 )
		{
			warningFlag = 1;
		}
	}
	if( warningFlag == 1 )
	{
		q->enqueueEvent( Events::EV_US_SENSOR_WARNING, 0 );
	}
}
