package com.tipcalc.rest
import _root_.net.liftweb.util.Helpers
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.rest._
import _root_.net.liftweb.common._
import scala.xml.Elem
import java.math.{BigDecimal, RoundingMode}

object TipRest extends RestHelper {
  serve {
    case "api" :: "voice" :: "tip" :: _ XmlGet _ => voiceTip
    case "api" :: "voice" :: "calc" :: _ XmlGet _ => voiceCalc
    case "api" :: "sms" :: "tip" :: _ XmlGet _ => smsTip
  }
  
  def voiceTip = {
	(S.param("Digits"), S.param("error")) match {
	  case (Full(cmd), _) if cmd == "1" =>
        <Response>
          <Say>Goodbye.</Say>
        </Response>
	  case (_, Full(error)) if error == "true" =>
        <Response>
          <Gather finishOnKey="#" timeout="10" action="/api/voice/calc.xml" method="GET">
            <Say>Invalid bill amount. Please enter the bill amount and then press pound.</Say>
          </Gather>
        </Response>
	  case (_, _) =>
        <Response>
          <Gather finishOnKey="#" timeout="10" action="/api/voice/calc.xml" method="GET">
            <Say>Hello. Please enter the bill amount and then press pound.</Say>
          </Gather>
        </Response>
	}
  }
  
  def voiceCalc = {
	S.param("Digits") match {
	  case Full(amount) =>
	    try
	    {
	      val result = calculateTip(amount)
          <Response>
            <Say>15% gratuity of ${result._1} is ${result._2}. 18% gratuity of ${result._1} is ${result._3}.</Say>
            <Gather numDigits="1" timeout="10" action="/api/voice/tip.xml" method="GET">
              <Say>To exit, press 1. Press any other key to start over.</Say>
            </Gather>
          </Response>
	    } catch {
	      case _ => voiceError
	    }
	  case _ => voiceError
	}
  }
  
  def voiceError = {
    S.redirectTo("/api/voice/tip.xml?error=true")
  }
 
  def calculateTip(num: String) = {
    val amount = createBigDecimal(num)
    val orig = "%.2f".format(amount)
    val a15 = "%.2f".format(amount.multiply(createBigDecimal("0.15")).setScale(2, RoundingMode.HALF_EVEN))
    val a18 = "%.2f".format(amount.multiply(createBigDecimal("0.18")).setScale(2, RoundingMode.HALF_EVEN))
    (orig, a15, a18)
  }
  
  def createBigDecimal(num: String) = {
    new BigDecimal(num).setScale(2, RoundingMode.HALF_EVEN)
  }
  
  def smsTip = {
    val pattern = """(\d{1,3}(\,\d{3})*|(\d+))(\.\d{2})?""".r
    
    S.param("Body") match {
      case Full(body) =>
        pattern.findFirstMatchIn(body) match {
          case Some(amount) =>
            val result = calculateTip(amount.matched)
            <Response>
              <Sms>15% gratuity of ${result._1} is ${result._2}. 18% gratuity of ${result._1} is ${result._3}.</Sms>
            </Response>
          case _ => smsError
        }     
      case _ => smsError
    }
  }
  
  def smsError = {
    <Response>
      <Sms>Error parsing bill amount. Please try again.</Sms>
    </Response>
  }
}
