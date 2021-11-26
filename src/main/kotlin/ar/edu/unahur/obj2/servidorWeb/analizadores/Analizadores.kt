package ar.edu.unahur.obj2.servidorWeb.analizadores

import ar.edu.unahur.obj2.servidorWeb.*
import ar.edu.unahur.obj2.servidorWeb.integraciones.ClienteMail
import ar.edu.unahur.obj2.servidorWeb.integraciones.Consola
import ar.edu.unahur.obj2.servidorWeb.integraciones.DiscordMail
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

interface Analizador{
  fun analizar(respuestaHttp: RespuestaHttp, modulo: Modulo){}
}

class AnalizadorIPsSospechosas(val ipsSus : MutableList<String>,val correoContacto : String) : Analizador{
  // Lo ponemos en un companion object para que todas las instancias usen el mismo
  val modulosConsultados = mutableListOf<Modulo>()
  val pedidosSospechosos = mutableListOf<PedidoHttp>()

  companion object {
    var clienteMail: ClienteMail = DiscordMail(
      "reubicaditos",
      "900118123464777808",
      "iWwZt3AOhTJ4XHVHDXa_vLgmiHy6SZbjEeaPZOMFqeRbAVDDwM152-SBUadUN4yUTtYv"
    );
  }

  fun moduloMasConsultado() = modulosConsultados.groupingBy { it }.eachCount().maxByOrNull{it.value}!!.key

  override fun analizar(respuestaHttp: RespuestaHttp, modulo: Modulo){
    if(ipsSus.any{it == respuestaHttp.pedido.ip }) {
      modulosConsultados.add(modulo)
      pedidosSospechosos.add(respuestaHttp.pedido)

      clienteMail.enviar(
        correoContacto.toString(),
        "IpSospechosa",
        "la ip: ${respuestaHttp.pedido.ip} se la considera sospechosa\n" +
              "Realizó ${pedidosSospechosos.filter{ it.ip == respuestaHttp.pedido.ip }.size} pedidos\n" +
              "el modulo mas consultado fue ${moduloMasConsultado().nombre}\n" +
              "el conjunto de Ip sopechosas con la misma ruta son ${(pedidosSospechosos.filter{ServidorWeb.obtenerRutaUrl(it.url) == ServidorWeb.obtenerRutaUrl(respuestaHttp.pedido.url)}).map{it.ip}}")
    }
  }
}

class monitorConDeteccionDeDemora(val demoraMaxima : Int) : Analizador{
  var consolita = Consola

  override fun analizar(respuestaHttp: RespuestaHttp, modulo: Modulo){
    if(respuestaHttp.tiempo < demoraMaxima)
      consolita.escribirLinea("${respuestaHttp.pedido.ip} ${respuestaHttp.pedido.fechaHora} GET ${respuestaHttp.pedido.url} ${respuestaHttp.codigo} ${respuestaHttp.tiempo}")
    else
      consolita.escribirLinea("DEMORADA - ${respuestaHttp.pedido.ip} ${respuestaHttp.pedido.fechaHora} GET ${respuestaHttp.pedido.url} ${respuestaHttp.codigo} ${respuestaHttp.tiempo}")
  }
}

class estadistica() : Analizador{
  val respuestasRecibidas = mutableListOf<RespuestaHttp>()

  override fun analizar(respuestaHttp: RespuestaHttp, modulo: Modulo){
    tiempoDeRespuestaPromedio()/*
    cantidadDePedidosEntre()  los comenté porque necesitan que se les pase algo por parametro
    cantidadDeRespuestasCon() */
    porcentajeDePedidosConExito()
  }

  fun tiempoDeRespuestaPromedio() {
    (respuestasRecibidas.sumBy { it.tiempo }) / respuestasRecibidas.size
  }

  fun cantidadDePedidosEntre(tiempo1 : LocalDateTime, tiempo2 : LocalDateTime) {
    respuestasRecibidas.filter { it.pedido.fechaHora.isAfter(tiempo1) && it.pedido.fechaHora.isBefore(tiempo2) }.size
  }
  fun cantidadDeRespuestasCon(palabra: String) {
    respuestasRecibidas.count{ it.body.contains(palabra)}
  }

  fun porcentajeDePedidosConExito() {
    (respuestasRecibidas.count { it.codigo == CodigoHttp.OK } * 100) / respuestasRecibidas.size
  }
}
