#include <Debug.h>
#include <JSN270.h>
#include <Arduino.h>
#include <SoftwareSerial.h>
#include <Servo.h>
#define SSID      "JW"    // 와이파이 SSID
#define KEY       "39397610"    // 와이파이 비밀번호
#define AUTH       "WPA2"     // 와이파이 네트워크 보안 (NONE, WEP, WPA, WPA2)
#define USE_DHCP_IP 1
#if !USE_DHCP_IP
#define MY_IP          "192.168.1.133"
#define SUBNET         "255.255.255.0"
#define GATEWAY        "192.168.1.254"
#endif
#define SERVER_PORT    80
#define PROTOCOL       "TCP"

Servo servo;  
SoftwareSerial mySerial(3, 2); // RX, TX
JSN270 JSN270(&mySerial);

void setup() {
  char c;
  char android;
  servo.attach(7);
  servo.write(50);
  mySerial.begin(9600);  //통신속도
  Serial.begin(9600);
  Serial.println("--------- JSN270 HTTP server --------");
  delay(5000);
  delay(1000);
  JSN270.sendCommand("at+ver\r");
  delay(5);
  while(JSN270.receive((uint8_t *)&c, 1, 1000) > 0) {
    Serial.print((char)c);
  }
  delay(1000);
#if USE_DHCP_IP
  JSN270.dynamicIP();
#else
  JSN270.staticIP(MY_IP, SUBNET, GATEWAY);
#endif    
    
  if (JSN270.join(SSID, KEY, AUTH)) {
    Serial.println("WiFi connect to " SSID);
  }
  else {
    Serial.println("Failed WiFi connect to " SSID);
    Serial.println("Restart System");

    return;
  }    
  delay(1000);

  JSN270.sendCommand("at+wstat\r");
  delay(5);
  while(JSN270.receive((uint8_t *)&c, 1, 1000) > 0) {
    Serial.print((char)c);
  }
  delay(1000);        

  JSN270.sendCommand("at+nstat\r");
  delay(5);
  while(JSN270.receive((uint8_t *)&c, 1, 1000) > 0) {
    Serial.print((char)c);
  }
  delay(1000);
}

void loop() {
  if (!JSN270.server(SERVER_PORT, PROTOCOL)) {
    Serial.println("Failed connect ");
    Serial.println("Restart System");
  } else {
    Serial.println("Waiting for connection...");
  }
      
  String currentLine = "";               //클라이언트로부터 수신 데이터를 저장할 문자열을 생성 
  String andreceive = "";
  int get_http_request = 0;              //http 응답 변수

  while (1) {
    
    if (mySerial.overflow()) {          //버퍼크기 확인
      Serial.println("SoftwareSerial overflow!");
    }
   
    if (JSN270.available() > 0) {
      char android = JSN270.read();
      Serial.print(android);

      if (android == '\n') {
                           
        if (andreceive.length() == 0) {
          if (get_http_request) {
            Serial.println("new client");
            JSN270.sendCommand("at+exit\r");
            delay(100);
            JSN270.println("HTTP/1.1 200 OK");    //html 링크 소스 받아오기
            JSN270.println("Content-type:text/html");  //html 링크 소스 받아오기
            JSN270.println();
            JSN270.print("Rocker Controler <br>");
            JSN270.print("Click <a href=\"/close\">here</a> lock<br>");
            JSN270.print("Click <a href=\"/open\">here</a> unlock<br>");
            JSN270.println();
            delay(1000);
            JSN270.print("+++");
            delay(100);
            break;
          }
        }
      
        else {        

          if (andreceive.startsWith("close")) {    //문 잠그기 
          
            get_http_request = 1;
            servo.write(150);
            delay(1000);
          }
          else if (andreceive.startsWith("open")) {    //문 열기
            
            get_http_request = 1;
            servo.write(50);
            delay(1000);
          }
          
          andreceive = "";
        }
      }
      else if (android != '\r') {   
        andreceive += android;   
      }
    }
  }

  JSN270.sendCommand("at+nclose\r");
  Serial.println("client disonnected");
}
