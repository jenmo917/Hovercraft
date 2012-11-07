
#include <Arduino.h>

int ledPin =  13;    // LED connected to digital pin 13

// The setup() method runs once, when the sketch starts
void setup()   {
  // initialize the digital pin as an output:
	Serial.begin(9600);
}

// the loop() method runs over and over again,
// as long as the Arduino has power

void loop()
{
	Serial.println("Hello Computer 1");
}


int main(void) {

  init();
  setup();

  while(true) {
    loop();
  }
}
