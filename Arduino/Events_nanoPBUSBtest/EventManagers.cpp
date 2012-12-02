/*
 * EventManagers.cpp
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */
#include "EventManagers.h"
#include <UsbHost.h>
#include <AndroidAccessory.h>

void timeManager5000(EventQueue* q)
{
	static unsigned long prevMillis = 0;
	unsigned long currMillis;

	currMillis = millis();
	if (currMillis - prevMillis >= 5000) {
		prevMillis = currMillis;
		q->enqueueEvent(Events::EV_TIME_5000, 0);    // param is not used here
	}
}

void timeManager1000(EventQueue* q)
{
	static unsigned long prevMillis = 0;
	unsigned long currMillis;

	currMillis = millis();
	if (currMillis - prevMillis >= 1000) {
		prevMillis = currMillis;
		q->enqueueEvent(Events::EV_TIME_1000, 0);    // param is not used here
	}
}

void timeManager500(EventQueue* q)
{
	static unsigned long prevMillis = 0;
	unsigned long currMillis;

	currMillis = millis();
	if (currMillis - prevMillis >= 500) {
		prevMillis = currMillis;
		q->enqueueEvent(Events::EV_TIME_500, 0);    // param is not used here
	}
}

void timeManager100(EventQueue* q)
{
	static unsigned long prevMillis = 0;
	unsigned long currMillis;

	currMillis = millis();
	if (currMillis - prevMillis >= 100) {
		prevMillis = currMillis;
		q->enqueueEvent(Events::EV_TIME_100, 0);    // param is not used here
	}
}

// this function generates an EV_TIME event
// each 1000 ms
void timeManager(EventQueue* q)
{
	timeManager5000(q);
    timeManager1000(q);
    timeManager500(q);
    timeManager100(q);
}

// this function generates an EV_ANALOG event
// whenever the analog channel AN_CHAN changes
void analogManager(EventQueue* q)
{
    static int prevValue = 0;
    int currValue;

    currValue = analogRead(AN_CHAN);

    if (abs(currValue - prevValue) >= AN_DELTA)
    {
        prevValue = currValue;
        q->enqueueEvent(Events::EV_ANALOG0, currValue);    // use param to pass analog value to event handler
    }
}

void USBReadManager(EventQueue* q)
{
	int len;

	if (acc.isConnected())
	{
		len = acc.read(rcvmsg,sizeof(rcvmsg),1);
		if (len > 0)
		{
			q->enqueueEvent(Events::EV_SERIAL, len);
		}
	}
}




