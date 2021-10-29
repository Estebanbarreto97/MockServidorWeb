package ar.edu.unahur.obj2.servidorWeb.analizadores

import ar.edu.unahur.obj2.servidorWeb.PedidoHttp
import ar.edu.unahur.obj2.servidorWeb.RespuestaHttp
import ar.edu.unahur.obj2.servidorWeb.ServidorWeb
import ar.edu.unahur.obj2.servidorWeb.integraciones.ClienteMail
import ar.edu.unahur.obj2.servidorWeb.integraciones.Consola
import ar.edu.unahur.obj2.servidorWeb.integraciones.DiscordMail

abstract class Analizador(){

}


class AnalizadorIPsSospechosas(val ipsSus : MutableList<String>,val correoContacto : String) : Analizador(){
  // Lo ponemos en un companion object para que todas las instancias usen el mismo
  val modulosConsultados = mutableListOf<ServidorWeb.Modulo>()
  companion object {
    var clienteMail: ClienteMail = DiscordMail(
      "reubicaditos",
      "900118123464777808",
      "iWwZt3AOhTJ4XHVHDXa_vLgmiHy6SZbjEeaPZOMFqeRbAVDDwM152-SBUadUN4yUTtYv"
    );
  }
  fun moduloMasConsultado() = modulosConsultados.groupingBy { it }.eachCount().maxByOrNull{it.value}!!.key

  fun analizar(respuestaHttp: RespuestaHttp,modulo: ServidorWeb.Modulo){
    modulosConsultados.add((modulo))
    if(ipsSus.any({it == respuestaHttp.pedido.ip })) {
      clienteMail.enviar(
        correoContacto.toString(),
        "IpSospechosa",
      "la ip: ${respuestaHttp.pedido.ip} se la considera sospechosa /n" +
              "Realizó ${ipregistrada(respuestaHttp.pedido).hizoPedido()} pedidos /n" +
              "el modulo mas consultado fue ${moduloMasConsultado()}")
    }
  }

  fun ipregistrada(pedidoHttp: PedidoHttp) : Ipsospechosa{
    return Ipsospechosa(pedidoHttp.ip)
  }

  class Ipsospechosa(val ip : String,var pedidosHechos : Int = 0){
      fun hizoPedido() : Int{
        pedidosHechos = pedidosHechos + 1
        return pedidosHechos
      }
  }

  // TODO: este método está solo para mostrar cómo hacer un test con mocks,
  // borrarlo cuando haya métodos de verdad...
  fun enviarMailDePrueba(correoContacto: String,) {
    clienteMail.enviar(
      "prueba@abcd.com.ar",
      "123 Probando",
      "Hola... sí, hola..."
    );
  }

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

}

