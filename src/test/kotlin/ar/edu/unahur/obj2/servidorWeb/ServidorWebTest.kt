package ar.edu.unahur.obj2.servidorWeb

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class ServidorWebTest : DescribeSpec({
  val modulo1 = Modulo(listOf("pdf", "jpeg", "png"), "Hola, soy modulo1", 20, "Modulo1")
  val modulo2 = Modulo(listOf("com", "doc", "py"), "Hola, soy modulo2", 18, "Modulo2")
  val modulo3 = Modulo(listOf("ar", "xl", "gif"), "Hola, soy modulo3", 25, "Modulo3")
  ServidorWeb.agregarModulo(modulo1)
  ServidorWeb.agregarModulo(modulo2)
  ServidorWeb.agregarModulo(modulo3)

  describe("Servidor web") {
    describe("procesar un pedido") {
      it("devuelve respuesta OK"){
        val pedidoOK = PedidoHttp("192.168.32.2", "http://losreubicados.com/ideas.pdf", LocalDateTime.of(2020, 10, 30, 11, 50, 12))
        ServidorWeb.respuestaDelModulo(pedidoOK).codigo.shouldBe(CodigoHttp.OK)
      }
      it("devuelve Not implemented codigo 501"){
        val pedido501 = PedidoHttp("192.168.32.2", "https://losreubicados.com/ideas.pdf", LocalDateTime.of(2020, 10, 30, 11, 50, 12))
        ServidorWeb.respuestaDelModulo(pedido501).codigo.shouldBe(CodigoHttp.NOT_IMPLEMENTED)
      }
    }
    describe("obtener string") {
      it("obtener protocolo url") {
        ServidorWeb.obtenerProtocoloUrl("http://losreubicados.com/ideas.pdf").shouldBe("http")
      }
      it("obtener extensión url") {
        ServidorWeb.obtenerExtensionUrl("http://losreubicados.com/ideas.pdf").shouldBe("pdf")
      }
      it("obtener ruta url") {
        ServidorWeb.obtenerRutaUrl("http://losreubicados.com/ideas.pdf").shouldBe("ideas.pdf")
      }
    }
    describe("prueba de módulos") {

      it("módulo que da respuesta OK") {
        val pedidoOK = PedidoHttp("192.168.32.2", "http://losreubicados.com/ideas.pdf", LocalDateTime.of(2020, 10, 30, 11, 50, 12))
        ServidorWeb.respuestaDelModulo(pedidoOK).shouldBe(RespuestaHttp(CodigoHttp.OK, modulo1.devuelve, modulo1.tiempo, pedidoOK))
      }
      it("respuesta del módulo 404") {
        val pedido404 = PedidoHttp("192.168.32.2", "http://losreubicados.com/ideas.js", LocalDateTime.of(2020, 10, 30, 11, 50, 12))
        ServidorWeb.respuestaDelModulo(pedido404).shouldBe(RespuestaHttp(CodigoHttp.NOT_FOUND, "", 10, pedido404))
      }
    }
  }

})
