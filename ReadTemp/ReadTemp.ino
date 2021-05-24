/*****************************************************************************
*
* Diwell Electronics Co.,Ltd.
* Project Name : (DTPML-SPI 시리즈) SPI Master Code
* Version : 1.2
* SYSTEM CLOCK : 16Mhz 
* BOARD : Arduino UNO. 5V operation 
* 지원 모델 : DTPML-SPI-151, DTPML-SPI-81


 PORT Description

1. ChipSelectPin : 10           
2. MOSI(Master Output) : 11
3. MISO(Master Input) : 12
4. SCK : 13
  온도센서모듈 입력전원은 3.3V로 하셔야 하며 포트 연결 방법은 회로도를 참고하십시오.
  온도센서 통신포트의 논리 레벨은 3.3V 이기 때문에 반드시 회로도를 참고하시기 바랍니다.

 Revision history.

1. 2016.5.4  : First version is released.
2. 2016.5.9  : 방사율 READ/WRITE 함수 추가(함수 내부 주의 사항 필독!)
3. 2016.5.9  : 레이저 ON 명령 함수 추가
4. 2020.1.2. : Timer1을 이용한 request 주기 설정
****************************************************************************/
#include<SPI.h>

#define OBJECT	0xA0			// 대상 온도 커맨드
#define SENSOR	0xA1			// 센서 온도 커맨드
#define ERATIO 0xA2                     // 방사율 읽기 커맨드 
#define LASER  0xA4                     // 레이저 ON 커맨드 
#define DELAY_10us  10                  // delay 10us
#define DELAY_35us  35                  // delay 35us

const int chipSelectPin  = 10;
unsigned char cEratio;                     // 방사율 저장 변수
unsigned char Timer1_Flag = 0;
int  iOBJECT, iSENSOR;	                  // 부호 2byte 온도 저장 변수 
volatile unsigned char Laser_Flag=0;
char income = '\0'; // 라즈베리파이에서 받아오는 문자열
int count = 0; // 카운터 변수

void setup() {
  /* Setting CS & SPI */
  digitalWrite(chipSelectPin , HIGH);    // SCE High Level
  pinMode(chipSelectPin , OUTPUT);        // SCE OUTPUT Mode
  SPI.setDataMode(SPI_MODE3);            // SPI Mode 3
  SPI.setClockDivider(SPI_CLOCK_DIV16);  // 16MHz/16 = 1MHz
  SPI.setBitOrder(MSBFIRST);             // MSB First
  SPI.begin();                           // Initialize SPI
  
  pinMode(2, INPUT_PULLUP);
  pinMode(4, OUTPUT); // led
  attachInterrupt(digitalPinToInterrupt(2), LASER_ISR, FALLING); 
  Ewrite_COMMAND(98);
  
  delay(500);                             // Sensor initialization time 
  Timer1_Init();                          // Timer1 setup : 50ms(20Hz) interval
  Serial.begin(9600);
  interrupts();                           // enable all interrupts
  
}
void LASER_ISR(void){
  detachInterrupt(digitalPinToInterrupt(2)); 
  Laser_Flag =1;
}
int SPI_COMMAND(unsigned char cCMD, unsigned char delay_us){   
    unsigned char T_high_byte;
    unsigned char T_low_byte;
    digitalWrite(chipSelectPin , LOW);  // SCE Low Level
    delayMicroseconds(10);              // delay(10us)
    SPI.transfer(cCMD);                // transfer  1st Byte
    delayMicroseconds(delay_us);       // delay      
    T_low_byte = SPI.transfer(0x22);   // transfer  2nd Byte
    delayMicroseconds(delay_us);        // delay
    T_high_byte = SPI.transfer(0x22);  // transfer  3rd Byte
    delayMicroseconds(10);              // delay(10us)
    digitalWrite(chipSelectPin , HIGH); // SCE High Level 
    
    return (T_high_byte<<8 | T_low_byte);	// 온도값 return 
}

