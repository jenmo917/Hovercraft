#include <Arduino.h>
	/*
	 * prescaler = 1 ---> PWM frequency is 31000 Hz
	   prescaler = 2 ---> PWM frequency is 4000 Hz
	   prescaler = 3 ---> PWM frequency is 490 Hz (default value)
	   prescaler = 4 ---> PWM frequency is 120 Hz
	   prescaler = 5 ---> PWM frequency is 30 Hz
	   prescaler = 6 ---> PWM frequency is <20 Hz
	 *
	 */
void setup()
{
	pinMode( 2 , OUTPUT );
	int eraser = 7;
	TCCR3B &= ~eraser;
	int prescaler = 1;
	TCCR3B |= prescaler;
}

void loop()
{
	//analogWrite(2,200);
}


int main(void) {

  /* Must call init for arduino to work properly */
	init();
	setup();
	analogWrite( 2 , 50 );
	while(true)
	{
	  loop();
	}
}
