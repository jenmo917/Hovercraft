/*
 * EventManagers.h
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */

#ifndef EVENTMANAGERS_H_
#define EVENTMANAGERS_H_

#include <Events.h>
#include <EventQueue.h>
#include <EventDispatcher.h>
#include "usb.h"
// use this analog channel
#define AN_CHAN 0

// generate an event when the analog
// channel value changes this much
// increase value for noisy sources
#define AN_DELTA 5

void timeManager5000( EventQueue* q );
void timeManager1000( EventQueue* q );
void timeManager500( EventQueue* q );
void timeManager100( EventQueue* q );
void timeManager( EventQueue* q );
void analogManager( EventQueue* q );
void USBReadManager( EventQueue* q );
void USSensorManager( EventQueue* q );

#endif /* EVENTMANAGERS_H_ */
