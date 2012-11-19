#include <Events.h>
#include <EventQueue.h>
#include <EventDispatcher.h>

// the event queue
EventQueue q;

// the event dispatcher
EventDispatcher disp(&q);

// use this analog channel
#define AN_CHAN 0

// generate an event when the analog
// channel value changes this much
// increase value for noisy sources
#define AN_DELTA 5

// time event handler
void timeHandler1000(int event, int param) {
    Serial.print("Time elapsed in seconds: ");
    Serial.println(millis() / 1000);
}

void timeHandler500(int event, int param) {
    Serial.print("Time elapsed in half seconds: ");
    Serial.println(millis() / 500);
}

void timeHandler100(int event, int param) {
    Serial.print("Time elapsed in 1/10 seconds: ");
    Serial.println(millis() / 100);
}

// analog event handler
void analogHandler(int event, int param) {
    Serial.print("Analog value: ");
    Serial.println(param);
}

// this function generates an EV_TIME event
// each 1000 ms
void timeManager() {
    static unsigned long prevMillis1000 = 0;
    static unsigned long prevMillis500 = 0;
    static unsigned long prevMillis100 = 0;
    unsigned long currMillis;

    currMillis = millis();
    if (currMillis - prevMillis1000 >= 1000) {
        prevMillis1000 = currMillis;
        q.enqueueEvent(Events::EV_TIME_1000, 0);    // param is not used here
    }
	if (currMillis - prevMillis500 >= 500) {
		prevMillis500 = currMillis;
		q.enqueueEvent(Events::EV_TIME_500, 0);    // param is not used here
	}
	if (currMillis - prevMillis100 >= 100) {
		prevMillis100 = currMillis;
		q.enqueueEvent(Events::EV_TIME_100, 0);    // param is not used here
	}
}

// this function generates an EV_ANALOG event
// whenever the analog channel AN_CHAN changes
void analogManager() {
    static int prevValue = 0;
    int currValue;

    currValue = analogRead(AN_CHAN);

    if (abs(currValue - prevValue) >= AN_DELTA) {
        prevValue = currValue;
        q.enqueueEvent(Events::EV_ANALOG0, currValue);    // use param to pass analog value to event handler
    }
}

// program setup
void setup() {
    Serial.begin(115200);

    disp.addEventListener(Events::EV_TIME_1000, timeHandler1000);
    disp.addEventListener(Events::EV_TIME_500, timeHandler500);
    disp.addEventListener(Events::EV_TIME_100, timeHandler100);
    disp.addEventListener(Events::EV_ANALOG0, analogHandler);
}

// loop
void loop() {
    // call the event generating functions
    timeManager();
    //analogManager();

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
