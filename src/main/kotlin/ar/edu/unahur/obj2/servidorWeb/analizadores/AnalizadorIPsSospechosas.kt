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
    if(ipsSus.any({it == respuestaHttp.pedido.ip })) {
      modulosConsultados.add(modulo)
      pedidosSospechosos.add(respuestaHttp.pedido)

      clienteMail.enviar(
        correoContacto.toString(),
        "IpSospechosa",
        "la ip: ${respuestaHttp.pedido.ip} se la considera sospechosa /n" +
              "Realizó pedidos${pedidosSospechosos.filter{ it.ip == respuestaHttp.pedido.ip }.size} /n" +
              "el modulo mas consultado fue ${moduloMasConsultado()} /n" +
              "el conjunto de Ip sopechosas con la misma ruta son ${pedidosSospechosos.filter { ServidorWeb.obtenerRutaUrl(it.url) == ServidorWeb.obtenerRutaUrl(respuestaHttp.pedido.url) }}")
    }
  }

/*
  // TODO: este método está solo para mostrar cómo hacer un test con mocks,
  // borrarlo cuando haya métodos de verdad...
  fun enviarMailDePrueba(correoContacto: String,) {
    clienteMail.enviar(
      "prueba@abcd.com.ar",
      "123 Probando",
      "Hola... sí, hola..."
    );
  }
*/
}

class monitorConDeteccionDeDemora(val demoraMaxima : Int) : Analizador(){
  fun analizar(respuestaHttp: RespuestaHttp,modulo: ServidorWeb.Modulo){
    if(respuestaHttp.tiempo > demoraMaxima)
      Consola.escribirLinea("${respuestaHttp.pedido.ip} ${respuestaHttp.pedido.fechaHora} GET ${respuestaHttp.pedido.url} ${respuestaHttp.codigo} ${respuestaHttp.tiempo}")
    else
      Consola.escribirLinea("DEMORADA - ${respuestaHttp.pedido.ip} ${respuestaHttp.pedido.fechaHora} GET ${respuestaHttp.pedido.url} ${respuestaHttp.codigo} ${respuestaHttp.tiempo}")
  }
}

class estadistica() : Analizador(){
  val respuestasRecibidas = mutableListOf<RespuestaHttp>()

  fun analizar(){

  }

  fun tiempoDeRespuestaPromedio() = respuestasRecibidas.size / (respuestasRecibidas.sumBy { it.tiempo })

  fun cantidadDePedidosEntre(tiempo1 : LocalDateTime, tiempo2 : LocalDateTime) = respuestasRecibidas.filter { it.pedido.fechaHora.isAfter(tiempo1) && it.pedido.fechaHora.isBefore(tiempo2)}.size

  fun cantidadDeRespuestasCon(palabra: String) = respuestasRecibidas.count{ it.body.contains(palabra)}

  fun porcentajeDePedidosConExito() = respuestasRecibidas.size / (respuestasRecibidas.count{it.codigo == CodigoHttp.OK} * 100)

}
