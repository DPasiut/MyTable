#define MOTOR_PIN_1 22
#define MOTOR_PIN_2 24
#define MOTOR_PIN_3 26
#define MOTOR_PIN_4 28

const int MAX_POSITION = 1000;
const int MIN_POSITION = 20;
int MAIN_MOTOR_MOVE = 0;
int SECOND_MOTOR_MOVE = 0;
int LIGHT_ON = 0;

int mainEngineCurrentPosition;
int secondEngineCurrentPosition;
int targetPosition;
String stringTargetPosition;
char destination;
String messageFromDevice;
char command;
String positionToSend = "";
String lightColor = "";


void setup()
{
  Serial1.begin(9600);
  Serial1.setTimeout(50);
  Serial.begin(9600);     //Set serial baud rate to 9600 bps
  pinMode(MOTOR_PIN_1, OUTPUT);
  pinMode(MOTOR_PIN_2, OUTPUT);
  pinMode(MOTOR_PIN_3, OUTPUT);
  pinMode(MOTOR_PIN_4, OUTPUT);
  stopMotor();
}


void loop()
{
  mainEngineCurrentPosition=analogRead(0);
  secondEngineCurrentPosition=analogRead(2);

  sendPositionInfo();

  if(Serial1.available() > 0) {
    messageFromDevice = Serial1.readString();
    command = getCommand(messageFromDevice);
    Serial.println(command);
  }

  if(command == 'c' && LIGHT_ON == 1){
//    changeLightColor();
  }
  if(command == 'o'){
//    turnLightOn();
    LIGHT_ON = 1;
  }
  if(command == 'f'){
//    turnLightOff();
    LIGHT_ON = 0;
  }
  if(command == 'q'){
    if(mainEngineCurrentPosition < MAX_POSITION){
        turnMotorLeft();
        //turnSecondMotorLeft();
    }else {
      stopMotor();
      stopSecondMotor();
//      command='w';
    }
  }

  if(command == 'e'){
    if(mainEngineCurrentPosition > MIN_POSITION){
      turnMotorRight();
      //turnSecondMotorRight();
    }else {
      stopMotor();
      stopSecondMotor();
//      command='w';
    }
  }

  if(command == 'w'){
    stopMotor();
    stopSecondMotor();
  }

  if(command == 'u'){
    if(mainEngineCurrentPosition < targetPosition
        && mainEngineCurrentPosition < MAX_POSITION){
      turnMotorLeft();
    }else {
      stopMotor();
      command='w';
    }
  }

 if(command == 'd'){
     if(mainEngineCurrentPosition > targetPosition && mainEngineCurrentPosition > MIN_POSITION){
      turnMotorRight();
    }else {
      stopMotor();
      command='w';
    }
 }
  motorListener();
  delay(5);
}

int getSecondMotorDirection(){
  if ((mainEngineCurrentPosition > secondEngineCurrentPosition && MAIN_MOTOR_MOVE)
      || (mainEngineCurrentPosition > secondEngineCurrentPosition && SECOND_MOTOR_MOVE)){
    return 1;
  }
  else if ((mainEngineCurrentPosition < secondEngineCurrentPosition && MAIN_MOTOR_MOVE)
            || (mainEngineCurrentPosition < secondEngineCurrentPosition && SECOND_MOTOR_MOVE) ){
    return 2;
  }
  else return 0;
}
void motorListener(){
  switch(getSecondMotorDirection()){
    case 1 :
      turnSecondMotorLeft();
      break;
    case 2 :
      turnSecondMotorRight();
      break;
    default :
      stopSecondMotor();
  }
}
void turnMotorLeft(){
  digitalWrite(MOTOR_PIN_1, HIGH);
  digitalWrite(MOTOR_PIN_2, LOW);
  MAIN_MOTOR_MOVE = 1;
}

void turnMotorRight(){
  digitalWrite(MOTOR_PIN_1, LOW);
  digitalWrite(MOTOR_PIN_2, HIGH);
  MAIN_MOTOR_MOVE = 1;
}

void turnSecondMotorLeft(){
  digitalWrite(MOTOR_PIN_3, HIGH);
  digitalWrite(MOTOR_PIN_4, LOW);
  SECOND_MOTOR_MOVE = 1;
}

void turnSecondMotorRight(){
  digitalWrite(MOTOR_PIN_3, LOW);
  digitalWrite(MOTOR_PIN_4, HIGH);
  SECOND_MOTOR_MOVE = 1;
}
void stopMotor(){
  digitalWrite(MOTOR_PIN_1, LOW);
  digitalWrite(MOTOR_PIN_2, LOW);
  MAIN_MOTOR_MOVE = 0;
}

void stopSecondMotor(){
  digitalWrite(MOTOR_PIN_3, LOW);
  digitalWrite(MOTOR_PIN_4, LOW);
  SECOND_MOTOR_MOVE = 0;
}

void sendPositionInfo(){
   positionToSend = String(mainEngineCurrentPosition);
   int position_len = positionToSend.length() + 1;
   char char_array[position_len];
   positionToSend.toCharArray(char_array,position_len);
   Serial1.write(char_array);
   Serial1.write(';');
   positionToSend = "";
}

char getCommand(String messageFromDevice) {

    if (messageFromDevice.length() < 2) {
        return messageFromDevice.charAt(messageFromDevice.length() - 1);
    }
    if (messageFromDevice.length() > 1) {
        if (messageFromDevice.charAt(messageFromDevice.length() - 1) == 'c') {
            return 'c';
            lightColor = messageFromDevice.substring(1, messageFromDevice.length() - 1);
        } else {
            targetPosition = messageFromDevice.toInt();
            if (targetPosition > mainEngineCurrentPosition) {
                return 'u';
            } else {
                return 'd';
            }
        }
    }
}
