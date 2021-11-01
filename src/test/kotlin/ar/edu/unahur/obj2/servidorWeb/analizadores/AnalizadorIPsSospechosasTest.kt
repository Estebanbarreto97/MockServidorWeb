package ar.edu.unahur.obj2.servidorWeb.analizadores

import ar.edu.unahur.obj2.servidorWeb.CodigoHttp
import ar.edu.unahur.obj2.servidorWeb.PedidoHttp
import ar.edu.unahur.obj2.servidorWeb.RespuestaHttp
import ar.edu.unahur.obj2.servidorWeb.ServidorWeb
import ar.edu.unahur.obj2.servidorWeb.integraciones.ClienteMail
import ar.edu.unahur.obj2.servidorWeb.integraciones.Consola
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.LocalDateTime

class Analizadores : DescribeSpec({
  val clienteMailMock = mockk<ClienteMail>()
  AnalizadorIPsSospechosas.clienteMail = clienteMailMock

  describe("analizador de IP sospechosa") {
    val analizadorIP = AnalizadorIPsSospechosas(mutableListOf("192.168.256.65", "192.168.1.2"), "discord@hotmail.com")
    val pedido1 = PedidoHttp("192.168.256.65", "http://losreubicados.com/ideas.pdf", LocalDateTime.of(2020, 10, 30, 11, 50, 12))
    val respuesta1 = RespuestaHttp(CodigoHttp.OK, "Todo bien, soy respuesta1", 20, pedido1)
    val modulo1 = ServidorWeb.Modulo(listOf("hola, que tal"), "bien", 25, "Modulo1")
    it("recibe IP sospechosa") {
      every{ clienteMailMock.enviar(any(), any(), any()) } just Runs
      analizadorIP.analizar(respuesta1, modulo1)
      verify { clienteMailMock.enviar("discord@hotmail.com", "IpSospechosa"
        , "la ip: 192.168.256.65 se la considera sospechosa\n" +
                "Realizó 1 pedidos\n" +
                "el modulo mas consultado fue Modulo1\n" +
                "el conjunto de Ip sopechosas con la misma ruta son [192.168.256.65]") }
    }

  }
  describe("monitor con detección de demora de respuesta") {
    val monitor = monitorConDeteccionDeDemora(25)
    val pedido1 = PedidoHttp("192.168.256.65", "http://losreubicados.com/ideas.pdf", LocalDateTime.of(2020, 10, 30, 11, 50, 12))
    val respuesta1 = RespuestaHttp(CodigoHttp.OK, "Todo bien, soy respuesta1", 30, pedido1)
    val respuesta2 = RespuestaHttp(CodigoHttp.OK, "Todo bien, soy respuesta1", 18, pedido1)
    val modulo1 = ServidorWeb.Modulo(listOf("hola, que tal"), "bien", 35, "Modulo1")
    it("respuesta demorada"){
      val consolaMock = mockk<Consola>()
      monitor.consolita = consolaMock
      every{consolaMock.escribirLinea(any())} just Runs
      monitor.analizar(respuesta1, modulo1)
      verify {consolaMock.escribirLinea("DEMORADA - 192.168.256.65 2020-10-30T11:50:12 GET http://losreubicados.com/ideas.pdf OK 30") }
    }
    it("respuesta sin demora") {
      val consolaMock = mockk<Consola>()
      monitor.consolita = consolaMock
      every{consolaMock.escribirLinea(any())} just Runs
      monitor.analizar(respuesta2, modulo1)
      verify {consolaMock.escribirLinea("192.168.256.65 2020-10-30T11:50:12 GET http://losreubicados.com/ideas.pdf OK 18") }
    }
  }

  describe("estadísticas") {
    val pedido1 = PedidoHttp("192.168.256.65", "http://losreubicados.com/ideas.pdf", LocalDateTime.of(2020, 10, 30, 11, 50, 12))
    val respuesta1 = RespuestaHttp(CodigoHttp.OK, "Todo bien, soy respuesta1", 30, pedido1)
    val respuesta2 = RespuestaHttp(CodigoHttp.OK, "Todo bien, soy respuesta2", 25, pedido1)
    val respuesta3 = RespuestaHttp(CodigoHttp.OK, "Todo bien, soy respuesta3", 18, pedido1)
    val modulo1 = ServidorWeb.Modulo(listOf("hola, que tal"), "bien", 35, "Modulo1")
    val consolaMock = mockk<Consola>()
    val analizadorEstadistica = estadistica()
    analizadorEstadistica.consolita = consolaMock
    analizadorEstadistica.respuestasRecibidas.add(respuesta2)
    analizadorEstadistica.respuestasRecibidas.add(respuesta3)
    it("tiempo de respuesta promedio") {
      every{consolaMock.escribirLinea(any())} just Runs
      every{consolaMock.leerLinea()} returns "1"
      analizadorEstadistica.analizar(respuesta1, modulo1)
      verify {consolaMock.escribirLinea("El tiempo de respuesta promedio es de 24")}
    }
    it("cantidad de respuestas entre 2 fechas") {
      every{consolaMock.escribirLinea(any())} just Runs
      every{consolaMock.leerLinea()} returnsMany listOf("2", "2020-10-30T11:45:12Z", "2020-10-30T11:55:12Z")
      analizadorEstadistica.analizar(respuesta1, modulo1)
      verify {consolaMock.escribirLinea("La cantidad de pedidos entre las fechas dadas es de 3")}
    }
    it("cantidad de respuestas con cierta palabra") {
      every{consolaMock.escribirLinea(any())} just Runs
      every{consolaMock.leerLinea()} returnsMany  listOf("3", "Todo")
      analizadorEstadistica.analizar(respuesta1, modulo1)
      verify {consolaMock.escribirLinea("La cantidad de respuestas con la palabra seleccionada es de 3")}
    }
    it("porcentaje de pedidos con éxito") {
      val respuesta4 = RespuestaHttp(CodigoHttp.NOT_FOUND, "Todo bien, soy respuesta3", 31, pedido1)
      analizadorEstadistica.respuestasRecibidas.add(respuesta4)
      every{consolaMock.escribirLinea(any())} just Runs
      every{consolaMock.leerLinea()} returns "4"
      analizadorEstadistica.analizar(respuesta1, modulo1)
      verify {consolaMock.escribirLinea("El porcentaje de pedidos con éxito es de 75")}
    }
  }


})