void Ewrite_COMMAND(unsigned char cRatio){  // 방사율 WRITE 함수 (아래 주의 필독!!!)
  // 주의!!! : 방사율 WRITE 명령을 while문 안에 넣어서 무한 반복으로 실행하면 안됩니다.
  // 방사율 WRITE 명령은 버튼이나 특정 조건에 의해 한번만 실행하도록 하세요.
  // 반복 플레시 작업시 제품 수명에 영향을 끼칩니다.
  // 한번 저장된 방사율은 전원을 리셋 해도 값을 유지합니다..
    unsigned char dummy;
    if( (cRatio <10 ) || (cRatio > 100)){    // range : 10 ~ 100
      cRatio = 97;
    }
    digitalWrite(chipSelectPin , LOW);  // SCE Low Level
    delayMicroseconds(10);              // delay(10us)
    SPI.transfer(0xA3);                // transfer 1st Byte
    delayMicroseconds(DELAY_35us);      // delay(35us)          
    dummy = SPI.transfer(0x22);        // transfer 2nd Byte
    delayMicroseconds(DELAY_35us);      //delay(35us)  
    dummy = SPI.transfer(cRatio);        // transfer 3rd Byte (Eratio)
    delayMicroseconds(10);              //delay(10us)  
    digitalWrite(chipSelectPin , HIGH); // SCE High Level 
    delay(500);                          // delay(500ms);
}

ISR(TIMER1_OVF_vect){        // interrupt service routine (Timer1 overflow)
  TCNT1 = 62411;            // preload timer : 이 값을 바꾸지 마세요.
  Timer1_Flag = 1;          // Timer 1 Set Flag
}
void Timer1_Init(void){
  TCCR1A = 0;
  TCCR1B = 0;
  TCNT1 = 62411;            // preload timer 65536-16MHz/256/20Hz
  TCCR1B |= (1 << CS12);    // 256 prescaler 
  TIMSK1 |= (1 << TOIE1);   // enable timer overflow interrupt
}


void loop() {
  // put your main code here, to run repeatedly:
  
  while(Serial.available()){   //시리얼에 읽을 값이 있으면
    //Serial.println(Serial.available());
    digitalWrite(4, HIGH);    //초록불 켜기
    delay(1000);
    digitalWrite(4, LOW);    //초록불 켜기
    income += (char)Serial.read(); //income안에 해당 내용 저장
    if(income == 's'){
      break;
    }
    //Serial.println(income);//시리얼에 해당 내용 전송
  }
  if(income != 0){ //income에 내용이 있으면
    
    if(income == 's'){
      //Serial.println("1");
      while (count++ < 5){
        if(Timer1_Flag){                // 50ms마다 반복 실행(Timer 1 Flag check)
          Timer1_Flag = 0;                              // Reset Flag
          iOBJECT = SPI_COMMAND(OBJECT,DELAY_10us);     // 대상 온도 Read 
          delayMicroseconds(10);                        // 10us : 이 라인을 지우지 마세요 
          iSENSOR = SPI_COMMAND(SENSOR,DELAY_10us);     // 센서 온도 Read
          delayMicroseconds(10);                        // 10us : 이 라인을 지우지 마세요 
          cEratio = SPI_COMMAND(ERATIO,DELAY_35us);     // 방사율 READ, 반드시 35us
      
          //Serial.print("Object Temp : ");             // 하이퍼터미널 출력
          Serial.println((float(iOBJECT)/10 + 4),1);
          //Serial.print("     Sensor Temp : ");
          //Serial.print(float(iSENSOR)/10,1);
          //Serial.print("    Eratio : ");
          //Serial.println(float(cEratio)/100,2); 
          delay(500);
          SPI_COMMAND(LASER,DELAY_10us);
        }
        /*if(Laser_Flag == 1){
          Laser_Flag = 0;
          SPI_COMMAND(LASER,DELAY_10us);
          while(digitalRead(2)==LOW);
          attachInterrupt(digitalPinToInterrupt(2), LASER_ISR, FALLING); 
        }*/
      }
      LASER_ISR();
      count = 0;
    }
    income = '\0'; //전송한 income내용 초기화
  }
}
