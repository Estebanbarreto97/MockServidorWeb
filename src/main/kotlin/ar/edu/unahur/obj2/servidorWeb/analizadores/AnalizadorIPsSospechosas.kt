package ar.edu.unahur.obj2.servidorWeb.analizadores

import ar.edu.unahur.obj2.servidorWeb.CodigoHttp
import ar.edu.unahur.obj2.servidorWeb.PedidoHttp
import ar.edu.unahur.obj2.servidorWeb.RespuestaHttp
import ar.edu.unahur.obj2.servidorWeb.ServidorWeb
import ar.edu.unahur.obj2.servidorWeb.integraciones.ClienteMail
import ar.edu.unahur.obj2.servidorWeb.integraciones.Consola
import ar.edu.unahur.obj2.servidorWeb.integraciones.DiscordMail
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

abstract class Analizador(){

}

class AnalizadorIPsSospechosas(val ipsSus : MutableList<String>,val correoContacto : String) : Analizador(){
  // Lo ponemos en un companion object para que todas las instancias usen el mismo
  val modulosConsultados = mutableListOf<ServidorWeb.Modulo>()
  val pedidosSospechosos = mutableListOf<PedidoHttp>()

  companion object {
    var clienteMail: ClienteMail = DiscordMail(
      "reubicaditos",
      "900118123464777808",
      "iWwZt3AOhTJ4XHVHDXa_vLgmiHy6SZbjEeaPZOMFqeRbAVDDwM152-SBUadUN4yUTtYv"
    );
  }

  fun moduloMasConsultado() = modulosConsultados.groupingBy { it }.eachCount().maxByOrNull{it.value}!!.key

  fun analizar(respuestaHttp: RespuestaHttp,modulo: ServidorWeb.Modulo){
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

class monitorConDeteccionDeDemora(val demoraMaxima : Int) : Analizador(){
  var consolita = Consola

  fun analizar(respuestaHttp: RespuestaHttp,modulo: ServidorWeb.Modulo){
    if(respuestaHttp.tiempo < demoraMaxima)
      consolita.escribirLinea("${respuestaHttp.pedido.ip} ${respuestaHttp.pedido.fechaHora} GET ${respuestaHttp.pedido.url} ${respuestaHttp.codigo} ${respuestaHttp.tiempo}")
    else
      consolita.escribirLinea("DEMORADA - ${respuestaHttp.pedido.ip} ${respuestaHttp.pedido.fechaHora} GET ${respuestaHttp.pedido.url} ${respuestaHttp.codigo} ${respuestaHttp.tiempo}")
  }
}

class estadistica() : Analizador(){
  val respuestasRecibidas = mutableListOf<RespuestaHttp>()
  var consolita = Consola

  fun obtenerConsulta() {
    consolita.escribirLinea("¿Qué desea hacer")
    consolita.escribirLinea("1-Tiempo de respuesta promedio")
    consolita.escribirLinea("2- Cantidad de pedidos entre 2 tiempos")
    consolita.escribirLinea("3- Cantidad de respuestas con cierta palabra")
    consolita.escribirLinea("4- El porcentaje de pedidos con éxito")
    when (consolita.leerLinea()) {
      "1" -> consolita.escribirLinea("El tiempo de respuesta promedio es de ${tiempoDeRespuestaPromedio()}")
      "2" -> consolita.escribirLinea("La cantidad de pedidos entre las fechas dadas es de ${cantidadDePedidosEntre(obtenerFechaInicio(), obtenerFechaFin())}")
      "3" -> consolita.escribirLinea("La cantidad de respuestas con la palabra seleccionada es de ${cantidadDeRespuestasCon(obtenerString())}")
      "4" -> consolita.escribirLinea("El porcentaje de pedidos con éxito es de ${porcentajeDePedidosConExito()}")
    }
  }

  fun obtenerFechaInicio(): LocalDateTime {
    consolita.escribirLinea("Ingrese la fecha inicial en formato YY-MM-DD-HH-MM-SS")
    return LocalDateTime.parse(consolita.leerLinea(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }

  fun obtenerFechaFin(): LocalDateTime {
    consolita.escribirLinea("Ingrese la fecha final en formato YY-MM-DD-HH-MM-SS")
    return LocalDateTime.parse(consolita.leerLinea(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }

  fun obtenerString(): String {
    consolita.escribirLinea("Ingrese la palabra que desea buscar")
    return consolita.leerLinea().toString()
  }

  fun analizar(respuestaHttp: RespuestaHttp, modulo: ServidorWeb.Modulo){
    respuestasRecibidas.add(respuestaHttp)
    obtenerConsulta()
  }

  fun tiempoDeRespuestaPromedio() = (respuestasRecibidas.sumBy { it.tiempo }) / respuestasRecibidas.size

  fun cantidadDePedidosEntre(tiempo1 : LocalDateTime, tiempo2 : LocalDateTime) = respuestasRecibidas.filter { it.pedido.fechaHora.isAfter(tiempo1) && it.pedido.fechaHora.isBefore(tiempo2)}.size

  fun cantidadDeRespuestasCon(palabra: String) = respuestasRecibidas.count{ it.body.contains(palabra)}

  fun porcentajeDePedidosConExito() = (respuestasRecibidas.count{it.codigo == CodigoHttp.OK} * 100) / respuestasRecibidas.size

}
