//*****Created by Jordan Schmuckler for Project PINS.
const int analogInPin0 = A0;// Analog input pins
const int analogInPin1 = A1;
const int analogInPin2 = A2;
const int analogInPin3 = A3;
 
//Arrays for the 4 inputs**********************************************
float sensorValue[4] = {0,0,0,0};
float voltageValue[4] = {0,0,0,0};

//Char used for reading in Serial characters
char inbyte = 0;

void setup() {
  // put your setup code here, to run once:
 Serial1.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
    readSensors();
    getVoltageValue();
    sendAndroidValues();

}

void sendAndroidValues()
 {
  //puts # before the values so our app knows what to do with the data
  //Serial1.print('#');
  //for loop cycles through 4 sensors and sends values via serial
  for(int k=0; k<4; k++)
  {
    Serial1.print(voltageValue[k]);
    Serial1.print(' ');
  }
  Serial1.print('#');
 //Serial1.print('~'); //used as an end of transmission character - used in app for string length
 Serial1.println();
 delay(175);        //added a delay to eliminate missed transmissions
}


void readSensors()
{
  // read the analog in value to the sensor array
  sensorValue[0] = analogRead(analogInPin0);
  sensorValue[1] = analogRead(analogInPin1);
  sensorValue[2] = analogRead(analogInPin2);
  sensorValue[3] = analogRead(analogInPin3);
}


void getVoltageValue()
{
  for (int x = 0; x < 4; x++)
  {
    voltageValue[x] = ((sensorValue[x]/1023)*5);
  }
}
