int pinBouton;
int pinLed;
int verif =0;

void setup() {
  pinBouton = 10;
  pinLed = 13;
  
  pinMode(pinBouton,INPUT);
  pinMode(pinLed,OUTPUT);
  Serial.begin(9600);
}

void loop() {
  boolean etatBouton = digitalRead(pinBouton);
  digitalWrite(pinLed,LOW);
  if(etatBouton==1){
    envoieMessage();
    delay(10000);
  }
}

/**
 * envoie d'une donnée a l'appareil connecté en bluetooth
 */
void envoieMessage(){
      Serial.println("a");
      digitalWrite(pinLed,HIGH);
}
