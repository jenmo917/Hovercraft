#include "EventHandlers.h"
#include "EventManagers.h"
#include "I2C.h"

// the event queue
EventQueue q;

// the event dispatcher
EventDispatcher disp(&q);

// program setup
void setup() {
    Serial.begin(115200);
    I2CSetup();
    disp.addEventListener(Events::EV_TIME_1000, timeHandler1000);
    disp.addEventListener(Events::EV_TIME_1000, I2CSensorDataHandler);
    //disp.addEventListener(Events::EV_TIME_500, timeHandler500);
    //disp.addEventListener(Events::EV_TIME_100, timeHandler100);
    disp.addEventListener(Events::EV_ANALOG0, analogHandler);
}

// loop
void loop() {
    // call the event generating functions
    timeManager(&q);
    //analogManager(&q);

    // get events from the queue and call the
    // registered function(s)
    disp.run();
}

int main(void)
{
  /* Must call init for arduino to work properly */
	init();
	setup();

	for (;;)
	{
	  loop();
	}
}
